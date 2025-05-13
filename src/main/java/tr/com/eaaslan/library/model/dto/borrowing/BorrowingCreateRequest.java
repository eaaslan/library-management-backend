package tr.com.eaaslan.library.model.dto.borrowing;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record BorrowingCreateRequest(

        @NotNull(message = "Book ID is required")
        @Positive(message = "Book ID must be positive")
        Long bookId,
        
        LocalDate dueDate,

        LocalDate returnDate
) {
}
