package co.com.rapicredit.batchpayments.hexagonal.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Pago {
    private Date fechaAplicacion;
    private Date fechaPago;
    private BigDecimal valorPago;
    private int idCredito;
    private int medioPago;
    private String xlsxError;
}
