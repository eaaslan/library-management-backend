package tr.com.eaaslan.library.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Year;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create a valid book")
    void shouldCreateValidBook() {
        Book book = Book.builder()
                .isbn("1234567890")
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(Year.of(2008))
                .publisher("Prentice Hall")
                .genre(Genre.NON_FICTION)
                .description("A handbook of agile software craftsmanship")
                .build();

        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertTrue(violations.isEmpty(), "Book should have no validation violations");

    }

    @ParameterizedTest
    @CsvSource(value = {
            ",ISBN is required",
            "12345,ISBN must be between 10 and 13 characters",
            "12345123451234512345,ISBN must be between 10 and 13 characters"
    }, nullValues = "null")
    @DisplayName("Should reject invalid ISBNs with specific errors")
    void shouldRejectInvalidIsbn(String invalidIsbn, String expectedMessage) {

        Book book = Book.builder()
                .isbn(invalidIsbn)
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(Year.of(2008))
                .publisher("Prentice Hall")
                .genre(Genre.NON_FICTION)
                .build();

        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Book should have validation violations");

        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        assertEquals(expectedMessage, messages.getFirst());
    }

    @Test
    @DisplayName("Should reject empty ISBN with multiple violations")
    void shouldRejectEmptyIsbnWithMultipleViolations() {
        Book book = Book.builder()
                .isbn("")
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(Year.of(2008))
                .publisher("Prentice Hall")
                .genre(Genre.NON_FICTION)
                .build();

        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertEquals(2, violations.size(), "Should have exactly two violations");

        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toList();

        assertAll(
                () -> assertTrue(messages.contains("ISBN is required")),
                () -> assertTrue(messages.contains("ISBN must be between 10 and 13 characters"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideBooksWithValidationErrors")
    @DisplayName("Should reject books with validation errors")
    void shouldRejectBooksWithValidationErrors(
            String testCase,
            Book invalidBook,
            String expectedErrorMessage) {

        // When
        Set<ConstraintViolation<Book>> violations = validator.validate(invalidBook);

        // Then
        assertFalse(violations.isEmpty(),
                testCase + " should have validation violations");

        boolean hasExpectedMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .anyMatch(message -> message.equals(expectedErrorMessage));

        assertTrue(hasExpectedMessage,
                "Should have validation message: " + expectedErrorMessage);
    }

    private static Stream<Arguments> provideBooksWithValidationErrors() {
        return Stream.of(
                // Blank title case
                Arguments.of(
                        "Book with blank title",
                        Book.builder()
                                .isbn("1234567890")
                                .title("")
                                .author("Robert C. Martin")
                                .publicationYear(Year.of(2008))
                                .publisher("Prentice Hall")
                                .genre(Genre.NON_FICTION)
                                .build(),
                        "Title is required"
                ),

                // Null genre case
                Arguments.of(
                        "Book with null genre",
                        Book.builder()
                                .isbn("1234567890")
                                .title("Clean Code")
                                .author("Robert C. Martin")
                                .publicationYear(Year.of(2008))
                                .publisher("Prentice Hall")
                                .genre(null)
                                .build(),
                        "Genre is required"
                ),

                // Future publication year case
                Arguments.of(
                        "Book with future publication year",
                        Book.builder()
                                .isbn("1234567890")
                                .title("Clean Code")
                                .author("Robert C. Martin")
                                .publicationYear(Year.now().plusYears(1))
                                .publisher("Prentice Hall")
                                .genre(Genre.NON_FICTION)
                                .build(),
                        "Publication year must be in the past"
                )
        );
    }

    @Test
    @DisplayName("Should test equal and hashcode")
    void shouldTestEqualAndHashcode() {
        Book book1 = Book.builder()
                .isbn("1234567890")
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(Year.of(2008))
                .publisher("Prentice Hall")
                .genre(Genre.NON_FICTION)
                .build();

        Book book2 = Book.builder()
                .isbn("1234567890")
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(Year.of(2008))
                .publisher("Prentice Hall")
                .genre(Genre.NON_FICTION)
                .build();

        Book book3 = Book.builder()
                .isbn("1234567891")
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(Year.of(2008))
                .publisher("Prentice Hall")
                .genre(Genre.NON_FICTION)
                .build();

        assertEquals(book1, book2);
        assertNotEquals(book1, book3);
        assertEquals(book1.hashCode(), book2.hashCode());
        assertNotEquals(book1.hashCode(), book3.hashCode());
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        Book book = Book.builder()
                .isbn("1234567890")
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(Year.of(2008))
                .publisher("Prentice Hall")
                .genre(Genre.NON_FICTION)
                .build();

        String bookString = book.toString();

        assertTrue(bookString.contains("1234567890"), "toString should contain ISBN");
        assertTrue(bookString.contains("Clean Code"), "toString should contain title");
        assertTrue(bookString.contains("Robert C. Martin"), "toString should contain author");
    }
}