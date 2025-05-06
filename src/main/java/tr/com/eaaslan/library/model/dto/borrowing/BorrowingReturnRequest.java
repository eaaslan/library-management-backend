package tr.com.eaaslan.library.model.dto.borrowing;

import java.time.LocalDate;


public record BorrowingReturnRequest(
        LocalDate returnDate
) {
}
