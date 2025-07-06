package dev.jvops.bank.config;

import dev.jvops.bank.api.external.notificationgateway.NotificationGatewayClient;
import dev.jvops.bank.api.external.paymentgateway.PaymentGatewayClient;
import dev.jvops.bank.api.external.paymentgateway.PaymentGatewayResponse;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestMockConfig {

    @Bean
    public PaymentGatewayClient paymentGatewayClient() {
        PaymentGatewayClient client = Mockito.mock(PaymentGatewayClient.class);

        PaymentGatewayResponse.DataResponse data = new PaymentGatewayResponse.DataResponse();
        data.setAuthorization(true);

        PaymentGatewayResponse response = new PaymentGatewayResponse();
        response.setStatus("success");
        response.setData(data);

        Mockito.when(client.authorizeTransaction()).thenReturn(response);

        return client;
    }

    @Bean
    public NotificationGatewayClient notificationGatewayClient() {
        NotificationGatewayClient client = Mockito.mock(NotificationGatewayClient.class);
        Mockito.doNothing().when(client).notifyTransaction();
        return client;
    }
}
