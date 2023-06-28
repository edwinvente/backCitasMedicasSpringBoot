package co.com.rapicredit.batchpayments.hexagonal.infrastructure.web.controllers;

import co.com.rapicredit.batchpayments.hexagonal.application.service.*;
import co.com.rapicredit.batchpayments.hexagonal.domain.Pago;

import co.com.rapicredit.batchpayments.hexagonal.application.dto.PaymentResponseDTO;
import co.com.rapicredit.batchpayments.hexagonal.domain.models.PaymentReport;
import co.com.rapicredit.batchpayments.hexagonal.domain.models.response.AwsResponse;
import co.com.rapicredit.batchpayments.hexagonal.domain.models.response.PaymentResponse;
import co.com.rapicredit.batchpayments.hexagonal.domain.repositorios.PaymentReportRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class ImportExcelController {
    @Autowired
    private SendMessageSlackAdapter sendMessageSlackAdapter;
    @Autowired
    private PaymentReportRepository paymentReportRepository;
    @Autowired
    private AwsServiceAdapter awsServiceAdapter;
    @Autowired
    private AsyncServiceAdapter asyncServiceAdapter;
    @Autowired
    private EmailServiceAdapter emailServiceAdapter;
    @PostMapping("/importar")
    public ResponseEntity<PaymentResponse> importXlsx(@RequestParam("file") MultipartFile file) {
        AwsResponse responseUploadFileToS3 = this.uploadFileToS3(file);
        PaymentResponse paymentResponse = this.createPaymentResponse(responseUploadFileToS3.getMessage(), responseUploadFileToS3.getStatus());
        PaymentReport backlogReport = this.createPaymentReport(responseUploadFileToS3.getPatch(), responseUploadFileToS3.getMessage());
        if (responseUploadFileToS3.getStatus()) {
            Sheet sheet = this.readFileXlsx(file, paymentResponse);
            if(sheet != null) this.startQueueProcess(sheet, backlogReport);
        }
        return ResponseEntity.ok(paymentResponse);
    }
    public AwsResponse uploadFileToS3(MultipartFile file) { return awsServiceAdapter.uploadFileToS3(file); }
    public Sheet readFileXlsx(MultipartFile file, PaymentResponse response)  {
        try{
            Workbook workbook = WorkbookFactory.create(file.getInputStream()); // Usa Apache POI para leer el archivo de Excel
            return workbook.getSheetAt(0);
        }catch (IOException e){
            response.setStatus("error en lectura del excel: " + e.getMessage());
            response.setMessage(e.getMessage());
            String message = sendMessageSlackAdapter.createPayload(e.getMessage());
            sendMessageSlackAdapter.sendMessage(message);
        }
       return null;
    }
    public void startQueueProcess(Sheet sheet, PaymentReport backlogReport ){ asyncServiceAdapter.startQueue(sheet, backlogReport); }
    public PaymentResponse createPaymentResponse( String message, boolean status ){ return new PaymentResponse(message, status ? "200" : "400"); }
    public PaymentReport createPaymentReport(String awsPatch, String status){
        PaymentReport paymentReport = new PaymentReport();
        paymentReport.setDate(this.getLoacalDate());
        paymentReport.setPatch( awsPatch );
        paymentReport.setUserId( new Random().nextInt(100) );
        paymentReport.setStatus(status);
        try { return paymentReportRepository.save(paymentReport); } catch (Exception e){ e.getMessage();}
        return null;
    }
    public Date getLoacalDate(){
        ZoneId bogotaZone = ZoneId.of("America/Bogota");
        ZonedDateTime bogotaDateTime = ZonedDateTime.now(bogotaZone);
        DateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try{
            date = currentDate.parse(bogotaDateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        } catch (Exception e){ e.getMessage(); }
        return date;
        /*ZoneId zoneId = ZoneId.of("America/Bogota");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return new DateTime(zonedDateTime);*/

    }
    @PostMapping("/load/payment")
    public ResponseEntity<PaymentResponseDTO> loadPayment(@RequestBody Pago pago){
        PaymentResponseDTO payment = new PaymentResponseDTO();

        List<String> states = new ArrayList<>();
        states.add("EXITOSO");
        states.add("PENDIENTE");
        states.add("ANULADO");

        payment.setLoanId(String.valueOf(new Random().nextInt(100)));
        payment.setState(getRandomElement(states));
        return ResponseEntity.ok(payment);
    }
    public String getRandomElement(List<String> list) {
        // Crea una instancia de la clase Random
        Random random = new Random();
        // Obtiene un índice aleatorio de la lista
        int index = random.nextInt(list.size());
        // Devuelve el elemento en la posición del índice aleatorio
        return list.get(index);
    }
    @GetMapping("/download-excel")
    public ResponseEntity<InputStreamResource> downloadExcel() throws IOException, ParseException {
        // Crear una lista de datos para escribir en el archivo de Excel
        List<Pago> dataList = new ArrayList<>();

        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        Date fecha = formato.parse(String.valueOf(LocalDate.now()));

        Pago paguito = new Pago();
        paguito.setFechaAplicacion( new Date() );
        paguito.setIdCredito(1);
        paguito.setMedioPago(3);
        paguito.setValorPago(BigDecimal.valueOf(154000));
        paguito.setXlsxError(null);

        dataList.add(paguito);

        // Crear el libro de trabajo de Excel
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Crear una hoja en el libro de trabajo
        XSSFSheet sheet = workbook.createSheet("Data");

        // Crear la fila de encabezado con los nombres de columna
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Fecha de la aplicación");
        headerRow.createCell(1).setCellValue("Valor de pago");
        headerRow.createCell(2).setCellValue("ID del crédito");
        headerRow.createCell(3).setCellValue("Medio de pago");

        // Crear una fila y poner algunos datos en cada celda
        int rowIdx = 1;
        for (Pago data : dataList) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(data.getFechaAplicacion());
            row.createCell(1).setCellValue(data.getValorPago().doubleValue());
            row.createCell(2).setCellValue(data.getIdCredito());
            row.createCell(3).setCellValue(data.getMedioPago());
        }

        // Crear un flujo de salida para escribir el archivo de Excel
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);

        // Crear un recurso de entrada a partir del flujo de salida
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(inputStream);

        // Devolver el archivo como una respuesta HTTP
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=reporte-fallidos.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(outputStream.size())
                .body(resource);
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }

}
