package tr.com.eaaslan.library.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;

public class UserSuspendedException extends BusinessRuleException {
    public UserSuspendedException(String email, LocalDate suspensionEndDate) {
        super(String.format("User with email '%s' is suspended until %s. You cannot borrow books at this time",
                email, suspensionEndDate));
    }
}