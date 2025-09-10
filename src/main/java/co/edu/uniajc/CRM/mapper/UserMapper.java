package co.edu.uniajc.CRM.mapper;

import co.edu.uniajc.CRM.dto.*;
import co.edu.uniajc.CRM.model.User;

public final class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(), u.getName(), u.getEmail(),
                u.getRole(), u.getActive(),
                u.getCreatedAt(), u.getUpdatedAt()
        );
    }
}
