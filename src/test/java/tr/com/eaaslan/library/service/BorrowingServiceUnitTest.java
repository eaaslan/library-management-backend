package tr.com.eaaslan.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceUnitTest {

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowingMapper borrowingMapper;

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    private User activeUser;
    private User suspendedUser;
    private Book availableBook;
    private Book unavailableBook;
    private Borrowing activeBorrowing;
    private Borrowing overdueBorrowing;
    private BorrowingResponse borrowingResponse;
    private BorrowingCreateRequest createRequest;
    private BorrowingReturnRequest returnRequest;
    private Page<Borrowing> borrowingsPage;

    @BeforeEach
    void setUp() {
        // Initialize test data
        activeUser = User.builder()
                .email("user@example.com")
                .status(UserStatus.ACTIVE)
                .role(UserRole.PATRON)
                .maxAllowedBorrows(3)
                .build();
        activeUser.setId(1L);

        suspendedUser = User.builder()
                .email("suspended@example.com")
                .status(UserStatus.SUSPENDED)
                .suspensionEndDate(LocalDate.now().plusDays(7))
                .role(UserRole.PATRON)
                .build();
        suspendedUser.setId(2L);

        availableBook = Book.builder()
                .title("Available Book")
                .isbn("1234567890")
                .available(true)
                .quantity(1)
                .build();
        availableBook.setId(1L);

        unavailableBook = Book.builder()
                .title("Unavailable Book")
                .isbn("0987654321")
                .available(false)
                .quantity(0)
                .build();
        unavailableBook.setId(2L);

        activeBorrowing = Borrowing.builder()
                .user(activeUser)
                .book(availableBook)
                .borrowDate(LocalDate.now().minusDays(7))
                .dueDate(LocalDate.now().plusDays(7))
                .status(BorrowingStatus.ACTIVE)
                .build();
        activeBorrowing.setId(1L);

        overdueBorrowing = Borrowing.builder()
                .user(activeUser)
                .book(unavailableBook)
                .borrowDate(LocalDate.now().minusDays(30))
                .dueDate(LocalDate.now().minusDays(1))
                .status(BorrowingStatus.OVERDUE)
                .build();
        overdueBorrowing.setId(2L);

        borrowingResponse = new BorrowingResponse(
                1L, 1L, "user@example.com", "Active User",
                1L, "Available Book", "1234567890",
                LocalDate.now(), LocalDate.now().plusDays(14), null,
                BorrowingStatus.ACTIVE.name(), false, LocalDateTime.now(), "system"
        );

        createRequest = new BorrowingCreateRequest(1L, null, null);
        returnRequest = new BorrowingReturnRequest(LocalDate.now());
        borrowingsPage = new PageImpl<>(List.of(activeBorrowing, overdueBorrowing));
    }

    @Test
    @DisplayName("Should create a new borrowing")
    void shouldCreateNewBorrowing() {
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(bookRepository.findById(availableBook.getId())).thenReturn(Optional.of(availableBook));
        when(borrowingRepository.countByUserIdAndStatus(activeUser.getId(), BorrowingStatus.ACTIVE)).thenReturn(0L);
        when(borrowingRepository.existsByUserIdAndBookIdAndStatus(
                activeUser.getId(), availableBook.getId(), BorrowingStatus.ACTIVE)).thenReturn(false);
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(i -> i.getArgument(0));
        when(borrowingMapper.toResponse(any(Borrowing.class))).thenReturn(borrowingResponse);

        BorrowingResponse response = borrowingService.borrowBook(createRequest, activeUser.getEmail());

        assertNotNull(response);
        assertEquals(borrowingResponse, response);

        verify(bookRepository).save(any(Book.class));
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    @DisplayName("Should throw exception when user has suspended status")
    void shouldThrowExceptionWhenUserHasSuspendedStatus() {
        when(userRepository.findByEmail(suspendedUser.getEmail())).thenReturn(Optional.of(suspendedUser));

        assertThrows(UserSuspendedException.class,
                () -> borrowingService.borrowBook(createRequest, suspendedUser.getEmail()));

        verify(bookRepository, never()).findById(any());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when book is unavailable")
    void shouldThrowExceptionWhenBookIsUnavailable() {
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(bookRepository.findById(unavailableBook.getId())).thenReturn(Optional.of(unavailableBook));
        when(borrowingRepository.countByUserIdAndStatus(activeUser.getId(), BorrowingStatus.ACTIVE)).thenReturn(0L);

        BorrowingCreateRequest unavailableBookRequest = new BorrowingCreateRequest(unavailableBook.getId(), null, null);

        assertThrows(BookNotAvailableException.class,
                () -> borrowingService.borrowBook(unavailableBookRequest, activeUser.getEmail()));

        verify(userRepository).findByEmail(any());
        verify(bookRepository).findById(any());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user already borrowed the book")
    void shouldThrowExceptionWhenUserAlreadyBorrowedTheBook() {
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(borrowingRepository.countByUserIdAndStatus(activeUser.getId(), BorrowingStatus.ACTIVE)).thenReturn(0L);
        when(bookRepository.findById(availableBook.getId())).thenReturn(Optional.of(availableBook));
        when(borrowingRepository.existsByUserIdAndBookIdAndStatus(
                activeUser.getId(), availableBook.getId(), BorrowingStatus.ACTIVE)).thenReturn(true);

        assertThrows(AlreadyBorrowedException.class,
                () -> borrowingService.borrowBook(createRequest, activeUser.getEmail()));

        verify(userRepository).findByEmail(any());
        verify(borrowingRepository).countByUserIdAndStatus(any(), any());
        verify(bookRepository).findById(any());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return a book successfully")
    void shouldReturnBookSuccessfully() {
        when(borrowingRepository.findById(activeBorrowing.getId())).thenReturn(Optional.of(activeBorrowing));
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(i -> i.getArgument(0));
        when(borrowingMapper.toResponse(any(Borrowing.class))).thenReturn(borrowingResponse);

        BorrowingResponse response = borrowingService.returnBook(activeBorrowing.getId(), returnRequest, activeUser.getEmail());

        assertNotNull(response);
        verify(bookRepository).save(any());
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    @DisplayName("Should throw exception when attempting to return someone else's book")
    void shouldThrowExceptionWhenAttemptingToReturnSomeoneElsesBook() {
        User otherUser = User.builder()
                .email("other@example.com")
                .status(UserStatus.ACTIVE)
                .role(UserRole.PATRON)
                .build();
        otherUser.setId(3L);

        when(borrowingRepository.findById(activeBorrowing.getId())).thenReturn(Optional.of(activeBorrowing));
        when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () ->
                borrowingService.returnBook(activeBorrowing.getId(), returnRequest, otherUser.getEmail()));

        verify(bookRepository, never()).save(any());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when book is already returned")
    void shouldThrowExceptionWhenBookIsAlreadyReturned() {
        Borrowing returnedBorrowing = Borrowing.builder()
                .user(activeUser)
                .book(availableBook)
                .borrowDate(LocalDate.now().minusDays(14))
                .dueDate(LocalDate.now().minusDays(7))
                .returnDate(LocalDate.now().minusDays(5))
                .status(BorrowingStatus.RETURNED)
                .build();
        returnedBorrowing.setId(3L);

        when(borrowingRepository.findById(returnedBorrowing.getId())).thenReturn(Optional.of(returnedBorrowing));
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));

        assertThrows(BookAlreadyReturnedException.class, () ->
                borrowingService.returnBook(returnedBorrowing.getId(), returnRequest, activeUser.getEmail()));

        verify(bookRepository, never()).save(any());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should mark book as returned late when returned after due date")
    void shouldMarkBookAsReturnedLateWhenReturnedAfterDueDate() {
        Borrowing lateBorrowing = Borrowing.builder()
                .user(activeUser)
                .book(availableBook)
                .borrowDate(LocalDate.now().minusDays(14))
                .dueDate(LocalDate.now().minusDays(1))
                .status(BorrowingStatus.ACTIVE)
                .build();
        lateBorrowing.setId(4L);

        when(borrowingRepository.findById(lateBorrowing.getId())).thenReturn(Optional.of(lateBorrowing));
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(i -> {
            Borrowing savedBorrowing = i.getArgument(0);
            assertTrue(savedBorrowing.isReturnedLate(), "Borrowing should be marked as returned late");
            return savedBorrowing;
        });
        when(borrowingMapper.toResponse(any(Borrowing.class))).thenReturn(borrowingResponse);

        borrowingService.returnBook(lateBorrowing.getId(), returnRequest, activeUser.getEmail());

        verify(bookRepository).save(any(Book.class));
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    @DisplayName("Should get all borrowings with pagination")
    void shouldGetAllBorrowingsWithPagination() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("borrowDate").descending());
        Page<Borrowing> borrowingsPage = new PageImpl<>(List.of(activeBorrowing));

        when(borrowingRepository.findAll(pageable)).thenReturn(borrowingsPage);
        when(borrowingMapper.toResponse(activeBorrowing)).thenReturn(borrowingResponse);

        Page<BorrowingResponse> response = borrowingService.getAllBorrowings(0, 10, "borrowDate");

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(borrowingResponse, response.getContent().get(0));
    }

    @Test
    @DisplayName("Should get borrowings by user")
    void shouldGetBorrowingsByUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> borrowingsPage = new PageImpl<>(List.of(activeBorrowing));

        when(userRepository.findById(activeUser.getId())).thenReturn(Optional.of(activeUser));
        when(borrowingRepository.findByUserId(eq(activeUser.getId()), any(Pageable.class)))
                .thenReturn(borrowingsPage);
        when(borrowingMapper.toResponse(activeBorrowing)).thenReturn(borrowingResponse);

        Page<BorrowingResponse> response = borrowingService.getBorrowingsByUser(activeUser.getId(), 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(borrowingResponse, response.getContent().get(0));
    }

    @Test
    @DisplayName("Should get borrowings by current user")
    void shouldGetBorrowingsByCurrentUser() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> borrowingsPage = new PageImpl<>(List.of(activeBorrowing));

        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(userRepository.findById(activeUser.getId())).thenReturn(Optional.of(activeUser));
        when(borrowingRepository.findByUserId(eq(activeUser.getId()), any(Pageable.class)))
                .thenReturn(borrowingsPage);
        when(borrowingMapper.toResponse(activeBorrowing)).thenReturn(borrowingResponse);

        // Act
        Page<BorrowingResponse> response = borrowingService.getBorrowingsByCurrentUser(activeUser.getEmail(), 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(borrowingResponse, response.getContent().getFirst());

        // Verify
        verify(userRepository).findByEmail(activeUser.getEmail());
        verify(borrowingRepository).findByUserId(eq(activeUser.getId()), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get borrowings by book")
    void shouldGetBorrowingsByBook() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> borrowingsPage = new PageImpl<>(List.of(activeBorrowing));

        when(bookRepository.findById(availableBook.getId())).thenReturn(Optional.of(availableBook));
        when(borrowingRepository.findByBookId(eq(availableBook.getId()), any(Pageable.class)))
                .thenReturn(borrowingsPage);
        when(borrowingMapper.toResponse(activeBorrowing)).thenReturn(borrowingResponse);

        // Act
        Page<BorrowingResponse> response = borrowingService.getBorrowingsByBook(availableBook.getId(), 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(borrowingResponse, response.getContent().get(0));

        // Verify
        verify(bookRepository).findById(availableBook.getId());
        verify(borrowingRepository).findByBookId(eq(availableBook.getId()), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get overdue borrowings")
    void shouldGetOverdueBorrowings() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> borrowingsPage = new PageImpl<>(List.of(overdueBorrowing));

        when(borrowingRepository.findOverdueBorrowings(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(borrowingsPage);
        when(borrowingMapper.toResponse(overdueBorrowing)).thenReturn(borrowingResponse);

        // Act
        Page<BorrowingResponse> response = borrowingService.getOverdueBorrowings(0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(borrowingResponse, response.getContent().get(0));

        // Verify
        verify(borrowingRepository).findOverdueBorrowings(any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get borrowing by id")
    void shouldGetBorrowingById() {
        // Arrange
        when(borrowingRepository.findById(activeBorrowing.getId())).thenReturn(Optional.of(activeBorrowing));
        when(borrowingMapper.toResponse(activeBorrowing)).thenReturn(borrowingResponse);

        // Act
        BorrowingResponse response = borrowingService.getBorrowingById(activeBorrowing.getId());

        // Assert
        assertNotNull(response);
        assertEquals(borrowingResponse.id(), response.id());
        assertEquals(borrowingResponse.bookTitle(), response.bookTitle());
        assertEquals(borrowingResponse.userEmail(), response.userEmail());

        // Verify
        verify(borrowingRepository).findById(activeBorrowing.getId());
        verify(borrowingMapper).toResponse(activeBorrowing);
    }

    @Test
    @DisplayName("Should throw exception when borrowing not found by id")
    void shouldThrowExceptionWhenBorrowingNotFoundById() {
        // Arrange
        Long nonExistentId = 999L;
        when(borrowingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> borrowingService.getBorrowingById(nonExistentId));

        // Verify
        verify(borrowingRepository).findById(nonExistentId);
        verify(borrowingMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should update overdue status for borrowings")
    void shouldUpdateOverdueStatusForBorrowings() {
        // Arrange
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        LocalDate today = LocalDate.now();

        Borrowing overdueBorrowing = Borrowing.builder()
                .user(activeUser)
                .book(availableBook)
                .borrowDate(today.minusDays(30))
                .dueDate(today.minusDays(1))
                .status(BorrowingStatus.ACTIVE)
                .build();
        overdueBorrowing.setId(4L);

        Page<Borrowing> activeBorrowingsPage = new PageImpl<>(List.of(overdueBorrowing));

        when(borrowingRepository.findByStatus(eq(BorrowingStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(activeBorrowingsPage);
        when(borrowingRepository.saveAll(anyList())).thenReturn(List.of(overdueBorrowing));

        // Act
        borrowingService.updateOverdueStatus();

        // Assert - We can verify that the saveAll method was called
        verify(borrowingRepository).findByStatus(eq(BorrowingStatus.ACTIVE), any(Pageable.class));
        verify(borrowingRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when user has exceeded borrowing limit")
    void shouldThrowExceptionWhenUserHasExceededBorrowingLimit() {
        // Arrange
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(borrowingRepository.countByUserIdAndStatus(activeUser.getId(), BorrowingStatus.ACTIVE))
                .thenReturn((long) activeUser.getMaxAllowedBorrows());

        // Act & Assert
        assertThrows(BorrowingLimitExceededException.class,
                () -> borrowingService.borrowBook(createRequest, activeUser.getEmail()));

        // Verify
        verify(userRepository).findByEmail(activeUser.getEmail());
        verify(borrowingRepository).countByUserIdAndStatus(activeUser.getId(), BorrowingStatus.ACTIVE);
        verify(bookRepository, never()).findById(any());
    }
}