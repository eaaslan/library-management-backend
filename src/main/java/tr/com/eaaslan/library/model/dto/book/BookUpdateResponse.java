package tr.com.eaaslan.library.model.dto.book;

import java.time.LocalDateTime;

public record BookUpdateResponse(
        Long id,
        String isbn,
        String title,
        String author,
        int publicationYear,
        String publisher,
        String genre,
        String imageUrl,
        String description,
        int quantity,
        boolean available,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy

) {
}
