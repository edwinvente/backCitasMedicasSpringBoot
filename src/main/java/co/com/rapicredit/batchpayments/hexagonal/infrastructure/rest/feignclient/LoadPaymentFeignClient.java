package co.com.rapicredit.batchpayments.hexagonal.infrastructure.rest.feignclient;

import co.com.rapicredit.batchpayments.hexagonal.domain.Pago;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "loadPayments", url = "http://localhost:8080")
public interface LoadPaymentFeignClient {
    @PostMapping(value = "/load/payment")
    Response loadPayment(@RequestBody Pago pago);
}
