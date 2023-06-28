package co.com.rapicredit.batchpayments.hexagonal.application.port;

import co.com.rapicredit.batchpayments.hexagonal.domain.models.PaymentReport;

public interface PaymentReportPort {
    PaymentReport findById(int id);
    PaymentReport save(PaymentReport paymentReportEntity);

}
