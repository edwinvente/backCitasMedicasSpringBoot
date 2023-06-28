package co.com.rapicredit.batchpayments.hexagonal.domain.models;

import lombok.Data;

@Data
public class PaymentMethod {
    private int id;
    private String bankDetail;

    public PaymentMethod(int id, String bankDetail){
        this.id = id;
        this.bankDetail = bankDetail;
    }
}
