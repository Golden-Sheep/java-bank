package dev.jvops.bank.api.external.paymentgateway;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "payment-gateway", url = "${payment.gateway.url}")
public interface PaymentGatewayClient {

    @GetMapping
    PaymentGatewayResponse authorizeTransaction();
}