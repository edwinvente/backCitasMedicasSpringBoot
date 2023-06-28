package co.com.rapicredit.batchpayments.hexagonal.application.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PaymentDTO {
    private String loanid;
    private String method;
    private Date datetime;
    private double amoutn;
    private String source;
    private String user;
}
