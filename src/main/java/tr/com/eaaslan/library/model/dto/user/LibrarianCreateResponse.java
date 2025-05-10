package tr.com.eaaslan.library.model.dto.user;

import java.time.LocalDateTime;

public record LibrarianCreateResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String role,
        LocalDateTime createdAt,
        String createdBy
) {
}