package tr.com.eaaslan.library.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistException extends LibraryException {
    public ResourceAlreadyExistException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue), HttpStatus.CONFLICT);
    }
}
