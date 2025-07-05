package dev.jvops.bank.transaction.controller;

import dev.jvops.bank.transaction.dto.TransactionRequestDTO;
import dev.jvops.bank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody TransactionRequestDTO dto) {
        try {
            transactionService.transfer(dto);
            return ResponseEntity.ok().build(); // 200 OK sem body
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        } catch (IllegalStateException e) {
            return ResponseEntity.status(502).body(e.getMessage()); // 502 Bad Gateway
        }
    }
}
