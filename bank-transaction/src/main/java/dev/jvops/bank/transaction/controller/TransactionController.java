package dev.jvops.bank.transaction.controller;

import dev.jvops.bank.transaction.dto.TransactionRequestDTO;
import dev.jvops.bank.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionRequestDTO dto) {
        transactionService.transfer(dto);
        return ResponseEntity.ok().build(); // 200 OK sem body
    }
}
