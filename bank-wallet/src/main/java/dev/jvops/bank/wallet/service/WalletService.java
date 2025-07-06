package dev.jvops.bank.wallet.service;

import dev.jvops.bank.user.model.User;
import dev.jvops.bank.wallet.model.Wallet;
import dev.jvops.bank.wallet.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id)
                .filter(wallet -> wallet.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found or has been deleted."));
    }
}
