package dev.jvops.bank.user.service;

import dev.jvops.bank.user.dto.UserStoreDTO;
import dev.jvops.bank.user.model.User;
import dev.jvops.bank.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User createUser(UserStoreDTO storeDTO) {
        String encryptedPassword = passwordEncoder.encode(storeDTO.getPassword());

        User user = User.builder()
                .name(storeDTO.getName())
                .cpf(storeDTO.getCpf())
                .email(storeDTO.getEmail())
                .password(encryptedPassword)
                .phoneNumber(storeDTO.getPhoneNumber())
                .build();

        return userRepository.save(user);
    }

    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("User not found or has been deleted."));
    }
}
