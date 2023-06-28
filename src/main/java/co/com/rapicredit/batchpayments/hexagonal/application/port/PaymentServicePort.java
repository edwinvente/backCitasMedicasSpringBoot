package co.com.rapicredit.batchpayments.hexagonal.application.port;

import co.com.rapicredit.batchpayments.hexagonal.domain.Pago;
import co.com.rapicredit.batchpayments.hexagonal.application.dto.PaymentResponseDTO;

import java.util.List;

public interface PaymentServicePort {
    PaymentResponseDTO loadPayment(Pago pago, List<Pago> paymentReportErrorDTOList);
}
