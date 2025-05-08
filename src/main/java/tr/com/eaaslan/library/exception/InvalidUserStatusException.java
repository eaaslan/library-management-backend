package tr.com.eaaslan.library.exception;


public class InvalidUserStatusException extends BusinessRuleException {
    public InvalidUserStatusException(String email, String status) {
        super(String.format("User with email '%s' is not active. Current status: %s", email, status));
    }
}