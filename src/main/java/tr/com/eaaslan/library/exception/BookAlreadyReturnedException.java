package tr.com.eaaslan.library.exception;


public class BookAlreadyReturnedException extends BusinessRuleException {
    public BookAlreadyReturnedException(Long borrowingId) {
        super(String.format("Borrowing record with ID '%d' is already returned", borrowingId));
    }
}