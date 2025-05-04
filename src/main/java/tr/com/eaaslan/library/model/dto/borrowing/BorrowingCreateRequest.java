package tr.com.eaaslan.library.model.dto.borrowing;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BorrowingCreateRequest(

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be positive")
        Long userId,

        @NotNull(message = "Book ID is required")
        @Positive(message = "Book ID must be positive")
        Long bookId,

        LocalDate dueDate
) {
}
