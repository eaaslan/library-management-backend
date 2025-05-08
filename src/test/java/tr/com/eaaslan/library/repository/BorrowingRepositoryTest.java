package tr.com.eaaslan.library.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tr.com.eaaslan.library.config.TestJpaConfig;
import tr.com.eaaslan.library.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import(TestJpaConfig.class)
public class BorrowingRepositoryTest {

    @Container
    static PostgreSQLContainer container = new PostgreSQLContainer("postgres:15");

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private User activeUser;
    private User secondUser;
    private Book firstBook;
    private Book secondBook;
    private Borrowing activeBorrowing;
    private Borrowing overdueBorrowing;
    private Borrowing returnedBorrowing;
    private Borrowing returnedLateBorrowing;


    @BeforeEach
    void setUp() {
        borrowingRepository.deleteAll();
        userRepository.deleteAll();
        bookRepository.deleteAll();

        activeUser = User.builder()
                .email("active@library.com")
                .password("password")
                .firstName("Active")
                .lastName("User")
                .phoneNumber("05501234567")
                .role(UserRole.PATRON)
                .status(UserStatus.ACTIVE)
                .build();

        secondUser = User.builder()
                .email("second@library.com")
                .password("password")
                .firstName("Second")
                .lastName("User")
                .phoneNumber("05501234568")
                .role(UserRole.PATRON)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(activeUser);
        userRepository.save(secondUser);

        firstBook = Book.builder()
                .isbn("1234567890")
                .title("First Book")
                .author("First Author")
                .publicationYear(Year.of(2020))
                .publisher("Test Publisher")
                .genre(Genre.FICTION)
                .description("Test description")
                .available(true)
                .quantity(1)
                .build();

        secondBook = Book.builder()
                .isbn("1234567891")
                .title("Second Book")
                .author("Second Author")
                .publicationYear(Year.of(2020))
                .publisher("Test Publisher")
                .genre(Genre.FICTION)
                .description("Test description")
                .available(true)
                .quantity(1)
                .build();

        bookRepository.save(firstBook);
        bookRepository.save(secondBook);

        LocalDate today = LocalDate.now();

        activeBorrowing = Borrowing.builder()
                .user(activeUser)
                .book(firstBook)
                .borrowDate(today.minusDays(5))
                .dueDate(today.plusDays(14))
                .status(BorrowingStatus.ACTIVE)
                .build();

        overdueBorrowing = Borrowing.builder()
                .user(activeUser)
                .book(secondBook)
                .borrowDate(today.minusDays(15))
                .dueDate(today.minusDays(1))
                .status(BorrowingStatus.OVERDUE)
                .build();

        returnedBorrowing = Borrowing.builder()
                .user(secondUser)
                .book(firstBook)
                .borrowDate(today.minusDays(20))
                .dueDate(today.minusDays(6))
                .returnDate(today.minusDays(8))
                .status(BorrowingStatus.RETURNED)
                .returnedLate(false)
                .build();

        returnedLateBorrowing = Borrowing.builder()
                .user(secondUser)
                .book(secondBook)
                .borrowDate(today.minusDays(30))
                .dueDate(today.minusDays(16))
                .returnDate(today.minusDays(10))
                .status(BorrowingStatus.RETURNED)
                .returnedLate(true)
                .build();

        borrowingRepository.save(activeBorrowing);
        borrowingRepository.save(overdueBorrowing);
        borrowingRepository.save(returnedBorrowing);
        borrowingRepository.save(returnedLateBorrowing);

    }

    @Test
    @DisplayName("Should connect to database")
    void testConnection() {
        assertTrue(container.isRunning());
    }

    @Test
    @DisplayName("Should find all borrowings")
    void shouldFindAllBorrowings() {
        List<Borrowing> borrowings = borrowingRepository.findAll();
        assertEquals(4, borrowings.size());
    }

    @Test
    @DisplayName("Should find all active borrowings")
    void shouldFindBorrowingsByUserId() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Borrowing> activeUserBorrowings = borrowingRepository.findByUserId(activeUser.getId(), pageable);
        Page<Borrowing> secondUserBorrowings = borrowingRepository.findByUserId(secondUser.getId(), pageable);

