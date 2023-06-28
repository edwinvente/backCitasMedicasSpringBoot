package co.com.rapicredit.batchpayments.hexagonal.application.service;

import co.com.rapicredit.batchpayments.hexagonal.application.port.SendMessageSlackPort;
import co.com.rapicredit.batchpayments.hexagonal.infrastructure.rest.feignclient.SendMessageSlack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SendMessageSlackAdapter implements SendMessageSlackPort {
    @Autowired
    private SendMessageSlack sendMessageSlack;

    @Override
    public String createPayload(String message) {
        String msg = "{\"text\": \"%s.\"}";
        return String.format(msg, message);
    }

    @Override
    public void sendMessage(String payload) {
        sendMessageSlack.sendMessageSlack(payload);
    }
}
