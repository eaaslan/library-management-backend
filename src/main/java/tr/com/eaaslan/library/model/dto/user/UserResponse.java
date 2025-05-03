package tr.com.eaaslan.library.model.dto.user;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String address,
        String role,
        boolean active,
        int borrowLimit,
        LocalDateTime createdAt,
        String createdBy
) {
}