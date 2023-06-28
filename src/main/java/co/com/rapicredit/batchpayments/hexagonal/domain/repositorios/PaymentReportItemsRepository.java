package co.com.rapicredit.batchpayments.hexagonal.domain.repositorios;

import co.com.rapicredit.batchpayments.hexagonal.domain.models.PaymentReportItems;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentReportItemsRepository extends CrudRepository<PaymentReportItems, Long> {
}
