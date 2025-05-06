package tr.com.eaaslan.library.model.dto.borrowing;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BorrowingResponse(
        Long id,
        Long userId,
        String userEmail,
        String userName,
        Long bookId,
        String bookTitle,
        String bookIsbn,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        String status,
        boolean returnedLate,
        LocalDateTime createdAt,
        String createdBy
) {
}