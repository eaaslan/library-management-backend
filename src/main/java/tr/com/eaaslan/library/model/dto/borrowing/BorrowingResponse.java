package tr.com.eaaslan.library.model.dto.borrowing;

import java.time.LocalDateTime;

public record BorrowingResponse(
        Long id,
        Long userId,
        String userEmail,
        String userName,
        Long bookId,
        String bookTitle,
        String bookIsbn,
        LocalDateTime borrowDate,
        LocalDateTime dueDate,
        LocalDateTime returnDate,
        String status,
        boolean isOverdue,
        LocalDateTime createdAt,
        String createdBy
) {
}