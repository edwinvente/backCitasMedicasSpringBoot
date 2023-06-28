package co.com.rapicredit.batchpayments.hexagonal.application.dto;

import lombok.Data;
@Data
public class PaymentReportDTO {
    private int loandId;
    private int row;
    private String column;
    private String error;

    public PaymentReportDTO(int loandId, int row, String column, String error){
        this.loandId = loandId;
        this.row = row;
        this.column = column;
        this.error = error;
    }
}
