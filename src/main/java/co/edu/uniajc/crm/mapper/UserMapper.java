package co.edu.uniajc.crm.mapper;

import co.edu.uniajc.crm.dto.*;
import co.edu.uniajc.crm.model.User;

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
