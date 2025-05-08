package tr.com.eaaslan.library.exception;

import org.springframework.http.HttpStatus;

public class BorrowingLimitExceededException extends BusinessRuleException {
    public BorrowingLimitExceededException(int maxAllowedBorrows) {
        super(String.format("You cannot borrow more than %d books at the same time", maxAllowedBorrows));
    }
}