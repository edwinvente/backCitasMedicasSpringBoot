package co.com.rapicredit.batchpayments.hexagonal.application.service;

import co.com.rapicredit.batchpayments.hexagonal.domain.Pago;

import jakarta.mail.util.ByteArrayDataSource;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EmailServiceAdapter {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailServiceAdapter(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(String to, String subject, String template, Map<String, Object> model, List<Pago> paymentReportErrorDTO) {
        try {
            MimeMessagePreparator preparator = mimeMessage -> {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);
                message.setFrom("ecaicedo@rapicredit.com", "Rapicredit Pagos");
                message.setTo(to);
                message.setSubject(subject);
                message.addAttachment("informe_de_pagos_no_efectivos.xlsx", createExcel(paymentReportErrorDTO));
                String html = templateEngine.process(template, new Context(Locale.getDefault(), model));
                message.setText(html, true);
            };
            javaMailSender.send(preparator);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }
    public ByteArrayDataSource createExcel(List<Pago> paymentReportErrorDTOList) {
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
        for (Pago data : paymentReportErrorDTOList) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(data.getFechaPago());
            row.createCell(1).setCellValue(data.getValorPago().intValue());
            row.createCell(2).setCellValue(data.getIdCredito());
            row.createCell(3).setCellValue(data.getMedioPago());
        }
        // Crear un flujo de salida para escribir el archivo de Excel
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try { workbook.write(outputStream); } catch (Exception e){ e.getMessage(); }
        // Adjuntar el archivo de Excel al mensaje
        return new ByteArrayDataSource(outputStream.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}
