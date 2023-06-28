package co.com.rapicredit.batchpayments.hexagonal.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class PaymentReportItems {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private int paymentReportId;
    private String loanId;
    private String status;

}
