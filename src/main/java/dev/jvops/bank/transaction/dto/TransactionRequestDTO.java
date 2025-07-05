package dev.jvops.bank.transaction.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequestDTO {
    private Long originWalletId;
    private Long targetWalletId;
    private BigDecimal amount;
}