        assertEquals(2, activeUserBorrowings.getTotalElements());
        assertEquals(2, secondUserBorrowings.getTotalElements());
    }

    @Test
    @DisplayName("Should find borrowings by book ID")
    void shouldFindBorrowingsByBookId() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Borrowing> firstBookBorrowings = borrowingRepository.findByBookId(firstBook.getId(), pageable);
        Page<Borrowing> secondBookBorrowings = borrowingRepository.findByBookId(secondBook.getId(), pageable);

        assertEquals(2, firstBookBorrowings.getTotalElements(), "Should find 2 borrowings for first book");
        assertEquals(2, secondBookBorrowings.getTotalElements(), "Should find 2 borrowings for second book");
    }

    @Test
    @DisplayName("Should find borrowings by status")
    void shouldFindBorrowingsByStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Borrowing> activeBorrowings = borrowingRepository.findByStatus(BorrowingStatus.ACTIVE, pageable);
        Page<Borrowing> overdueBorrowings = borrowingRepository.findByStatus(BorrowingStatus.OVERDUE, pageable);
        Page<Borrowing> returnedBorrowings = borrowingRepository.findByStatus(BorrowingStatus.RETURNED, pageable);

        assertEquals(1, activeBorrowings.getTotalElements(), "Should find 1 active borrowing");
        assertEquals(1, overdueBorrowings.getTotalElements(), "Should find 1 overdue borrowing");
        assertEquals(2, returnedBorrowings.getTotalElements(), "Should find 2 returned borrowings");
    }

    @Test
    @DisplayName("Should find borrowings by user ID and status")
    void shouldFindBorrowingsByUserIdAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Borrowing> activeUserActiveBorrowings = borrowingRepository.findByUserIdAndStatus(
                activeUser.getId(), BorrowingStatus.ACTIVE, pageable);

        Page<Borrowing> secondUserReturnedBorrowings = borrowingRepository.findByUserIdAndStatus(
                secondUser.getId(), BorrowingStatus.RETURNED, pageable);

        assertEquals(1, activeUserActiveBorrowings.getTotalElements(),
                "Should find 1 active borrowing for active user");
        assertEquals(2, secondUserReturnedBorrowings.getTotalElements(),
                "Should find 2 returned borrowings for second user");
    }

    @Test
    @DisplayName("Should count late returns by user")
    void shouldCountLateReturnsByUser() {
        LocalDate startDate = LocalDate.now().minusDays(31);
        LocalDate endDate = LocalDate.now();

        long secondUserLateReturns = borrowingRepository.countByUserIdAndReturnedLateAndReturnDateBetween(
                secondUser.getId(), true, startDate, endDate);

        long activeUserLateReturns = borrowingRepository.countByUserIdAndReturnedLateAndReturnDateBetween(
                activeUser.getId(), true, startDate, endDate);

        assertEquals(1, secondUserLateReturns, "Second user should have 1 late return");
        assertEquals(0, activeUserLateReturns, "Active user should have 0 late returns");
    }

    @Test
    @DisplayName("Should find latest activity date by user ID")
    void shouldFindLatestActivityDateByUserId() {
        LocalDate latestActiveUserActivity = borrowingRepository.findLatestActivityDateByUserId(activeUser.getId());
        LocalDate latestSecondUserActivity = borrowingRepository.findLatestActivityDateByUserId(secondUser.getId());

        // The latest activity for active user is the borrow date of activeBorrowing (today - 5 days)
        assertEquals(LocalDate.now().minusDays(5), latestActiveUserActivity);

        // The latest activity for second user is the borrow date of returnedBorrowing (today - 20 days)
        assertEquals(LocalDate.now().minusDays(20), latestSecondUserActivity);
    }

    @Test
    @DisplayName("Should find overdue borrowings")
    void shouldFindOverdueBorrowings() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Borrowing> overdueBorrowings = borrowingRepository.findOverdueBorrowings(
                LocalDateTime.now(), pageable);

        assertEquals(1, overdueBorrowings.getTotalElements(), "Should find 1 overdue borrowing");
        assertEquals(BorrowingStatus.OVERDUE, overdueBorrowings.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("Should check if user has already borrowed a specific book")
    void shouldCheckIfUserHasAlreadyBorrowedSpecificBook() {
        boolean activeUserHasFirstBook = borrowingRepository.existsByUserIdAndBookIdAndStatus(
                activeUser.getId(), firstBook.getId(), BorrowingStatus.ACTIVE);

        boolean activeUserHasSecondBook = borrowingRepository.existsByUserIdAndBookIdAndStatus(
                activeUser.getId(), secondBook.getId(), BorrowingStatus.ACTIVE);

        boolean secondUserHasAnyActiveBook = borrowingRepository.existsByUserIdAndBookIdAndStatus(
                secondUser.getId(), firstBook.getId(), BorrowingStatus.ACTIVE);

        assertTrue(activeUserHasFirstBook, "Active user should have first book as active borrowing");
        assertFalse(activeUserHasSecondBook, "Active user has second book but as OVERDUE, not ACTIVE");
        assertFalse(secondUserHasAnyActiveBook, "Second user has no active borrowings");
    }

    @Test
    @DisplayName("Should count active borrowings for a user")
    void shouldCountActiveBorrowingsForUser() {
        long activeUserActiveBorrowings = borrowingRepository.countByUserIdAndStatus(
                activeUser.getId(), BorrowingStatus.ACTIVE);

        long secondUserActiveBorrowings = borrowingRepository.countByUserIdAndStatus(
                secondUser.getId(), BorrowingStatus.ACTIVE);

        assertEquals(1, activeUserActiveBorrowings, "Active user should have 1 active borrowing");
        assertEquals(0, secondUserActiveBorrowings, "Second user should have 0 active borrowings");
    }

    @Test
    @DisplayName("Should find borrowings by book ID and status")
    void shouldFindBorrowingsByBookIdAndStatus() {
        List<Borrowing> firstBookActive = borrowingRepository.findByBookIdAndStatus(
                firstBook.getId(), BorrowingStatus.ACTIVE);

        List<Borrowing> secondBookReturned = borrowingRepository.findByBookIdAndStatus(
                secondBook.getId(), BorrowingStatus.RETURNED);

        assertEquals(1, firstBookActive.size(), "Should find 1 active borrowing for first book");
        assertEquals(1, secondBookReturned.size(), "Should find 1 returned borrowing for second book");
    }

    @Test
    @DisplayName("Should sort borrowings by borrow date")
    void shouldSortBorrowingsByBorrowDate() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("borrowDate").descending());
        Page<Borrowing> borrowings = borrowingRepository.findAll(pageable);

        // The most recent borrowing should be the first in the list
        assertEquals(activeBorrowing.getId(), borrowings.getContent().get(0).getId(),
                "Most recent borrowing should be first when sorted by borrowDate descending");
    }

    @Test
    @DisplayName("Should save and retrieve a borrowing correctly")
    void shouldSaveAndRetrieveBorrowingCorrectly() {
        // Create a new borrowing
        LocalDate today = LocalDate.now();
        Borrowing newBorrowing = Borrowing.builder()
                .user(activeUser)
                .book(secondBook)
                .borrowDate(today)
                .dueDate(today.plusDays(14))
                .status(BorrowingStatus.ACTIVE)
                .build();

        // Save the borrowing
        Borrowing savedBorrowing = borrowingRepository.save(newBorrowing);

        // Check the saved borrowing has an ID
        assertNotNull(savedBorrowing.getId(), "Saved borrowing should have an ID");

        // Retrieve the borrowing and verify its properties
        Borrowing retrievedBorrowing = borrowingRepository.findById(savedBorrowing.getId()).orElse(null);
        assertNotNull(retrievedBorrowing, "Should be able to retrieve the saved borrowing");
        assertEquals(activeUser.getId(), retrievedBorrowing.getUser().getId(), "User should match");
        assertEquals(secondBook.getId(), retrievedBorrowing.getBook().getId(), "Book should match");
        assertEquals(today, retrievedBorrowing.getBorrowDate(), "Borrow date should match");
        assertEquals(today.plusDays(14), retrievedBorrowing.getDueDate(), "Due date should match");
        assertEquals(BorrowingStatus.ACTIVE, retrievedBorrowing.getStatus(), "Status should match");
    }
}
