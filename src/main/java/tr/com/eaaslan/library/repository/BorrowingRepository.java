package tr.com.eaaslan.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.eaaslan.library.model.Borrowing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import tr.com.eaaslan.library.model.BorrowingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    // Find by user ID
    Page<Borrowing> findByUserId(Long userId, Pageable pageable);

    // Find by book ID
    Page<Borrowing> findByBookId(Long bookId, Pageable pageable);

    // Find active borrowings (not returned)
    Page<Borrowing> findByStatus(BorrowingStatus status, Pageable pageable);

    // Find by user ID and status
    Page<Borrowing> findByUserIdAndStatus(Long userId, BorrowingStatus status, Pageable pageable);

    // Find overdue borrowings
    @Query("SELECT b FROM Borrowing b WHERE b.status = 'ACTIVE' AND b.dueDate < :currentDate")
    Page<Borrowing> findOverdueBorrowings(LocalDateTime currentDate, Pageable pageable);

    // Check if a user has already borrowed a specific book
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowingStatus status);

    // Count active borrowings for a user
    long countByUserIdAndStatus(Long userId, BorrowingStatus status);

    // Find by book ID and status
    List<Borrowing> findByBookIdAndStatus(Long bookId, BorrowingStatus status);
}