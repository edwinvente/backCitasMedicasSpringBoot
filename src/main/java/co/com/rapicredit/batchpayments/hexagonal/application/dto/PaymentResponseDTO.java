package co.com.rapicredit.batchpayments.hexagonal.application.dto;

import lombok.Data;

@Data
public class PaymentResponseDTO {
    private String loanId;
    private String state;
}
