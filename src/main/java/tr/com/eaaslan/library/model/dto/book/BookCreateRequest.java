package tr.com.eaaslan.library.model.dto.book;

public record BookCreateRequest(
        String isbn,
        String title,
        String author,
        int publicationYear,
        String publisher,
        String genre,
        String imageUrl,
        String description,
        int quantity
) {
}