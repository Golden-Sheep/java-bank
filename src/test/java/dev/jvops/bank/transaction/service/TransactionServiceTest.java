package dev.jvops.bank.transaction.service;

import dev.jvops.bank.api.external.notificationgateway.NotificationGatewayClient;
import dev.jvops.bank.api.external.paymentgateway.PaymentGatewayClient;
import dev.jvops.bank.transaction.dto.TransactionRequestDTO;
import dev.jvops.bank.transaction.model.Transaction;
import dev.jvops.bank.transaction.repository.TransactionRepository;
import dev.jvops.bank.wallet.model.Wallet;
import dev.jvops.bank.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private WalletService walletService;
    private TransactionRepository transactionRepository;
    private TransactionService transactionService;
    private PaymentGatewayClient paymentGatewayClient;
    private NotificationGatewayClient notificationGatewayClient;

    @BeforeEach
    void setUp() {
        walletService = mock(WalletService.class);
        transactionRepository = mock(TransactionRepository.class);
        paymentGatewayClient = mock(PaymentGatewayClient.class);
        notificationGatewayClient = mock(NotificationGatewayClient.class);
        transactionService = new TransactionService(walletService, transactionRepository, paymentGatewayClient, notificationGatewayClient);
    }

    @Test
    void testTransfer_SuccessfulTransaction() {
        // Arrange
        Long originWalletId = 1L;
        Long targetWalletId = 2L;
        BigDecimal amount = new BigDecimal("50.00");

        Wallet originWallet = new Wallet();
        originWallet.setId(originWalletId);
        originWallet.setAmount(new BigDecimal("100.00"));

        Wallet targetWallet = new Wallet();
        targetWallet.setId(targetWalletId);
        targetWallet.setAmount(new BigDecimal("20.00"));

        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setOriginWalletId(originWalletId);
        dto.setTargetWalletId(targetWalletId);
        dto.setAmount(amount);

        when(walletService.getWalletById(originWalletId)).thenReturn(originWallet);
        when(walletService.getWalletById(targetWalletId)).thenReturn(targetWallet);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentGatewayClient.authorizeTransaction()).thenReturn(true); // mock autorização
        when(notificationGatewayClient.NotifyTransaction()).thenReturn(true); // mock notificação

        // Act
        Transaction transaction = transactionService.transfer(dto);

        // Assert
        assertNotNull(transaction);
        assertEquals(originWallet, transaction.getOriginWallet());
        assertEquals(targetWallet, transaction.getTargetWallet());
        assertEquals(amount, transaction.getAmount());
        assertEquals(new BigDecimal("50.00"), originWallet.getAmount());
        assertEquals(new BigDecimal("70.00"), targetWallet.getAmount());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testTransfer_InsufficientBalance() {
        // Arrange
        Wallet originWallet = new Wallet();
        originWallet.setId(1L);
        originWallet.setAmount(new BigDecimal("10.00"));

        Wallet targetWallet = new Wallet();
        targetWallet.setId(2L);
        targetWallet.setAmount(new BigDecimal("100.00"));

        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setOriginWalletId(1L);
        dto.setTargetWalletId(2L);
        dto.setAmount(new BigDecimal("50.00"));

        when(walletService.getWalletById(1L)).thenReturn(originWallet);
        when(walletService.getWalletById(2L)).thenReturn(targetWallet);

        // Act + Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transfer(dto);
        });

        assertEquals("Insufficient balance", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }
}
