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
    private final BookAvailabilityEventService eventService;

    private static final Logger log = LoggerFactory.getLogger(BorrowingServiceImpl.class);

    private static final int DEFAULT_BORROW_DAYS = 14; // Two weeks

    @Override
    @Transactional
    public BorrowingResponse borrowBook(BorrowingCreateRequest request, String currentUserEmail) {

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentUserEmail));

        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            if (currentUser.getStatus() == UserStatus.SUSPENDED) {
                throw new UserSuspendedException(currentUserEmail, currentUser.getSuspensionEndDate());
            }
            throw new InvalidUserStatusException(currentUserEmail, currentUser.getStatus().toString());
        }

        long activeBorrowings = borrowingRepository.countByUserIdAndStatus(currentUser.getId(), BorrowingStatus.ACTIVE);
        long overdueBorrowings = borrowingRepository.countByUserIdAndStatus(currentUser.getId(), BorrowingStatus.OVERDUE);
        long totalUnreturnedBorrowings = activeBorrowings + overdueBorrowings;

        if (totalUnreturnedBorrowings >= currentUser.getMaxAllowedBorrows()) {
            throw new BorrowingLimitExceededException(currentUser.getMaxAllowedBorrows());
        }

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "ID", request.bookId()));

        if (!book.isAvailable() || book.getQuantity() <= 0) {
            throw new BookNotAvailableException(book.getId());
        }

        if (borrowingRepository.existsByUserIdAndBookIdAndStatus(
                currentUser.getId(), request.bookId(), BorrowingStatus.ACTIVE)) {
            throw new AlreadyBorrowedException(request.bookId(), currentUserEmail);
        }

        LocalDate today = LocalDate.now();
        LocalDate dueDate;

        if (request.dueDate() != null) {
            dueDate = request.dueDate();

            if (dueDate.isBefore(today)) {
                throw new InvalidDueDateException("Due date cannot be in the past");
            }

            if (dueDate.isAfter(today.plusDays(30))) {
                throw new InvalidDueDateException("Due date cannot be more than 30 days from today");
            }
        } else {
            dueDate = today.plusDays(DEFAULT_BORROW_DAYS);
        }

        Borrowing borrowing = Borrowing.builder()
                .user(currentUser)
                .book(book)
                .borrowDate(today)
                .dueDate(dueDate)
                .status(BorrowingStatus.ACTIVE)
                .build();

        borrowing = borrowingRepository.save(borrowing);

        book.setQuantity(book.getQuantity() - 1);
        if (book.getQuantity() <= 0) {
            book.setAvailable(false);
        }

        bookRepository.save(book);

        eventService.publishBookAvailabilityChange(book);

        return borrowingMapper.toResponse(borrowing);
    }

    @Override
    @Transactional
    public BorrowingResponse returnBook(Long id, BorrowingReturnRequest request, String currentUserEmail) {

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentUserEmail));

        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing", "ID", id));

        if (!borrowing.getUser().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != UserRole.LIBRARIAN &&
                currentUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("You can only return books borrowed by yourself");
        }

        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new BookAlreadyReturnedException(id);
        }

        LocalDate returnDate = request.returnDate() != null ?
                request.returnDate() : LocalDate.now();

        borrowing.setReturnDate(returnDate);
        borrowing.setStatus(BorrowingStatus.RETURNED);

        if (returnDate.isAfter(borrowing.getDueDate())) {
            borrowing.setReturnedLate(true);
            log.info("Book returned late: {} days overdue",
                    returnDate.toEpochDay() - borrowing.getDueDate().toEpochDay());
        }

        Book book = borrowing.getBook();
        book.setQuantity(book.getQuantity() + 1);
        book.setAvailable(true);

        eventService.publishBookAvailabilityChange(book);
        bookRepository.save(book);

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

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> getAllBorrowingsForExport() {
        log.info("Fetching all borrowing records for export");

        // Newest first order
        Sort sort = Sort.by(Sort.Direction.DESC, "borrowDate", "id");
        List<Borrowing> borrowings = borrowingRepository.findAll(sort);

        log.info("Retrieved {} borrowing records for export", borrowings.size());

        return borrowings.stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> getBorrowingsByUserForExport(Long userId) {
        log.info("Fetching borrowing records for user {} for export", userId);

        List<Borrowing> borrowings = borrowingRepository.findByUserIdOrderByBorrowDateDesc(userId);

        log.info("Retrieved {} borrowing records for user {} for export", borrowings.size(), userId);

        return borrowings.stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> getOverdueBorrowingsForExport() {
        log.info("Fetching overdue borrowing records for export");

        List<Borrowing> overdueBorrowings = borrowingRepository.findByStatusOrderByDueDateAsc(BorrowingStatus.OVERDUE);

        log.info("Retrieved {} overdue borrowing records for export", overdueBorrowings.size());

        return overdueBorrowings.stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> getBorrowingsByDateRangeForExport(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching borrowing records by date range for export: {} to {}", startDate, endDate);

        List<Borrowing> borrowings;

        if (startDate == null && endDate == null) {
            // Tüm kayıtları getir
            borrowings = borrowingRepository.findAll(Sort.by(Sort.Direction.DESC, "borrowDate"));
        } else if (startDate == null) {
            // Sadece end date var
            borrowings = borrowingRepository.findByBorrowDateLessThanEqualOrderByBorrowDateDesc(endDate);
        } else if (endDate == null) {
            // Sadece start date var
            borrowings = borrowingRepository.findByBorrowDateGreaterThanEqualOrderByBorrowDateDesc(startDate);
        } else {
            // Her iki tarih de var
            borrowings = borrowingRepository.findByBorrowDateBetweenOrderByBorrowDateDesc(startDate, endDate);
        }

        log.info("Retrieved {} borrowing records for date range export", borrowings.size());

        return borrowings.stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> getBorrowingsByBookForExport(Long bookId) {
        log.info("Fetching borrowing history for book {} for export", bookId);

        List<Borrowing> borrowings = borrowingRepository.findByBookIdOrderByBorrowDateDesc(bookId);

        log.info("Retrieved {} borrowing records for book {} for export", borrowings.size(), bookId);

        return borrowings.stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }
}