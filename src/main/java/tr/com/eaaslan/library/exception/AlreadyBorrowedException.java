package tr.com.eaaslan.library.exception;

import org.springframework.http.HttpStatus;

public class AlreadyBorrowedException extends BusinessRuleException {
    public AlreadyBorrowedException(Long bookId, String email) {
        super(String.format("User '%s' has already borrowed book with ID '%d'", email, bookId));
    }
}