package dev.jvops.bank.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jvops.bank.account.model.Account;
import dev.jvops.bank.account.model.enums.AccountType;
import dev.jvops.bank.account.repository.AccountRepository;
import dev.jvops.bank.api.external.notificationgateway.NotificationGatewayClient;
import dev.jvops.bank.api.external.paymentgateway.PaymentGatewayClient;
import dev.jvops.bank.api.external.paymentgateway.PaymentGatewayResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
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
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0) // inicia WireMock em porta aleatória
@ActiveProfiles("test")
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

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private Wallet originWallet;
    private Wallet targetWallet;

    private void mockAuthorization(Boolean authorization) {
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "status": "success",
                                "data": {
                                    "authorization": %s
                                }
                            }
                            """.formatted(authorization))
                )
        );
    }

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(
                User.builder()
                        .name("João da Silva")
                        .cpf("12345678901")
                        .email("joao@email.com")
                        .password("senha123")
                        .phoneNumber("11999999999")
                        .build()
        );

        Account originAccount = accountRepository.save(Account.builder()
                .document("12345678901")
                .type(AccountType.PF)
                .user(user)
                .build());

        originWallet = walletRepository.save(Wallet.builder()
                .amount(new BigDecimal("100.00"))
                .account(originAccount)
                .build());

        Account targetAccount = accountRepository.save(Account.builder()
                .document("09876543210")
                .type(AccountType.PF)
                .user(user)
                .build());

        targetWallet = walletRepository.save(Wallet.builder()
                .amount(new BigDecimal("50.00"))
                .account(targetAccount)
                .build());
    }


    @Test
    void testTransferSuccess() throws Exception {
        mockAuthorization(true);
        TransactionRequestDTO dto = buildTransactionDTO("40.00");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Wallet updatedOrigin = walletRepository.findById(originWallet.getId()).orElseThrow();
        Wallet updatedTarget = walletRepository.findById(targetWallet.getId()).orElseThrow();

        assert updatedOrigin.getAmount().compareTo(new BigDecimal("60.00")) == 0;
        assert updatedTarget.getAmount().compareTo(new BigDecimal("90.00")) == 0;
    }

    @Test
    void testTransferFailsWhenUnauthorized() throws Exception {
        mockAuthorization(false);
        TransactionRequestDTO dto = buildTransactionDTO("40.00");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testTransferFailsWhenInsufficientBalance() throws Exception {
        TransactionRequestDTO dto = buildTransactionDTO("1000.00");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    private PaymentGatewayResponse buildPaymentGatewayResponse(boolean authorized) {
        var data = new PaymentGatewayResponse.DataResponse();
        data.setAuthorization(authorized);

        var response = new PaymentGatewayResponse();
        response.setStatus("success");
        response.setData(data);

        return response;
    }

    private TransactionRequestDTO buildTransactionDTO(String amount) {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setOriginWalletId(originWallet.getId());
        dto.setTargetWalletId(targetWallet.getId());
        dto.setAmount(new BigDecimal(amount));
        return dto;
    }
}
