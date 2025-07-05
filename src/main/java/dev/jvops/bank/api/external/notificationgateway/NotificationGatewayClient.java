package dev.jvops.bank.api.external.notificationgateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
        import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class NotificationGatewayClient {

    @Value("${notification.gateway.url}")
    private String notificationGatewayUrl;

    private final ObjectMapper objectMapper;

    private RestTemplate createRestTemplateWithTimeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(20_000); // 20 seconds
        factory.setReadTimeout(20_000);    // 20 seconds
        return new RestTemplate(factory);
    }

    public boolean NotifyTransaction() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = createRestTemplateWithTimeout().exchange(
                    URI.create(notificationGatewayUrl),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                return true;
            }

        } catch (Exception e) {
            System.err.println("Failed to authorize transaction: " + e.getMessage());
        }

        return false;
    }
}


