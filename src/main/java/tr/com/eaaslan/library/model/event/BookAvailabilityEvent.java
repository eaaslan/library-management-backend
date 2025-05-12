package tr.com.eaaslan.library.model.event;

import java.time.LocalDateTime;

public record BookAvailabilityEvent(
        Long bookId,
        String bookTitle,
        boolean available,
        int quantity,
        LocalDateTime timestamp
) {
}