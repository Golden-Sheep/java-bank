package dev.jvops.bank.api.external.notificationgateway;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "notification-gateway", url = "${notification.gateway.url}")
public interface NotificationGatewayClient {

    @PostMapping
    void notifyTransaction();
}

