package co.com.rapicredit.batchpayments.hexagonal.application.port;

public interface SendMessageSlackPort {
    public String createPayload(String message);

    public void sendMessage(String payload);

}
