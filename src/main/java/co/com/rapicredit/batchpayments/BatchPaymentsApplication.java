package co.com.rapicredit.batchpayments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableFeignClients("co.com.rapicredit.batchpayments.hexagonal.infrastructure.rest.feignclient")
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
public class BatchPaymentsApplication {
	private static final Logger logger = LogManager.getLogger(BatchPaymentsApplication.class);


	public static void main(String[] args) {
		try {
			logger.info("RESTFul Application Starting.");
			SpringApplication.run(BatchPaymentsApplication.class, args);
			logger.info("RESTFul Application Started.");
		}
		catch(Exception e) {
			logger.error("RESTFul Application Failed to Start.",e);
		}

	}

}
