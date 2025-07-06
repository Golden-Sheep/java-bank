package dev.jvops.bank.transaction.service.exception;

public class TransactionUnauthorizedException extends RuntimeException {
    public static final String DEFAULT_MESSAGE = "Transaction was not authorized by external gateway";
    public TransactionUnauthorizedException() {
        super(DEFAULT_MESSAGE);
    }
}
