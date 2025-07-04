package dev.jvops.bank.user.service;

import dev.jvops.bank.dto.UserStoreDTO;
import dev.jvops.bank.user.model.User;
import dev.jvops.bank.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

class UserServiceTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void testCreateUser() {
        var dto = new UserStoreDTO();
        dto.setName("João");
        dto.setCpf("44472225560");
        dto.setEmail("joao@email.com");
        dto.setPassword("Senha123");
        dto.setPhoneNumber("11999999999");

        Mockito.when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var created = userService.createUser(dto);

        assertNotNull(created);
        assertTrue(passwordEncoder.matches("Senha123", created.getPassword()));
    }

    @Test
    void testGetUserById_Success() {
        var mockUser = setupMockUser(1L, null);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        var found = userService.getUserById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("João", found.getName());
    }

    @Test
    void testGetUserById_NotFound() {
        Mockito.when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(99L));
    }

    @Test
    void testGetUserById_Deleted() {
        var deletedUser = setupMockUser(2L, LocalDateTime.now());
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(deletedUser));

        assertThrows(RuntimeException.class, () -> userService.getUserById(2L));
    }

    private User setupMockUser(Long id, LocalDateTime deletedAt) {
        return User.builder()
                .id(id)
                .name("João")
                .cpf("12345678901")
                .email("joao@email.com")
                .password(passwordEncoder.encode("Senha123"))
                .phoneNumber("11999999999")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(deletedAt)
                .build();
    }
}