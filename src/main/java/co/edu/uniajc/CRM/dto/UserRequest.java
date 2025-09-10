package co.edu.uniajc.CRM.dto;

import co.edu.uniajc.CRM.model.User;
import jakarta.validation.constraints.*;

public record UserRequest(
        @NotBlank @Size(max = 30) String name,
        @NotBlank @Email String email,
        @NotNull User.Role role,
        @NotBlank @Size(min = 8, max = 16) String password
) {}
