package tr.com.eaaslan.library.exception;

public class InvalidDueDateException extends BusinessRuleException {
    public InvalidDueDateException(String message) {
        super(String.format("Invalid due date: %s", message));
    }
}
