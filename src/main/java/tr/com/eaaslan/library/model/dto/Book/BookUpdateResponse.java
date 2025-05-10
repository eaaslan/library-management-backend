package tr.com.eaaslan.library.model.dto.Book;

import java.time.LocalDateTime;

public record BookUpdateResponse(
        Long id,                // System-generated ID
        String isbn,
        String title,
        String author,
        int publicationYear,
        String publisher,
        String genre,
        String imageUrl,
        String description,
        int quantity,
        boolean available,      // Derived field
        LocalDateTime createdAt, // Audit information
        String createdBy,        // Audit information
        LocalDateTime updatedAt, // Audit information
        String updatedBy

) {
}
