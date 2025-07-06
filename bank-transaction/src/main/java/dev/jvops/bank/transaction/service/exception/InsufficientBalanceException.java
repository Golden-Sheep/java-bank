package dev.jvops.bank.transaction.service.exception;

public class InsufficientBalanceException extends RuntimeException {
    public static final String DEFAULT_MESSAGE = "Insufficient balance to complete the transaction";
    public InsufficientBalanceException() {
        super(DEFAULT_MESSAGE);
    }
}