package co.com.rapicredit.batchpayments.hexagonal.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.joda.time.DateTime;

import java.util.Date;

@Data
@Entity
public class PaymentReport {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private Date date;
    private int userId;
    private String status;
    private String patch;
    private int success;
    private int errors;
}
