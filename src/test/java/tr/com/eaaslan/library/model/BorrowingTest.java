package tr.com.eaaslan.library.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BorrowingTest {

    private User testUser;
    private Book testBook;
    private static Validator validator;

    @BeforeAll
    static void setUpClass() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("05501234567")
                .role(UserRole.PATRON)
                .status(UserStatus.ACTIVE)
                .build();

        // Set up test book
        testBook = Book.builder()
                .isbn("1234567890")
                .title("Test Book")
                .author("Test Author")
                .publicationYear(java.time.Year.of(2020))
                .publisher("Test Publisher")
                .genre(Genre.FICTION)
                .available(true)
                .quantity(1)
                .build();
    }

    @Test
    @DisplayName("Should create a valid borrowing")
    void shouldCreateValidBorrowing() {

        LocalDate borrowDate = LocalDate.now().minusDays(7);
        LocalDate dueDate = borrowDate.plusDays(14);

        Borrowing borrowing = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(BorrowingStatus.ACTIVE)
                .build();

        assertNotNull(borrowing);
        assertEquals(testUser, borrowing.getUser());
        assertEquals(testBook, borrowing.getBook());
        assertEquals(borrowDate, borrowing.getBorrowDate());
        assertEquals(dueDate, borrowing.getDueDate());
        assertEquals(BorrowingStatus.ACTIVE, borrowing.getStatus());
        assertFalse(borrowing.isReturnedLate());

        Set<ConstraintViolation<Borrowing>> violations = validator.validate(borrowing);
        assertTrue(violations.isEmpty(), "Borrowing should be valid");
    }

    @Test
    @DisplayName("Should identify overdue active borrowings correctly")
    void shouldIdentifyOverdueActiveBorrowingsCorrectly() {
        // Arrange
        LocalDate pastDueDate = LocalDate.now().minusDays(1);
        Borrowing overdueBorrowing = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(LocalDate.now().minusDays(15))
                .dueDate(pastDueDate)
                .status(BorrowingStatus.ACTIVE)
                .build();

        Borrowing notOverdueBorrowing = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(5))  // Future date
                .status(BorrowingStatus.ACTIVE)
                .build();

        Borrowing returnedBorrowing = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(LocalDate.now().minusDays(15))
                .dueDate(pastDueDate)  // Past date, but already returned
                .returnDate(LocalDate.now().minusDays(2))
                .status(BorrowingStatus.RETURNED)
                .build();

        // Act & Assert
        assertTrue(overdueBorrowing.isOverdue(),
                "Borrowing should be identified as overdue when due date is in the past and status is ACTIVE");

        assertFalse(notOverdueBorrowing.isOverdue(),
                "Borrowing should not be identified as overdue when due date is in the future");

        assertFalse(returnedBorrowing.isOverdue(),
                "Borrowing should not be identified as overdue when already returned, even if due date is in the past");
    }

    @Test
    @DisplayName("Should identify non-overdue active borrowings correctly")
    void shouldIdentifyNonOverdueActiveBorrowingsCorrectly() {

        LocalDate futureDueDate = LocalDate.now().plusDays(1);
        Borrowing borrowing = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(LocalDate.now().minusDays(5))
                .dueDate(futureDueDate)
                .status(BorrowingStatus.ACTIVE)
                .build();


        assertFalse(borrowing.isOverdue(), "Borrowing should not be overdue when due date is in the future");
    }

    @Test
    @DisplayName("Should not identify returned borrowings as overdue")
    void shouldNotIdentifyReturnedBorrowingsAsOverdue() {

        LocalDate pastDueDate = LocalDate.now().minusDays(1);
        Borrowing borrowing = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(LocalDate.now().minusDays(15))
                .dueDate(pastDueDate)
                .returnDate(LocalDate.now())
                .status(BorrowingStatus.RETURNED)
                .build();

        assertFalse(borrowing.isOverdue(), "Returned borrowings should not be overdue even if due date is in the past");
    }

    @Test
    @DisplayName("Should mark as returned late correctly")
    void shouldMarkAsReturnedLateCorrectly() {

        LocalDate pastDueDate = LocalDate.now().minusDays(5);
        LocalDate lateReturnDate = LocalDate.now().minusDays(2);

        Borrowing borrowing = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(LocalDate.now().minusDays(20))
                .dueDate(pastDueDate)
                .returnDate(lateReturnDate)
                .status(BorrowingStatus.RETURNED)
                .returnedLate(true)
                .build();

        assertTrue(borrowing.isReturnedLate(), "Borrowing should be marked as returned late");
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {

        LocalDate borrowDate = LocalDate.now().minusDays(7);
        LocalDate dueDate = borrowDate.plusDays(14);

        Borrowing borrowing1 = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(BorrowingStatus.ACTIVE)
                .build();

        Borrowing borrowing2 = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(BorrowingStatus.ACTIVE)
                .build();

        Borrowing differentBorrowing = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(LocalDate.now().minusDays(8))
                .dueDate(dueDate)
                .status(BorrowingStatus.ACTIVE)
                .build();

        assertEquals(borrowing1, borrowing2, "Same user, book, and borrowDate should be equal");
        assertNotEquals(borrowing1, differentBorrowing, "Different borrowDate should not be equal");
        assertEquals(borrowing1.hashCode(), borrowing2.hashCode(), "Equal objects should have same hashCode");
        assertNotEquals(borrowing1.hashCode(), differentBorrowing.hashCode(), "Different objects should have different hashCode");
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {

        LocalDate borrowDate = LocalDate.now().minusDays(7);
        LocalDate dueDate = borrowDate.plusDays(14);

        Borrowing borrowing = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(BorrowingStatus.ACTIVE)
                .build();

        String borrowingString = borrowing.toString();

        assertTrue(borrowingString.contains(testUser.getEmail()), "toString should contain user email");
        assertTrue(borrowingString.contains(testBook.getTitle()), "toString should contain book title");
        assertTrue(borrowingString.contains(borrowDate.toString()), "toString should contain borrow date");
        assertTrue(borrowingString.contains(dueDate.toString()), "toString should contain due date");
        assertTrue(borrowingString.contains(BorrowingStatus.ACTIVE.toString()), "toString should contain status");
    }

    @ParameterizedTest
    @MethodSource("provideBorrowingScenarios")
    @DisplayName("Should test various borrowing scenarios")
    void shouldTestVariousBorrowingScenarios(
            String testName,
            LocalDate borrowDate,
            LocalDate dueDate,
            LocalDate returnDate,
            BorrowingStatus status,
            boolean expectedIsOverdue) {

        Borrowing borrowing = Borrowing.builder()
                .user(testUser)
                .book(testBook)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .returnDate(returnDate)
                .status(status)
                .build();

        assertEquals(expectedIsOverdue, borrowing.isOverdue(),
                testName + ": incorrect overdue status");
    }

    private static Stream<Arguments> provideBorrowingScenarios() {
        LocalDate today = LocalDate.now();

        return Stream.of(
                // testName, borrowDate, dueDate, returnDate, status, expectedIsOverdue
                Arguments.of(
                        "Active with future due date",
                        today.minusDays(7),
                        today.plusDays(7),
                        null,
                        BorrowingStatus.ACTIVE,
                        false
                ),
                Arguments.of(
                        "Active with past due date",
                        today.minusDays(14),
                        today.minusDays(1),
                        null,
                        BorrowingStatus.ACTIVE,
                        true
                ),
                Arguments.of(
                        "Overdue status",
                        today.minusDays(14),
                        today.minusDays(1),
                        null,
                        BorrowingStatus.OVERDUE,
                        false // isOverdue only considers ACTIVE status
                ),
                Arguments.of(
                        "Returned with past due date",
                        today.minusDays(14),
                        today.minusDays(1),
                        today,
                        BorrowingStatus.RETURNED,
                        false
                )
        );
    }
}