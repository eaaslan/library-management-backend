package tr.com.eaaslan.library.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tr.com.eaaslan.library.config.TestJpaConfig;
import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.Genre;
import tr.com.eaaslan.library.util.BookTestData;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class BookRepositoryTest {
    
    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        bookRepository.deleteAll();
        List<Book> books = BookTestData.getTestBooks();
        bookRepository.saveAll(books);
    }


    @Test
    @DisplayName("Should find all books")
    void shouldFindAllBooks() {
        List<Book> books = bookRepository.findAll();
        assertEquals(20, books.size());
    }

    @Test
    @DisplayName("Should find available books")
    void shouldFindAvailableBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> books = bookRepository.findAllByAvailableTrue(pageable);
        assertEquals(BookTestData.getAvailableBooks().size(), books.getTotalElements());
    }

    @Test
    @DisplayName("Should find books by genre")
    void shouldFindBooksByGenre() {
        Page<Book> books = bookRepository.findByGenre(Genre.PSYCHOLOGY, PageRequest.of(0, 1));
        assertEquals(BookTestData.getBooksWithGenre((Genre.PSYCHOLOGY)).size(), books.getTotalElements());
    }

    @Test
    @DisplayName("Should find books by title containing text")
    void shouldFindBooksByTitleContainingText() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Book> booksWithWhere = bookRepository.findByTitleContainingIgnoreCase("where", pageable);
        Page<Book> booksWithAnd = bookRepository.findByTitleContainingIgnoreCase("and", pageable);
        Page<Book> booksWithNonExistent = bookRepository.findByTitleContainingIgnoreCase("nonexistent", pageable);

        assertEquals(1, booksWithWhere.getTotalElements(), "Should find one book with 'where' in title");
        assertEquals(3, booksWithAnd.getTotalElements(), "Should find one book with 'and' in title");
        assertEquals(0, booksWithNonExistent.getTotalElements(), "Should find no books with 'nonexistent' in title");
    }

    @Test
    @DisplayName("Should find books by title containing text")
    void shouldFindBooksByAuthorContainingText() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Book> booksWithAdam = bookRepository.findByAuthorContainingIgnoreCase("adam", pageable);
        Page<Book> booksWithTournier = bookRepository.findByAuthorContainingIgnoreCase("tournier", pageable);
        Page<Book> booksWithNonExistent = bookRepository.findByAuthorContainingIgnoreCase("nonexistent", pageable);

        assertEquals(3, booksWithAdam.getTotalElements(), "Should find one book with 'where' in author");
        assertEquals(1, booksWithTournier.getTotalElements(), "Should find one book with 'and' in author");
        assertEquals(0, booksWithNonExistent.getTotalElements(), "Should find no books with 'nonexistent' in author");
    }

    @Test
    @DisplayName("Should find books by ISBN")
    void shouldFindBookByIsbn() {
        String existingIsbn = "074322678X";
        String nonExistingIsbn = "0000000000";

        Optional<Book> foundBook = bookRepository.findByIsbn(existingIsbn);
        Optional<Book> notFoundBook = bookRepository.findByIsbn(nonExistingIsbn);

        assertTrue(foundBook.isPresent(), "Should find book with existing ISBN");
        assertEquals("Where You'll Find Me: And Other Stories", foundBook.get().getTitle());
        assertEquals("Ann Beattie", foundBook.get().getAuthor());

        assertFalse(notFoundBook.isPresent(), "Should not find book with non-existing ISBN");
    }

    @Test
    @DisplayName("Should save new book")
    void shouldSaveNewBook() {

        Book newBook = Book.builder()
                .isbn("9780132350884")
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(Year.of(2008))
                .publisher("Prentice Hall")
                .genre(Genre.SCIENCE)
                .description("A handbook of agile software craftsmanship")
                .available(true)
                .quantity(1)
                .build();

        Book savedBook = bookRepository.save(newBook);

        assertNotNull(savedBook.getId(), "Saved book should have an ID");

        Optional<Book> retrievedBook = bookRepository.findByIsbn("9780132350884");
        assertTrue(retrievedBook.isPresent(), "Should retrieve the saved book");
        assertEquals("Clean Code", retrievedBook.get().getTitle());
        assertEquals("Robert C. Martin", retrievedBook.get().getAuthor());
    }

    @Test
    @DisplayName("Should update existing book")
    void shouldUpdateExistingBook() {

        String isbn = "074322678X"; // Existing ISBN from test data
        Optional<Book> bookToUpdate = bookRepository.findByIsbn(isbn);
        assertTrue(bookToUpdate.isPresent(), "Book to update should exist");

        Book book = bookToUpdate.get();
        book.setTitle("Updated Title");
        book.setDescription("Updated description");

        Book updatedBook = bookRepository.save(book);

        assertEquals("Updated Title", updatedBook.getTitle());
        assertEquals("Updated description", updatedBook.getDescription());

        Optional<Book> retrievedBook = bookRepository.findByIsbn(isbn);
        assertTrue(retrievedBook.isPresent());
        assertEquals("Updated Title", retrievedBook.get().getTitle());
        assertEquals("Updated description", retrievedBook.get().getDescription());
    }

    @Test
    @DisplayName("Should delete book by id")
    void shouldDeleteBookById() {

        String isbn = "074322678X"; // Existing ISBN from test data
        Optional<Book> bookToDelete = bookRepository.findByIsbn(isbn);
        assertTrue(bookToDelete.isPresent(), "Book to delete should exist");

        Long bookId = bookToDelete.get().getId();

        bookRepository.deleteById(bookId);

        Optional<Book> retrievedBook = bookRepository.findByIsbn(isbn);
        assertFalse(retrievedBook.isPresent(), "Book should be deleted");
    }

    @Test
    @DisplayName("Should find books with pagination and sorting")
    void shouldFindBooksWithPaginationAndSorting() {

        Pageable firstPageWithTwoItems = PageRequest.of(0, 2, Sort.by("title").ascending());
        Pageable secondPageWithTwoItems = PageRequest.of(1, 2, Sort.by("title").ascending());

        Page<Book> firstPage = bookRepository.findAll(firstPageWithTwoItems);
        Page<Book> secondPage = bookRepository.findAll(secondPageWithTwoItems);

        assertEquals(2, firstPage.getContent().size(), "First page should have 2 items");
        assertEquals(2, secondPage.getContent().size(), "Second page should have 2 items");
        assertNotEquals(firstPage.getContent().get(0).getTitle(), secondPage.getContent().get(0).getTitle(),
                "First and second page should have different books");
    }

    @ParameterizedTest
    @MethodSource("provideGenresAndCounts")
    @DisplayName("Should find correct number of books by genre")
    void shouldFindCorrectNumberOfBooksByGenre(Genre genre, int expectedCount) {
        Page<Book> books = bookRepository.findByGenre(genre, PageRequest.of(0, 20));
        assertEquals(books.getTotalElements(), expectedCount, "Should find correct number of books by genre " + genre);
    }

    private static Stream<Arguments> provideGenresAndCounts() {
        return Stream.of(
                Arguments.of(Genre.PSYCHOLOGY, 3),
                Arguments.of(Genre.MUSIC, 3),
                Arguments.of(Genre.DRAMA, 2),
                Arguments.of(Genre.SCIENCE, 1),
                Arguments.of(Genre.FANTASY, 0)
        );
    }

}
