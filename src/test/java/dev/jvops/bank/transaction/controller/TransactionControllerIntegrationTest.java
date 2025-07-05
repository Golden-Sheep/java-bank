package dev.jvops.bank.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jvops.bank.account.model.Account;
import dev.jvops.bank.account.model.enums.AccountType;
import dev.jvops.bank.account.repository.AccountRepository;
import dev.jvops.bank.api.external.notificationgateway.NotificationGatewayClient;
import dev.jvops.bank.api.external.paymentgateway.PaymentGatewayClient;
import dev.jvops.bank.config.TestMockConfig;
import dev.jvops.bank.transaction.dto.TransactionRequestDTO;
import dev.jvops.bank.transaction.repository.TransactionRepository;
import dev.jvops.bank.user.model.User;
import dev.jvops.bank.user.repository.UserRepository;
import dev.jvops.bank.wallet.model.Wallet;
import dev.jvops.bank.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMockConfig.class)
public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PaymentGatewayClient paymentGatewayClient;

    @Autowired
    private NotificationGatewayClient notificationGatewayClient;

    @Autowired
    private ObjectMapper objectMapper;

    private Wallet originWallet;
    private Wallet targetWallet;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Criar o usuário
        User user = User.builder()
                .name("João da Silva")
                .cpf("12345678901")
                .email("joao@email.com")
                .password("senha123")
                .phoneNumber("11999999999")
                .build();
        user = userRepository.save(user);

        // Criar conta de origem e carteira associada
        Account originAccount = Account.builder()
                .document("12345678901")
                .type(AccountType.PF)
                .user(user)
                .build();

        Wallet originWallet = Wallet.builder()
                .amount(new BigDecimal("100.00"))
                .account(originAccount)
                .build();
        originAccount.setWallet(originWallet);

        originAccount = accountRepository.save(originAccount);

        // Criar conta de destino e carteira associada
        Account targetAccount = Account.builder()
                .document("09876543210")
                .type(AccountType.PF)
                .user(user)
                .build();

        Wallet targetWallet = Wallet.builder()
                .amount(new BigDecimal("50.00"))
                .account(targetAccount)
                .build();
        targetAccount.setWallet(targetWallet);

        targetAccount = accountRepository.save(targetAccount);

        // Atribuir às variáveis de teste
        this.originWallet = originAccount.getWallet();
        this.targetWallet = targetAccount.getWallet();

        // Mocks dos gateways
        Mockito.when(paymentGatewayClient.authorizeTransaction()).thenReturn(true);
        Mockito.when(notificationGatewayClient.NotifyTransaction()).thenReturn(true);
    }

    @Test
    void testTransferSuccess() throws Exception {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setOriginWalletId(originWallet.getId());
        dto.setTargetWalletId(targetWallet.getId());
        dto.setAmount(new BigDecimal("40.00"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Wallet updatedOrigin = walletRepository.findById(originWallet.getId()).get();
        Wallet updatedTarget = walletRepository.findById(targetWallet.getId()).get();

        assert updatedOrigin.getAmount().compareTo(new BigDecimal("60.00")) == 0;
        assert updatedTarget.getAmount().compareTo(new BigDecimal("90.00")) == 0;
    }

    @Test
    void testTransferFailsWhenUnauthorized() throws Exception {
        Mockito.when(paymentGatewayClient.authorizeTransaction()).thenReturn(false);

        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setOriginWalletId(originWallet.getId());
        dto.setTargetWalletId(targetWallet.getId());
        dto.setAmount(new BigDecimal("40.00"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadGateway()) // agora espera 502
                .andExpect(content().string("Transaction not authorized by external gateway"));
    }


    @Test
    void testTransferFailsWhenInsufficientBalance() throws Exception {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setOriginWalletId(originWallet.getId());
        dto.setTargetWalletId(targetWallet.getId());
        dto.setAmount(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
