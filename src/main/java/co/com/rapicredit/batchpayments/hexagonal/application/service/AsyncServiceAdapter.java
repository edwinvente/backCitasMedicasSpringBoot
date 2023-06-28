package co.com.rapicredit.batchpayments.hexagonal.application.service;

import co.com.rapicredit.batchpayments.hexagonal.domain.Pago;
import co.com.rapicredit.batchpayments.hexagonal.application.dto.PaymentReportDTO;
import co.com.rapicredit.batchpayments.hexagonal.application.dto.PaymentResponseDTO;
import co.com.rapicredit.batchpayments.hexagonal.domain.models.PaymentMethod;
import co.com.rapicredit.batchpayments.hexagonal.domain.models.PaymentReport;
import co.com.rapicredit.batchpayments.hexagonal.domain.models.PaymentReportItems;
import co.com.rapicredit.batchpayments.hexagonal.domain.repositorios.PaymentReportItemsRepository;
import co.com.rapicredit.batchpayments.hexagonal.domain.repositorios.PaymentReportRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AsyncServiceAdapter {
    @Autowired
    private SendMessageSlackAdapter sendMessageSlackAdapter;
    @Autowired
    private PaymentServiceAdapter paymentServiceAdapter;
    @Autowired
    private PaymentReportRepository paymentReportRepository;
    @Autowired
    private PaymentReportItemsRepository paymentReportItemsRepository;
    @Autowired
    private EmailServiceAdapter emailServiceAdapter;
    public List<PaymentReportDTO> reportDTOList = new ArrayList<>();
    public void setReportDTOList(List<PaymentReportDTO> reportDTOList) {
        this.reportDTOList = reportDTOList;
    }
    public List<Pago> paymentReportErrorDTOList = new ArrayList<>();
    public void setPaymentReportErrorDTOList(List<Pago> paymentReportErrorDTOList) {
        this.paymentReportErrorDTOList = paymentReportErrorDTOList;
    }
    private String finalStatus;
    public String getFinalStatus() { return finalStatus; }
    public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }
    @Async
    public CompletableFuture<Void> startQueue(Sheet sheet, PaymentReport backlogReport) {
        try {
            Thread.sleep(3000); // Hacemos un sleep de 3 segundos (3000 milisegundos)
            List<Pago> payments = this.readXlsx(sheet);
            this.sendPaymentsAsync(payments, backlogReport);
        } catch (InterruptedException e) { this.notifySlack(e.getMessage()); }
        catch (IOException e) { this.notifySlack(e.getMessage()); throw new RuntimeException(e); }
        return CompletableFuture.completedFuture(null);
    }
    public void notifySlack(String msg){
        String message = sendMessageSlackAdapter.createPayload("Error en el hilo: " + msg);
        sendMessageSlackAdapter.sendMessage(message);
    }
    public List<Pago> readXlsx(Sheet sheet) throws IOException {
        List<Pago> payments = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Salta la primera fila (encabezados)
            payments.add(this.createPayment(row));
        }
        return payments;
    }
    public void sendPaymentsAsync(List<Pago> payments, PaymentReport backlogReport) {
        //Enviamos los pagos al backend de manera asyncrona
        List<PaymentResponseDTO> paymentsGenerate = payments.stream()
                .map( payment -> paymentServiceAdapter.loadPayment(payment, paymentReportErrorDTOList))
                .toList();

        String messageBacklog = payments.size() > 0 ? "Pagos enviados" : "No se encontraron pagos...";
        backlogReport.setStatus(messageBacklog);
        this.setFinalStatus(messageBacklog);

        CompletableFuture.supplyAsync(() -> {
            this.storeReport(paymentsGenerate, backlogReport);
            return true;
        });
    }
    public Pago createPayment(Row row){
        Pago pago = new Pago();
        int indexCell = 0;
        try {
            pago.setFechaPago(row.getCell(0).getDateCellValue()); indexCell++;
            pago.setValorPago(BigDecimal.valueOf(row.getCell(1).getNumericCellValue())); indexCell++;
            pago.setIdCredito( (int)row.getCell(2).getNumericCellValue() ); indexCell++;
            pago.setMedioPago( (int)row.getCell(3).getNumericCellValue() );
            String statusRules = this.BusinessRules(pago);
            // reglas de negocio
            if (statusRules != null) {
                pago.setXlsxError("Fila del error: " + (row.getRowNum() + 1) + " nombre de la columna: " + this.getNameColumns(indexCell) + " --- motivo del error: "+ statusRules);
                reportDTOList.add( new PaymentReportDTO( (int)row.getCell(2).getNumericCellValue(), (row.getRowNum() + 1), this.getNameColumns(indexCell), statusRules ) );
                this.createXlsxWithErrors(row);
            }
        } catch (Exception e) {
            pago.setXlsxError("Fila del error: " + (row.getRowNum() + 1) + " nombre de la columna: " + this.getNameColumns(indexCell) + " --- Valor en el campo: " + row.getCell(indexCell).getStringCellValue() + " --- mensaje de error: " + this.getErrorColumns(indexCell));
            reportDTOList.add( new PaymentReportDTO( 0, (row.getRowNum() + 1), this.getNameColumns(indexCell), this.getErrorColumns(indexCell) ) );
            this.createXlsxWithErrors(row);
        }
        return pago;
    }
    public void createXlsxWithErrors(Row row){
        Pago payment = new Pago();
        for (int i = 0; i <= 3; i++) {
            Cell cell = row.getCell(i);
            Object cellValue = switch (cell.getCellType()) {
                case BLANK -> "";
                case BOOLEAN -> cell.getBooleanCellValue();
                case ERROR -> "Error"; // Valor por defecto para códigos de error
                case FORMULA -> cell.getCellFormula();
                case NUMERIC -> cell.getNumericCellValue();
                case STRING -> cell.getStringCellValue();
                default -> ""; // Valor por defecto para otros tipos de celda
            };

            switch (i) {
                case 0 -> {
                    try {
                        // convertir a Date
                        payment.setFechaPago(convert(cellValue.toString()));
                    } catch (Exception e) { payment.setFechaPago(null); }
                }
                case 1 -> {
                    try {
                        // convertir a BigDecimal
                        BigDecimal bigDecimal = new BigDecimal(cellValue.toString());
                        payment.setValorPago( bigDecimal );
                    } catch (NumberFormatException e) { payment.setValorPago(BigDecimal.valueOf(0)); }

                }
                case 2 -> {
                    try {
                        // convertir a int
                        double value = Double.parseDouble(cellValue.toString());
                        int integer = (int) Math.round(value);
                        payment.setIdCredito( integer );
                    } catch (NumberFormatException e) { payment.setIdCredito(0); }

                }
                case 3 -> {
                    try {
                        // convertir a int
                        double value = Double.parseDouble(cellValue.toString());
                        int integer = (int) Math.round(value);
                        payment.setMedioPago( integer );
                    } catch (NumberFormatException e) { payment.setMedioPago(0); }
                }
            }
        }
        paymentReportErrorDTOList.add(payment);
    }

    public Date convert(String source) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
                .toFormatter();
        if(source.contains(".")){
            double days = Double.parseDouble(source);
            LocalDate date = LocalDate.ofEpochDay((long)days- (365*70));
            return java.sql.Date.valueOf(date);
        }
        LocalDate date = LocalDate.parse(source, formatter);
        return java.sql.Date.valueOf(date);
    }
    public String BusinessRules(Pago payment){
        if (this.validatePaymentMethod(payment.getMedioPago()) == null) return "Medio de pago invalido";
        return null;
    }
    public PaymentMethod validatePaymentMethod(int paymentMethod){
        // En el proyecto base se debe de cambiar por la consulta a la tabla
        List<PaymentMethod> methods = new ArrayList<>();
        methods.add( new PaymentMethod(1, "Consignación Bancolombia") );
        methods.add( new PaymentMethod(2, "PSE") );
        methods.add( new PaymentMethod(3, "Débito automatico") );
        //methods.add( new PaymentMethod(4, "Consignación Davivienda") );
        return methods.stream()
                .filter(o -> o.getId() == paymentMethod)
                .findFirst()
                .orElse(null);
    }
    public String getNameColumns(int index){
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Fecha del pago");
        columnNames.add("Valor de pago");
        columnNames.add("ID del crédito");
        columnNames.add("Medio de pago");
        return columnNames.get(index);
    }
    public String getErrorColumns(int index){
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Se esperaba un campo de tipo fecha"); // Fecha del pago
        columnNames.add("Se esperaba un campo de tipo numero"); // Valor de pago
        columnNames.add("Se esperaba un campo de tipo numero"); // ID del crédito
        columnNames.add("Se esperaba un campo de tipo numero"); // Medio de pago
        return columnNames.get(index);
    }
    public void storeReport(List<PaymentResponseDTO> report, PaymentReport backlogReport){
        int effective = 0;
        int rechargers = 0;
        int totals = 0;
        List<PaymentReportItems> items = new ArrayList<>();
        for (PaymentResponseDTO payment: report) {
            if (Objects.equals(payment.getState(), "EXITOSO")) effective++;
            else rechargers++;
            totals++;
            items.add(this.createPaymentItemsReport(backlogReport.getId(), payment));
        }
        backlogReport.setSuccess(effective);
        backlogReport.setErrors(rechargers);
        paymentReportRepository.save(backlogReport);
        paymentReportItemsRepository.saveAll(items);

        try {
            this.sendMailReport(effective, rechargers, totals, items);
            setPaymentReportErrorDTOList(new ArrayList<>());
            setReportDTOList(new ArrayList<>());
        } catch (ParseException e) { e.getMessage(); }
        this.sendSlackMessage(backlogReport, effective, rechargers, totals);
    }
    public PaymentReportItems createPaymentItemsReport(Long reportId, PaymentResponseDTO payment){
        PaymentReportItems item = new PaymentReportItems();
        item.setPaymentReportId(Math.toIntExact(reportId));
        item.setLoanId(payment.getLoanId());
        item.setStatus(payment.getState());
        return item;
    }
    void sendSlackMessage(PaymentReport backlogReport, int effective, int rechargers, int totals){
        String body = "Proceso de carga masiva de pagos #" + backlogReport.getId() +
                "\n Pagos efectivos: " + effective +
                "\n Pagos rechazados: " + rechargers +
                "\n Pagos totales: " + totals +
                "\n Estado del proceso: " + backlogReport.getStatus();
        String message = sendMessageSlackAdapter.createPayload(body);
        sendMessageSlackAdapter.sendMessage(message);
    }
    void sendMailReport(int effective, int rechargers, int totals, List<PaymentReportItems> paymentReportItems) throws ParseException {
        List<PaymentReportItems> filterReport = paymentReportItems.stream()
                .filter(pay -> pay.getStatus().equals("ANULADO") || pay.getStatus().equals("PENDIENTE"))
                .collect(Collectors.toList());

        Map<String, Object> model = new HashMap<>();
        model.put("negocio", "Hildebrando");
        model.put("message", "Te brindamos los detalles del informe generado del proceso de pagos masivos");
        model.put("effective", effective);
        model.put("rechargers", rechargers);
        model.put("totals", totals);
        model.put("serverErrors", filterReport);
        model.put("excelErrors", reportDTOList);
        emailServiceAdapter.sendEmail("ecaicedo@rapicredit.com", "Reporte de pagos masivos", "report", model, paymentReportErrorDTOList);
    }

}
