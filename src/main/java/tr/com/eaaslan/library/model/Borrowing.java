package tr.com.eaaslan.library.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
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
    private LocalDate borrowDate;

    @Column(name = "return_date")
    //@Future(message = "Return date must be in the future")
    private LocalDate returnDate;

    @Column(name = "due_date")
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

    @ManyToOne
    private Book book;

    @ManyToOne
    private User user;


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
