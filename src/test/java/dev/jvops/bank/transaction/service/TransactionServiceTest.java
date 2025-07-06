package dev.jvops.bank.transaction.service;

import dev.jvops.bank.api.external.notificationgateway.NotificationGatewayClient;
import dev.jvops.bank.api.external.paymentgateway.PaymentGatewayClient;
import dev.jvops.bank.api.external.paymentgateway.PaymentGatewayResponse;
import dev.jvops.bank.transaction.dto.TransactionRequestDTO;
import dev.jvops.bank.transaction.model.Transaction;
import dev.jvops.bank.transaction.repository.TransactionRepository;
import dev.jvops.bank.transaction.service.exception.InsufficientBalanceException;
import dev.jvops.bank.transaction.service.exception.TransactionUnauthorizedException;
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
    private PaymentGatewayClient paymentGatewayClient;
    private NotificationGatewayClient notificationGatewayClient;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        walletService = mock(WalletService.class);
        transactionRepository = mock(TransactionRepository.class);
        paymentGatewayClient = mock(PaymentGatewayClient.class);
        notificationGatewayClient = mock(NotificationGatewayClient.class);

        transactionService = new TransactionService(
                walletService,
                transactionRepository,
                paymentGatewayClient,
                notificationGatewayClient
        );
    }

    @Test
    void testTransfer_SuccessfulTransaction() {
        Long originWalletId = 1L;
        Long targetWalletId = 2L;
        BigDecimal amount = new BigDecimal("50.00");

        Wallet originWallet = buildWallet(originWalletId, new BigDecimal("100.00"));
        Wallet targetWallet = buildWallet(targetWalletId, new BigDecimal("20.00"));

        TransactionRequestDTO dto = buildDTO(originWalletId, targetWalletId, amount);

        when(walletService.getWalletById(originWalletId)).thenReturn(originWallet);
        when(walletService.getWalletById(targetWalletId)).thenReturn(targetWallet);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentGatewayClient.authorizeTransaction()).thenReturn(buildPaymentGatewayResponse(true));
        doNothing().when(notificationGatewayClient).notifyTransaction();

        Transaction transaction = transactionService.transfer(dto);

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
        Wallet originWallet = buildWallet(1L, new BigDecimal("10.00"));
        Wallet targetWallet = buildWallet(2L, new BigDecimal("100.00"));

        TransactionRequestDTO dto = buildDTO(1L, 2L, new BigDecimal("50.00"));

        when(walletService.getWalletById(1L)).thenReturn(originWallet);
        when(walletService.getWalletById(2L)).thenReturn(targetWallet);

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.transfer(dto);
        });

        assertEquals(InsufficientBalanceException.DEFAULT_MESSAGE, exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testTransfer_FailsWhenNotAuthorized() {
        Wallet originWallet = buildWallet(1L, new BigDecimal("100.00"));
        Wallet targetWallet = buildWallet(2L, new BigDecimal("0.00"));

        TransactionRequestDTO dto = buildDTO(1L, 2L, new BigDecimal("50.00"));

        when(walletService.getWalletById(1L)).thenReturn(originWallet);
        when(walletService.getWalletById(2L)).thenReturn(targetWallet);
        when(paymentGatewayClient.authorizeTransaction()).thenReturn(buildPaymentGatewayResponse(false));

        TransactionUnauthorizedException exception = assertThrows(TransactionUnauthorizedException.class, () -> {
            transactionService.transfer(dto);
        });

        assertEquals(TransactionUnauthorizedException.DEFAULT_MESSAGE, exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    // Helper: cria Wallet
    private Wallet buildWallet(Long id, BigDecimal amount) {
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setAmount(amount);
        return wallet;
    }

    // Helper: cria DTO de transação
    private TransactionRequestDTO buildDTO(Long originId, Long targetId, BigDecimal amount) {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setOriginWalletId(originId);
        dto.setTargetWalletId(targetId);
        dto.setAmount(amount);
        return dto;
    }

    // Helper: cria mock da resposta do Feign
    private PaymentGatewayResponse buildPaymentGatewayResponse(boolean authorized) {
        PaymentGatewayResponse.DataResponse data = new PaymentGatewayResponse.DataResponse();
        data.setAuthorization(authorized);

        PaymentGatewayResponse response = new PaymentGatewayResponse();
        response.setStatus("success");
        response.setData(data);

        return response;
    }
}
