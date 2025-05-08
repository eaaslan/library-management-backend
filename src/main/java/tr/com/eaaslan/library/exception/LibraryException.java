package tr.com.eaaslan.library.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LibraryException extends RuntimeException {

    private final HttpStatus httpStatus;

    public LibraryException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
