package co.com.rapicredit.batchpayments.hexagonal.infrastructure.rest.feignclient;

import co.com.rapicredit.batchpayments.hexagonal.domain.Pago;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "slackNotifications", url = "https://hooks.slack.com/services")
public interface SendMessageSlack {

    @PostMapping(value = "/T037QARD9AL/B04HKUEMBCG/DwU27fZ7HYIaOg6j91uNYllu")
    Map<String, Object> sendMessageSlack(@RequestBody String msg);

}
