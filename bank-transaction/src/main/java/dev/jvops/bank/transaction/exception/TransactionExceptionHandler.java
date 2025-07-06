package dev.jvops.bank.transaction.exception;

import dev.jvops.bank.transaction.service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "dev.jvops.bank.transaction")
public class TransactionExceptionHandler {

    @ExceptionHandler({
            InsufficientBalanceException.class
    })
    public ResponseEntity<ErrorResponse> handleTransactionErrors(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(TransactionUnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(TransactionUnauthorizedException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), // 403
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    private record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
}