package tr.com.eaaslan.library.model.dto.borrowing;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BorrowingReturnRequest(
        LocalDate returnDate
) {
}
