package tr.com.eaaslan.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.com.eaaslan.library.model.*;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingCreateRequest;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingResponse;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingReturnRequest;
import tr.com.eaaslan.library.model.mapper.BorrowingMapper;
import tr.com.eaaslan.library.repository.BookRepository;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor

public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowingMapper borrowingMapper;

    private static final Logger log = LoggerFactory.getLogger(BorrowingServiceImpl.class);

    private static final int DEFAULT_BORROW_DAYS = 14; // Two weeks

    //todo create exception for trying to borrow same book and exceed borrow limit
    //todo check whether use localdatetime or localdate and on response dates does not set
    @Override
    @Transactional
    public BorrowingResponse borrowBook(Long id, BorrowingCreateRequest request, String currentUserEmail) {
        // Get the current user
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("User is not active");
        }
        // Get the borrowing record
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrowing record not found"));
        // Get the user who will borrow the book (could be the current user or another user if librarian/admin)

        if (!borrowing.getUser().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != UserRole.LIBRARIAN &&
                currentUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("You can only return books borrowed by yourself");
        }

        // Check if the book is already returned
        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new RuntimeException("Book is already returned");
        }
        // Update borrowing status
        LocalDate returnDate = request.returnDate() != null ?
                request.returnDate() : LocalDate.now();

        borrowing.setReturnDate(returnDate);
        borrowing.setStatus(BorrowingStatus.RETURNED);

        // Check if the book is returned late and set the flag
        if (returnDate.isAfter(borrowing.getDueDate())) {
            borrowing.setReturnedLate(true);
            log.info("Book returned late: {} days overdue",
                    returnDate.toEpochDay() - borrowing.getDueDate().toEpochDay());
        }


        // Update book availability
        Book book = borrowing.getBook();
        book.setQuantity(book.getQuantity() + 1);
        book.setAvailable(true);
        bookRepository.save(book);

        // Save the updated borrowing record
        Borrowing updatedBorrowing = borrowingRepository.save(borrowing);
        log.info("Book returned: {}, by user: {}, returnedLate: {}",
                book.getTitle(), borrowing.getUser().getEmail(), borrowing.isReturnedLate());

        return borrowingMapper.toResponse(updatedBorrowing);
    }

    @Override
    @Transactional
    public BorrowingResponse returnBook(Long id, BorrowingReturnRequest request, String currentUserEmail) {
        // Get the current user
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Get the borrowing record
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrowing record not found"));

        // Check if the current user is authorized to return this book
        if (!borrowing.getUser().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != UserRole.LIBRARIAN &&
                currentUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("You can only return books borrowed by yourself");
        }

        // Check if the book is already returned
        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new RuntimeException("Book is already returned");
        }

        // Update borrowing status
        LocalDate returnDate = request.returnDate() != null ?
                request.returnDate() : LocalDate.now();

        borrowing.setReturnDate(returnDate);
        borrowing.setStatus(BorrowingStatus.RETURNED);

        // Check if the book is returned late and set the flag
        if (returnDate.isAfter(borrowing.getDueDate())) {
            borrowing.setReturnedLate(true);
            log.info("Book returned late: {} days overdue",
                    returnDate.toEpochDay() - borrowing.getDueDate().toEpochDay());
        }

        // Update book availability
        Book book = borrowing.getBook();
        book.setQuantity(book.getQuantity() + 1);
        book.setAvailable(true);
        bookRepository.save(book);

        // Save the updated borrowing record
        Borrowing updatedBorrowing = borrowingRepository.save(borrowing);
        log.info("Book returned: {}, by user: {}", book.getTitle(), borrowing.getUser().getEmail());

        return borrowingMapper.toResponse(updatedBorrowing);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowingResponse> getAllBorrowings(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return borrowingRepository.findAll(pageable)
                .map(borrowingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowingResponse> getBorrowingsByUser(Long userId, int page, int size) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowDate").descending());
        return borrowingRepository.findByUserId(userId, pageable)
                .map(borrowingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowingResponse> getBorrowingsByCurrentUser(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return getBorrowingsByUser(user.getId(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowingResponse> getBorrowingsByBook(Long bookId, int page, int size) {
        // Verify book exists
        bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowDate").descending());
        return borrowingRepository.findByBookId(bookId, pageable)
                .map(borrowingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowingResponse> getOverdueBorrowings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        return borrowingRepository.findOverdueBorrowings(LocalDateTime.now(), pageable)
                .map(borrowingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BorrowingResponse getBorrowingById(Long id) {
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrowing record not found"));

        return borrowingMapper.toResponse(borrowing);
    }

    @Transactional
    public void updateOverdueStatus() {
        // Update overdue status for active borrowings
        LocalDateTime now = LocalDateTime.now();
        Page<Borrowing> activeBorrowings = borrowingRepository.findByStatus(
                BorrowingStatus.ACTIVE,
                PageRequest.of(0, Integer.MAX_VALUE));

        List<Borrowing> overdueBorrowings = activeBorrowings.getContent().stream()
                .filter(b -> b.getDueDate().isBefore(ChronoLocalDate.from(now)))
                .peek(b -> b.setStatus(BorrowingStatus.OVERDUE))
                .toList();

        if (!overdueBorrowings.isEmpty()) {
            borrowingRepository.saveAll(overdueBorrowings);
            log.info("Updated status for {} overdue borrowings", overdueBorrowings.size());
        }
    }
}