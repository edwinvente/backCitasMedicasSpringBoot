package co.com.rapicredit.batchpayments.hexagonal.domain.models.response;

import lombok.Data;

@Data
public class PaymentResponse {
    public String message;
    public String status;
    public PaymentResponse(String status, String message){
        this.status = status;
        this.message = message;
    }

}
