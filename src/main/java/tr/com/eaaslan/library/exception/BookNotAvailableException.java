package tr.com.eaaslan.library.exception;


public class BookNotAvailableException extends BusinessRuleException {
    public BookNotAvailableException(Long bookId) {
        super(String.format("Book with ID '%d' is not available for borrowing", bookId));
    }

    public BookNotAvailableException(String title) {
        super(String.format("Book '%s' is not available for borrowing", title));
    }
}