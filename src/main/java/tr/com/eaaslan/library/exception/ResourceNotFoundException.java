package tr.com.eaaslan.library.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends LibraryException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue), HttpStatus.NOT_FOUND);
    }
}
