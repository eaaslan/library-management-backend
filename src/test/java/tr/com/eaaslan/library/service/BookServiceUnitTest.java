package tr.com.eaaslan.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import tr.com.eaaslan.library.exception.ResourceAlreadyExistException;
import tr.com.eaaslan.library.exception.ResourceNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceUnitTest {

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

    @Test
    @DisplayName("Get book by ID - Success case")
    void getBookById_Success() {
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(testBook));
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        BookResponse actual = bookServiceImpl.getBookById(1L);

        assertNotNull(actual);
        assertEquals(testBookResponse.id(), actual.id());
        assertEquals(testBookResponse.title(), actual.title());

        verify(bookRepository).findById(1L);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Get book by ID - Not found case")
    void getBookById_NotFound() {
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookServiceImpl.getBookById(1L));

        verify(bookRepository).findById(1L);
        verify(bookMapper, never()).toResponse(any(Book.class));
    }

    @Test
    @DisplayName("Get book by ISBN - Success case")
    void getBookByIsbn_Success() {
        when(bookRepository.findByIsbn(any(String.class))).thenReturn(Optional.of(testBook));
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        BookResponse actual = bookServiceImpl.getBookByIsbn("9780132350884");

        assertNotNull(actual);
        assertEquals(testBookResponse.isbn(), actual.isbn());
        assertEquals(testBookResponse.title(), actual.title());

        verify(bookRepository).findByIsbn("9780132350884");
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Get book by ISBN - Not found case")
    void getBookByIsbn_NotFound() {
        when(bookRepository.findByIsbn(any(String.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookServiceImpl.getBookByIsbn("9780132350884"));

        verify(bookRepository).findByIsbn("9780132350884");
        verify(bookMapper, never()).toResponse(any(Book.class));
    }

    @Test
    @DisplayName("Get all books - Success case")
    void getAllBooks_Success() {
        Page<Book> bookPage = new PageImpl<>(testBooks);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("title"));

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        List<BookResponse> actual = bookServiceImpl.getAllBooks(0, 5, "title");

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(testBookResponse.title(), actual.get(0).title());

        verify(bookRepository).findAll(pageable);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Update book - Success case")
    void updateBook_Success() {
        when(bookRepository.getBooksById(any(Long.class))).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        BookResponse actual = bookServiceImpl.updateBook(1L, testBookUpdateRequest);

        assertNotNull(actual);
        assertEquals(testBookResponse.id(), actual.id());

        verify(bookRepository).getBooksById(1L);
        verify(bookMapper).updateEntity(testBookUpdateRequest, testBook);
        verify(bookRepository).save(testBook);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Update book - Not found case")
    void updateBook_NotFound() {
        when(bookRepository.getBooksById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookServiceImpl.updateBook(1L, testBookUpdateRequest));

        verify(bookRepository).getBooksById(1L);
        verify(bookMapper, never()).updateEntity(any(), any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete book - Success case")
    void deleteBook_Success() {
        when(bookRepository.getBooksById(any(Long.class))).thenReturn(Optional.of(testBook));
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        BookResponse actual = bookServiceImpl.deleteBook(1L);

        assertNotNull(actual);
        assertEquals(testBookResponse.id(), actual.id());

        verify(bookRepository).getBooksById(1L);
        verify(bookRepository).delete(testBook);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Delete book - Not found case")
    void deleteBook_NotFound() {
        when(bookRepository.getBooksById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookServiceImpl.deleteBook(1L));

        verify(bookRepository).getBooksById(1L);
        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Search books by title - Success case")
    void searchBooksByTitle_Success() {
        Page<Book> bookPage = new PageImpl<>(testBooks);
        Pageable pageable = PageRequest.of(0, 10);

        when(bookRepository.findByTitleContainingIgnoreCase(any(String.class), any(Pageable.class))).thenReturn(bookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        List<BookResponse> actual = bookServiceImpl.searchBooksByTitle("Clean", 0, 10);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(testBookResponse.title(), actual.get(0).title());

        verify(bookRepository).findByTitleContainingIgnoreCase("Clean", pageable);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Search books by author - Success case")
    void searchBooksByAuthor_Success() {
        Page<Book> bookPage = new PageImpl<>(testBooks);
        Pageable pageable = PageRequest.of(0, 10);

        when(bookRepository.findByAuthorContainingIgnoreCase(any(String.class), any(Pageable.class))).thenReturn(bookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        List<BookResponse> actual = bookServiceImpl.searchBooksByAuthor("Martin", 0, 10);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(testBookResponse.author(), actual.get(0).author());

        verify(bookRepository).findByAuthorContainingIgnoreCase("Martin", pageable);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Search books by genre - Success case")
    void searchBooksByGenre_Success() {
        Page<Book> bookPage = new PageImpl<>(testBooks);
        Pageable pageable = PageRequest.of(0, 10);

        when(bookMapper.stringToGenre(any(String.class))).thenReturn(Genre.SCIENCE);
        when(bookRepository.findByGenre(any(Genre.class), any(Pageable.class))).thenReturn(bookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        List<BookResponse> actual = bookServiceImpl.searchBooksByGenre("SCIENCE", 0, 10);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(testBookResponse.genre(), actual.get(0).genre());

        verify(bookRepository).findByGenre(Genre.SCIENCE, pageable);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Get available books - Success case")
    void getAvailableBooks_Success() {
        Page<Book> bookPage = new PageImpl<>(testBooks);
        Pageable pageable = PageRequest.of(0, 10);

        when(bookRepository.findAllByAvailableTrue(any(Pageable.class))).thenReturn(bookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        Page<BookResponse> actual = bookServiceImpl.getAvailableBooks(0, 10);

        assertNotNull(actual);
        assertEquals(1, actual.getTotalElements());
        assertEquals(testBookResponse.available(), actual.getContent().get(0).available());

        verify(bookRepository).findAllByAvailableTrue(pageable);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Create book - Book already exists case")
    void createBook_BookAlreadyExists() {
        when(bookRepository.findByIsbn(any(String.class))).thenReturn(Optional.of(testBook));

        assertThrows(ResourceAlreadyExistException.class, () -> bookServiceImpl.createBook(testBookCreateRequest));

        verify(bookRepository).findByIsbn(testBookCreateRequest.isbn());
        verify(bookMapper, never()).toEntity(any());
        verify(bookRepository, never()).save(any());
    }
}
