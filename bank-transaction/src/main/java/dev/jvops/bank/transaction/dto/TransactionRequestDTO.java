package dev.jvops.bank.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequestDTO {

    @NotNull(message = "Origin wallet ID must not be null")
    private Long originWalletId;

    @NotNull(message = "Target wallet ID must not be null")
    private Long targetWalletId;

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;
}
