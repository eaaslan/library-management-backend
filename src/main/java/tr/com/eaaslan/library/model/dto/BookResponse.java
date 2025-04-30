package tr.com.eaaslan.library.model.dto;

import java.time.LocalDateTime;

public record BookResponse(
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
        String createdBy        // Audit information
) {
}
