package dev.jvops.bank.transaction.service;

import dev.jvops.bank.api.external.notificationgateway.NotificationGatewayClient;
import dev.jvops.bank.api.external.paymentgateway.PaymentGatewayClient;
import dev.jvops.bank.common.AppLogger;
import dev.jvops.bank.transaction.dto.TransactionRequestDTO;
import dev.jvops.bank.transaction.model.Transaction;
import dev.jvops.bank.transaction.repository.TransactionRepository;
import dev.jvops.bank.transaction.service.exception.InsufficientBalanceException;
import dev.jvops.bank.transaction.service.exception.TransactionUnauthorizedException;
import dev.jvops.bank.wallet.model.Wallet;
import dev.jvops.bank.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final PaymentGatewayClient paymentGatewayClient;
    private final NotificationGatewayClient notificationGatewayClient;

    @Transactional
    public Transaction transfer(TransactionRequestDTO dto){

        Wallet fromWallet = walletService.getWalletById(dto.getOriginWalletId());
        Wallet toWallet = walletService.getWalletById(dto.getTargetWalletId());

        BigDecimal amount = dto.getAmount();

        if (fromWallet.getAmount().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        fromWallet.setAmount(fromWallet.getAmount().subtract(amount));
        toWallet.setAmount(toWallet.getAmount().add(amount));

        Transaction transaction = Transaction.builder()
                .originWallet(fromWallet)
                .targetWallet(toWallet)
                .amount(amount)
                .build();

        // Verifica autorização externa
        boolean authorized = paymentGatewayClient.authorizeTransaction();
        if (!authorized) {
            throw new TransactionUnauthorizedException();
        }

        boolean notify = notificationGatewayClient.NotifyTransaction();
        if (!notify) {
            AppLogger.warn(TransactionService.class, "Transaction completed but notification failed");
        }

        return transactionRepository.save(transaction);

    }
}
