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
import tr.com.eaaslan.library.model.dto.book.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.book.BookResponse;
import tr.com.eaaslan.library.model.dto.book.BookUpdateRequest;
import tr.com.eaaslan.library.model.mapper.BookMapper;
import tr.com.eaaslan.library.repository.BookRepository;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceUnitTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private BookResponse testBookResponse;
    private BookCreateRequest testBookCreateRequest;
    private BookUpdateRequest testBookUpdateRequest;
    private List<Book> testBooks;
    private Page<Book> testBookPage;

    @BeforeEach
    void setUp() {
        // Initialize test book
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

        testBook.setId(1L);

        testBookResponse = new BookResponse(
                1L,
                "9780132350884",
                "Clean Code",
                "Robert C. Martin",
                2008,
                "Prentice Hall",
                "SCIENCE",
                "http://example.com/image.jpg",
                "A handbook of agile software craftsmanship",
                1,
                true,
                LocalDateTime.now(),
                "system"
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
                null,
                "Clean Code: Updated Title",
                null,
                null,
                null,
                null,
                null,
                "Updated description",
                2,
                true
        );

        testBooks = List.of(testBook);
        testBookPage = new PageImpl<>(testBooks);
    }

    @Test
    @DisplayName("Should create a new book successfully")
    void shouldCreateNewBook() {

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(any(BookCreateRequest.class))).thenReturn(testBook);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        BookResponse result = bookService.createBook(testBookCreateRequest);

        assertNotNull(result);
        assertEquals(testBookResponse.title(), result.title());
        assertEquals(testBookResponse.isbn(), result.isbn());

        verify(bookRepository).findByIsbn(testBookCreateRequest.isbn());
        verify(bookMapper).toEntity(testBookCreateRequest);
        verify(bookRepository).save(testBook);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Should throw exception when creating book with existing ISBN")
    void shouldThrowExceptionWhenCreatingBookWithExistingIsbn() {

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(testBook));

        assertThrows(ResourceAlreadyExistException.class, () -> bookService.createBook(testBookCreateRequest));

        verify(bookRepository).findByIsbn(testBookCreateRequest.isbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should get book by ID")
    void shouldGetBookById() {

        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        BookResponse result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals(testBookResponse.id(), result.id());
        assertEquals(testBookResponse.title(), result.title());

        verify(bookRepository).findById(1L);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Should throw exception when book ID not found")
    void shouldThrowExceptionWhenBookIdNotFound() {

        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(1L));

        verify(bookRepository).findById(1L);
        verify(bookMapper, never()).toResponse(any(Book.class));
    }

    @Test
    @DisplayName("Should get book by ISBN")
    void shouldGetBookByIsbn() {

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(testBook));
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        BookResponse result = bookService.getBookByIsbn("9780132350884");

        assertNotNull(result);
        assertEquals(testBookResponse.isbn(), result.isbn());

        verify(bookRepository).findByIsbn("9780132350884");
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Should throw exception when ISBN not found")
    void shouldThrowExceptionWhenIsbnNotFound() {

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookByIsbn("9780132350884"));

        verify(bookRepository).findByIsbn("9780132350884");
        verify(bookMapper, never()).toResponse(any(Book.class));
    }

    @Test
    @DisplayName("Should get all books with pagination")
    void shouldGetAllBooksWithPagination() {

        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(testBookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        List<BookResponse> result = bookService.getAllBooks(0, 10, "title");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBookResponse.title(), result.get(0).title());

        verify(bookRepository).findAll(pageable);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Should update book")
    void shouldUpdateBook() {

        when(bookRepository.getBooksById(anyLong())).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        BookResponse result = bookService.updateBook(1L, testBookUpdateRequest);

        assertNotNull(result);
        assertEquals(testBookResponse.id(), result.id());

        verify(bookRepository).getBooksById(1L);
        verify(bookMapper).updateEntity(testBookUpdateRequest, testBook);
        verify(bookRepository).save(testBook);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent book")
    void shouldThrowExceptionWhenUpdatingNonExistentBook() {

        when(bookRepository.getBooksById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(1L, testBookUpdateRequest));

        verify(bookRepository).getBooksById(1L);
        verify(bookMapper, never()).updateEntity(any(), any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete book")
    void shouldDeleteBook() {

        when(bookRepository.getBooksById(anyLong())).thenReturn(Optional.of(testBook));
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        BookResponse result = bookService.deleteBook(1L);

        assertNotNull(result);
        assertEquals(testBookResponse.id(), result.id());

        verify(bookRepository).getBooksById(1L);
        verify(bookRepository).delete(testBook);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent book")
    void shouldThrowExceptionWhenDeletingNonExistentBook() {

        when(bookRepository.getBooksById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.deleteBook(1L));

        verify(bookRepository).getBooksById(1L);
        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should search books by title")
    void shouldSearchBooksByTitle() {

        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(testBookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        List<BookResponse> result = bookService.searchBooksByTitle("Clean", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBookResponse.title(), result.get(0).title());

        verify(bookRepository).findByTitleContainingIgnoreCase("Clean", pageable);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Should search books by author")
    void shouldSearchBooksByAuthor() {

        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findByAuthorContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(testBookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        List<BookResponse> result = bookService.searchBooksByAuthor("Martin", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBookResponse.author(), result.get(0).author());

        verify(bookRepository).findByAuthorContainingIgnoreCase("Martin", pageable);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Should search books by genre")
    void shouldSearchBooksByGenre() {

        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findByGenre(any(Genre.class), any(Pageable.class))).thenReturn(testBookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        List<BookResponse> result = bookService.searchBooksByGenre("SCIENCE", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBookResponse.genre(), result.getFirst().genre());

        verify(bookRepository).findByGenre(Genre.SCIENCE, pageable);
        verify(bookMapper).toResponse(testBook);
    }

    @Test
    @DisplayName("Should get available books")
    void shouldGetAvailableBooks() {

        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAllByAvailableTrue(any(Pageable.class))).thenReturn(testBookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(testBookResponse);

        Page<BookResponse> result = bookService.getAvailableBooks(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBookResponse.title(), result.getContent().getFirst().title());
        assertTrue(result.getContent().getFirst().available());

        verify(bookRepository).findAllByAvailableTrue(pageable);
        verify(bookMapper).toResponse(testBook);
    }
}