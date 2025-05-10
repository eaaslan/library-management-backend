package tr.com.eaaslan.library.model.dto.user;

import java.time.LocalDateTime;

public record UserUpdateResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String role,
        String status,
        int maxAllowedBorrows,
        LocalDateTime createdAt,
        String createdBy,
        boolean deleted,
        LocalDateTime updatedAt,
        String updatedBy
) {
}