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
import tr.com.eaaslan.library.exception.*;
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

    @Override
    @Transactional
    public BorrowingResponse borrowBook(BorrowingCreateRequest request, String currentUserEmail) {
        // Get the current user
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentUserEmail));

        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            if (currentUser.getStatus() == UserStatus.SUSPENDED) {
                throw new UserSuspendedException(currentUserEmail, currentUser.getSuspensionEndDate());
            }
            throw new InvalidUserStatusException(currentUserEmail, currentUser.getStatus().toString());
        }
        if (borrowingRepository.countByUserIdAndStatus(currentUser.getId(), BorrowingStatus.ACTIVE) >= 3) {
            throw new BorrowingLimitExceededException(currentUser.getMaxAllowedBorrows());
        }

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        // if user tried to book same book again it will throw first book is not avaliable but i want to borrow same book again exception
        if (book.isAvailable() && book.getQuantity() <= 0) {
            throw new BookNotAvailableException(book.getId());
        }

        if (borrowingRepository.existsByUserIdAndBookIdAndStatus(currentUser.getId(), request.bookId(), BorrowingStatus.ACTIVE)) {
            throw new AlreadyBorrowedException(request.bookId(), currentUserEmail);
        }

        if (request.dueDate().isBefore(LocalDate.now())) {
            throw new InvalidDueDateException(request.dueDate().toString());
        }

        Borrowing borrowing = Borrowing.builder()
                .user(currentUser)
                .book(book)
                .borrowDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(DEFAULT_BORROW_DAYS))
                .status(BorrowingStatus.ACTIVE)
                .build();

        // Save the borrowing record
        borrowing = borrowingRepository.save(borrowing);

        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);
        return borrowingMapper.toResponse(borrowing);


    }

    @Override
    @Transactional
    public BorrowingResponse returnBook(Long id, BorrowingReturnRequest request, String currentUserEmail) {
        // Get the current user
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentUserEmail));

        // Get the borrowing record
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing", "ID", id));

        // Check if the current user is authorized to return this book
        if (!borrowing.getUser().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != UserRole.LIBRARIAN &&
                currentUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("You can only return books borrowed by yourself");
        }

        // Check if the book is already returned
        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new BookAlreadyReturnedException(id);
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
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowDate").descending());
        return borrowingRepository.findByUserId(userId, pageable)
                .map(borrowingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowingResponse> getBorrowingsByCurrentUser(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        return getBorrowingsByUser(user.getId(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowingResponse> getBorrowingsByBook(Long bookId, int page, int size) {
        // Verify book exists
        bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "ID", bookId));

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
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record", "ID", id));
        log.info("Fetching borrowing with ID: {}", id);
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