package tr.com.eaaslan.library.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends LibraryException {
    public BusinessRuleException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
