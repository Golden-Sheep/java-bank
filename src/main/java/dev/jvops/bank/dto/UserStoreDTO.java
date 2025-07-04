package dev.jvops.bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserStoreDTO {
    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 11, max = 11, message = "CPF must be exactly 11 digits")
    @Pattern(regexp = "\\d{11}", message = "CPF must contain only numbers")
    private String cpf;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 32, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
            message = "Password must contain at least one number, one lowercase, one uppercase letter and be at least 8 characters"
    )
    private String password;

    @NotBlank
    private String phoneNumber;
}
