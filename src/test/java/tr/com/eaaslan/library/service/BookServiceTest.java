package tr.com.eaaslan.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.Genre;
import tr.com.eaaslan.library.model.dto.Book.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.Book.BookResponse;
import tr.com.eaaslan.library.model.dto.Book.BookUpdateRequest;
import tr.com.eaaslan.library.model.mapper.BookMapper;
import tr.com.eaaslan.library.repository.BookRepository;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    private Book testBook;
    private BookResponse testBookResponse;
    private BookCreateRequest testBookCreateRequest;
    private BookUpdateRequest testBookUpdateRequest;
    private List<Book> testBooks;
    private List<BookResponse> testBookResponses;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookServiceImpl;

    @BeforeEach
    void setUp() {
        // Initialize test objects
        testBook = Book.builder()
                .isbn("9780132350884")
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(Year.of(2008))
                .publisher("Prentice Hall")
                .genre(Genre.SCIENCE)
                .imageUrl("http://example.com/image.jpg")
                .description("A handbook of agile software craftsmanship")
                .available(true)
                .quantity(1)
                .build();

        testBookResponse = new BookResponse(
                testBook.getId(),
                testBook.getIsbn(),
                testBook.getTitle(),
                testBook.getAuthor(),
                testBook.getPublicationYear().getValue(),
                testBook.getPublisher(),
                testBook.getGenre().name(),
                testBook.getImageUrl(),
                testBook.getDescription(),
                testBook.getQuantity(),
                testBook.isAvailable(),
                null, // createdAt
                null  // createdBy
        );

        testBookCreateRequest = new BookCreateRequest(
                "9780132350884",
                "Clean Code",
                "Robert C. Martin",
                2008,
                "Prentice Hall",
                "SCIENCE",
                "http://example.com/image.jpg",
                "A handbook of agile software craftsmanship",
                1
        );

        testBookUpdateRequest = new BookUpdateRequest(
                "9780132350884",
                "Clean Code: Updated Title",
                "Robert C. Martin",
                2008,
                "Prentice Hall",
                "SCIENCE",
                "http://example.com/image.jpg",
                "Updated description",
                2,
                true
        );

        testBooks = List.of(testBook);
        testBookResponses = List.of(testBookResponse);
    }

    @Test
    @DisplayName("Create book - Success case")
    void createBook_Success() {
        when(bookRepository.findByIsbn(any(String.class))).thenReturn(Optional.empty());
        when(bookMapper.toEntity(any(BookCreateRequest.class))).thenReturn(testBook);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        BookResponse actual = bookServiceImpl.createBook(testBookCreateRequest);

        assertNotNull(actual);
        assertEquals(testBookResponse.author(), actual.author());
        assertEquals(testBookResponse.isbn(), actual.isbn());

        verify(bookRepository).findByIsbn(testBookCreateRequest.isbn());
        verify(bookMapper).toEntity(testBookCreateRequest);
        verify(bookRepository).save(testBook);
        verify(bookMapper).toResponse(testBook);

    }
}
