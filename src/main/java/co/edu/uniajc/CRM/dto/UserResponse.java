package co.edu.uniajc.CRM.dto;

import co.edu.uniajc.CRM.model.User;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        User.Role role,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
