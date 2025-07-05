package dev.jvops.bank.config;

import dev.jvops.bank.api.external.notificationgateway.NotificationGatewayClient;
import dev.jvops.bank.api.external.paymentgateway.PaymentGatewayClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestMockConfig {

    @Bean
    public PaymentGatewayClient paymentGatewayClient() {
        return Mockito.mock(PaymentGatewayClient.class);
    }

    @Bean
    public NotificationGatewayClient notificationGatewayClient() {
        return Mockito.mock(NotificationGatewayClient.class);
    }
}
