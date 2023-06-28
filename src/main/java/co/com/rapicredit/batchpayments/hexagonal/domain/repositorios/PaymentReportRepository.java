package co.com.rapicredit.batchpayments.hexagonal.domain.repositorios;

import co.com.rapicredit.batchpayments.hexagonal.domain.models.PaymentReport;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentReportRepository extends CrudRepository<PaymentReport, Long> {
    PaymentReport findById(int id);

}
