package co.com.rapicredit.batchpayments.hexagonal.domain.models.response;

import lombok.Data;

@Data
public class AwsResponse {
    public boolean status;
    public String message;
    public String patch;

    public AwsResponse(){
        this.status = true;
        this.message = "Porceso iniciado...";
        this.patch = "No se ha cargado el archivo...";
    }

    public boolean getStatus() { return this.status; }
}
