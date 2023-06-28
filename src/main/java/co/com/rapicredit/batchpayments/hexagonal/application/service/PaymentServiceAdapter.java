package co.com.rapicredit.batchpayments.hexagonal.application.service;

import co.com.rapicredit.batchpayments.hexagonal.application.port.PaymentServicePort;
import co.com.rapicredit.batchpayments.hexagonal.domain.Pago;

import co.com.rapicredit.batchpayments.hexagonal.application.dto.PaymentResponseDTO;
import co.com.rapicredit.batchpayments.hexagonal.infrastructure.rest.feignclient.LoadPaymentFeignClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class PaymentServiceAdapter implements PaymentServicePort {
    @Autowired
    private LoadPaymentFeignClient loadPaymentFeifnClient;
    @Override
    public PaymentResponseDTO loadPayment(Pago pago, List<Pago> paymentReportErrorDTOList) {
        if (pago.getXlsxError() != null) return this.createPaymentWithXlsxError(pago);
        PaymentResponseDTO uniqueReport = new PaymentResponseDTO();
        try {
            Response response = loadPaymentFeifnClient.loadPayment(pago);
            // Crea un mapeador Jackson
            ObjectMapper mapper = new ObjectMapper();
            // Parsea el cuerpo de la respuesta a un objeto de la clase PaymentResponseDTO
            uniqueReport = mapper.readValue(response.body().asInputStream(), PaymentResponseDTO.class);
        } catch (FeignException e) {
            uniqueReport.setState("Error: " + e.getMessage());
            uniqueReport.setLoanId(null);
        } catch (IOException e) {
            uniqueReport.setState("Error: " + e.getMessage());
            uniqueReport.setLoanId(null);
            //throw new RuntimeException(e);
        }
        this.addPaymentReportDTO(pago, uniqueReport, paymentReportErrorDTOList);
        return uniqueReport;
    }
    public void addPaymentReportDTO(Pago payment, PaymentResponseDTO response, List<Pago> paymentReportErrorDTOList){
        if (!Objects.equals(response.getState(), "EXITOSO")){
            Pago paymentReportErrorDTO = new Pago();
            paymentReportErrorDTO.setFechaPago(payment.getFechaPago());
            paymentReportErrorDTO.setValorPago(payment.getValorPago());
            paymentReportErrorDTO.setIdCredito(payment.getIdCredito());
            paymentReportErrorDTO.setMedioPago(payment.getMedioPago());
            paymentReportErrorDTOList.add(paymentReportErrorDTO);
        }
    }
    public PaymentResponseDTO createPaymentWithXlsxError(Pago payment){
        PaymentResponseDTO paymentErrorXlsx = new PaymentResponseDTO();
        paymentErrorXlsx.setLoanId(String.valueOf(payment.getIdCredito()));
        paymentErrorXlsx.setState(payment.getXlsxError());
        return paymentErrorXlsx;
    }

}
