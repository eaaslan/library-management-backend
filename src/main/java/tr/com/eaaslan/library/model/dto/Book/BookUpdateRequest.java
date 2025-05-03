package tr.com.eaaslan.library.model.dto.Book;

public record BookUpdateRequest(

        String isbn,
        String title,
        String author,
        Integer publicationYear,
        String publisher,
        String genre,
        String imageUrl,
        String description,
        Integer quantity,
        Boolean available
) {
}