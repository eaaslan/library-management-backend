package tr.com.eaaslan.library.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Table
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Borrowing extends BaseEntity {


    @Column(name = "borrow_date", nullable = false)
    @NotNull(message = "Borrow date is required")
    private LocalDate borrowDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "due_date")
    @NotNull(message = "Due date cannot be null")
    //@FutureOrPresent(message = "Due date must be today or in the future")
    private LocalDate dueDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BorrowingStatus status;

    @Column(name = "returned_late")
    @Builder.Default
    private boolean returnedLate = false;

    @Transient
    public boolean isOverdue() {
        return status == BorrowingStatus.ACTIVE &&
                dueDate != null &&
                dueDate.isBefore(LocalDate.now());
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @AssertTrue(message = "Return date must be after borrow date")
    private boolean isReturnDateValid() {
        if (returnDate == null) {
            return true;
        }
        return returnDate.isAfter(borrowDate) || returnDate.isEqual(borrowDate);
    }

    @AssertTrue(message = "Due date must be after or equal to borrow date")
    private boolean isDueDateValid() {
        if (dueDate == null || borrowDate == null) {
            return true; // Will be caught by @NotNull if needed
        }
        return dueDate.isAfter(borrowDate) || dueDate.isEqual(borrowDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Borrowing borrowing)) return false;
        return user != null && book != null && borrowDate != null &&
                user.equals(borrowing.user) && book.equals(borrowing.book) && borrowDate.equals(borrowing.borrowDate);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() + (user != null ? user.hashCode() : 0) + (book != null ? book.hashCode() : 0) + (borrowDate != null ? borrowDate.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "Borrowing{" +
                "user=" + (user != null ? user.getEmail() : "null") +
                ", book=" + (book != null ? book.getTitle() : "null") +
                ", borrowingDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status=" + status +
                ", returnedLate=" + returnedLate +
                '}';
    }

}
