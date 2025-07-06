package dev.jvops.bank.api.external.paymentgateway;

import lombok.Data;

@Data
public class PaymentGatewayResponse {
    private String status;
    private DataResponse data;

    @Data
    public static class DataResponse {
        private boolean authorization;
    }
}
