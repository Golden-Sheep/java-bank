package dev.jvops.bank.api.external.paymentgateway;

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
public class PaymentGatewayClient {

    @Value("${payment.gateway.url}")
    private String paymentGatewayUrl;

    private final ObjectMapper objectMapper;

    private RestTemplate createRestTemplateWithTimeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(20_000); // 20 seconds
        factory.setReadTimeout(20_000);    // 20 seconds
        return new RestTemplate(factory);
    }

    public boolean authorizeTransaction() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = createRestTemplateWithTimeout().exchange(
                    URI.create(paymentGatewayUrl),
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return "success".equals(root.path("status").asText()) &&
                        root.path("data").path("authorization").asBoolean(false);
            }

        } catch (Exception e) {
            System.err.println("Failed to authorize transaction: " + e.getMessage());
        }

        return false;
    }
}
