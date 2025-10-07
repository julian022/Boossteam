package co.edu.uniajc.crm.dto;

import co.edu.uniajc.crm.model.User;
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
