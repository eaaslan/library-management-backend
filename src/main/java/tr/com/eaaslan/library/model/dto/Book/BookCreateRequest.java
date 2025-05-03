package tr.com.eaaslan.library.model.dto.Book;

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

    //todo check these later.
//    // Compact constructor that provides defaults
//    public BookCreateRequest {
//        if (quantity <= 0) {
//            quantity = 1;  // Default quantity if invalid value provided
//        }
//    }
//
//    // Static factory method that doesn't require quantity
//    public static BookCreateRequest create(
//            String isbn, String title, String author, int publicationYear,
//            String publisher, String genre, String imageUrl, String description) {
//        return new BookCreateRequest(isbn, title, author, publicationYear,
//                publisher, genre, imageUrl, description, 1);
//    }
}