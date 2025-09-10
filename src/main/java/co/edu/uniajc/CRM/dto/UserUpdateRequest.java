package co.edu.uniajc.CRM.dto;

import co.edu.uniajc.CRM.model.User;
import jakarta.validation.constraints.*;

public record UserUpdateRequest(
        @NotBlank @Size(max = 30) String name,
        @NotNull User.Role role,
        // contraseña opcional en update; si viene, se valida
        @Size(min = 8, max = 16) String password
) {}
