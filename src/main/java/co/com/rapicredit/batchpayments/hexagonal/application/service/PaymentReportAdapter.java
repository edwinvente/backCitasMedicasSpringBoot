package co.com.rapicredit.batchpayments.hexagonal.application.service;

import co.com.rapicredit.batchpayments.hexagonal.application.port.PaymentReportPort;
import co.com.rapicredit.batchpayments.hexagonal.domain.models.PaymentReport;
import co.com.rapicredit.batchpayments.hexagonal.domain.repositorios.PaymentReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentReportAdapter implements PaymentReportPort {
    @Autowired
    private PaymentReportRepository paymentReportRepository;
    @Override
    public PaymentReport findById(int id) {
        return paymentReportRepository.findById(id);
    }

    @Override
    public PaymentReport save(PaymentReport paymentReportEntity) {
        return paymentReportRepository.save(paymentReportEntity);
    }
}
