package tr.com.eaaslan.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import tr.com.eaaslan.library.model.*;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingCreateRequest;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingResponse;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingReturnRequest;
import tr.com.eaaslan.library.model.mapper.BorrowingMapper;
import tr.com.eaaslan.library.repository.BookRepository;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

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
    private BorrowingResponse borrowingResponse;
    private BorrowingCreateRequest createRequest;
    private BorrowingReturnRequest returnRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
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

        borrowingResponse = new BorrowingResponse(
                1L, 1L, "user@example.com", "User Name",
                1L, "Available Book", "1234567890",
                LocalDate.now(), LocalDate.now().plusDays(14), null,
                BorrowingStatus.ACTIVE.name(), false, null, null
        );

        createRequest = new BorrowingCreateRequest(1L, null, null);
        returnRequest = new BorrowingReturnRequest(LocalDate.now());
    }

    @Test
    @DisplayName("Should borrow a book successfully")
    void shouldBorrowBookSuccessfully() {
        // Arrange
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(bookRepository.findById(availableBook.getId())).thenReturn(Optional.of(availableBook));
        when(borrowingRepository.countByUserIdAndStatus(activeUser.getId(), BorrowingStatus.ACTIVE)).thenReturn(0L);
        when(borrowingRepository.existsByUserIdAndBookIdAndStatus(
                activeUser.getId(), availableBook.getId(), BorrowingStatus.ACTIVE)).thenReturn(false);
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(i -> i.getArgument(0));
        when(borrowingMapper.toResponse(any(Borrowing.class))).thenReturn(borrowingResponse);

        // Act
        BorrowingResponse response = borrowingService.borrowBook(createRequest, activeUser.getEmail());

        // Assert
        assertNotNull(response);
        assertEquals(borrowingResponse.id(), response.id());
        assertEquals(borrowingResponse.userEmail(), response.userEmail());
        assertEquals(borrowingResponse.bookTitle(), response.bookTitle());

        verify(bookRepository).save(any(Book.class));
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    @DisplayName("Should throw exception when user is suspended")
    void shouldThrowExceptionWhenUserIsSuspended() {
        // Arrange
        when(userRepository.findByEmail(suspendedUser.getEmail())).thenReturn(Optional.of(suspendedUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                borrowingService.borrowBook(createRequest, suspendedUser.getEmail()));

        verify(bookRepository, never()).findById(any());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user has reached borrow limit")
    void shouldThrowExceptionWhenUserHasReachedBorrowLimit() {
        // Arrange
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(borrowingRepository.countByUserIdAndStatus(activeUser.getId(), BorrowingStatus.ACTIVE)).thenReturn(3L);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () ->
                borrowingService.borrowBook(createRequest, activeUser.getEmail()));
        assertEquals("You cannot borrow more than 3 books at the same time.", exception.getMessage());

        verify(bookRepository, never()).findById(any());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when book is not available")
    void shouldThrowExceptionWhenBookIsNotAvailable() {
        // Arrange
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(borrowingRepository.countByUserIdAndStatus(activeUser.getId(), BorrowingStatus.ACTIVE)).thenReturn(0L);
        when(bookRepository.findById(unavailableBook.getId())).thenReturn(Optional.of(unavailableBook));

        // Create request for unavailable book
        BorrowingCreateRequest unavailableRequest = new BorrowingCreateRequest(unavailableBook.getId(), null, null);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () ->
                borrowingService.borrowBook(unavailableRequest, activeUser.getEmail()));
        assertEquals("Book is not available", exception.getMessage());

        verify(borrowingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return a book successfully")
    void shouldReturnBookSuccessfully() {
        // Arrange
        when(borrowingRepository.findById(activeBorrowing.getId())).thenReturn(Optional.of(activeBorrowing));
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(i -> i.getArgument(0));
        when(borrowingMapper.toResponse(any(Borrowing.class))).thenReturn(borrowingResponse);

        // Act
        BorrowingResponse response = borrowingService.returnBook(activeBorrowing.getId(), returnRequest, activeUser.getEmail());

        // Assert
        assertNotNull(response);
        verify(bookRepository).save(any(Book.class));
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    @DisplayName("Should throw exception when returning someone else's book")
    void shouldThrowExceptionWhenReturningOthersBook() {
        // Arrange
        User otherUser = User.builder()
                .email("other@example.com")
                .status(UserStatus.ACTIVE)
                .role(UserRole.PATRON)
                .build();
        otherUser.setId(2L);

        when(borrowingRepository.findById(activeBorrowing.getId())).thenReturn(Optional.of(activeBorrowing));
        when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                borrowingService.returnBook(activeBorrowing.getId(), returnRequest, otherUser.getEmail()));

        verify(bookRepository, never()).save(any());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get all borrowings")
    void shouldGetAllBorrowings() {
        // Arrange
        Page<Borrowing> borrowingsPage = new PageImpl<>(List.of(activeBorrowing));
        when(borrowingRepository.findAll(any(Pageable.class))).thenReturn(borrowingsPage);
        when(borrowingMapper.toResponse(activeBorrowing)).thenReturn(borrowingResponse);

        // Act
        Page<BorrowingResponse> response = borrowingService.getAllBorrowings(0, 10, "borrowDate");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(borrowingResponse, response.getContent().get(0));
    }

    @Test
    @DisplayName("Should get borrowings by user")
    void shouldGetBorrowingsByUser() {
        // Arrange
        Page<Borrowing> borrowingsPage = new PageImpl<>(List.of(activeBorrowing));
        when(userRepository.findById(activeUser.getId())).thenReturn(Optional.of(activeUser));
        when(borrowingRepository.findByUserId(eq(activeUser.getId()), any(Pageable.class))).thenReturn(borrowingsPage);
        when(borrowingMapper.toResponse(activeBorrowing)).thenReturn(borrowingResponse);

        // Act
        Page<BorrowingResponse> response = borrowingService.getBorrowingsByUser(activeUser.getId(), 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(borrowingResponse, response.getContent().get(0));
    }
}