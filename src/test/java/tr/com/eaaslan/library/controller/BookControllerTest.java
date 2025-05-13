package tr.com.eaaslan.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.Genre;
import tr.com.eaaslan.library.model.dto.book.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.book.BookUpdateRequest;
import tr.com.eaaslan.library.repository.BookRepository;
import tr.com.eaaslan.library.repository.UserRepository;
import tr.com.eaaslan.library.service.BookService;

import java.time.Year;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class BookControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Temizleme işlemleri
        bookRepository.deleteAll();

        // Test verilerini oluştur
        Book testBook = Book.builder()
                .isbn("9780132350884")
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(Year.of(2008))
                .publisher("Prentice Hall")
                .genre(Genre.SCIENCE)
                .imageUrl("http://example.com/cover.jpg")
                .description("A handbook of agile software craftsmanship")
                .available(true)
                .quantity(3)
                .build();

        bookRepository.save(testBook);
    }

    // Test veri hazırlama metodları
    private BookCreateRequest createTestBookRequest() {
        return new BookCreateRequest(
                "9781234567890", // Farklı bir ISBN kullan
                "Test Driven Development",
                "Kent Beck",
                2002,
                "Addison-Wesley",
                "SCIENCE",
                "http://example.com/tdd.jpg",
                "Guide to Test Driven Development",
                2
        );
    }

    private BookUpdateRequest createTestBookUpdateRequest() {
        return new BookUpdateRequest(
                null, // ISBN should not be updated
                "Clean Code: Updated Title",
                null,  // No change to author
                null,  // No change to year
                null,  // No change to publisher
                null,  // No change to genre
                null,  // No change to imageUrl
                "Updated description for the book",
                5,    // Update quantity
                true  // Set availability
        );
    }

    @Test
    @DisplayName("Should return all books with pagination")
    void shouldReturnAllBooksWithPagination() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")))
                .andExpect(jsonPath("$[0].author", is("Robert C. Martin")));
    }

    @Test
    @DisplayName("Should return book by ID")
    void shouldReturnBookById() throws Exception {
        // Kitap ID'sini veritabanından al
        Long bookId = bookRepository.findByIsbn("9780132350884").orElseThrow().getId();

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.author", is("Robert C. Martin")));
    }

    @Test
    @DisplayName("Should return 404 when book ID not found")
    void shouldReturn404WhenBookIdNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with ID: '999'"));
    }

    @Test
    @DisplayName("Should return book by ISBN")
    void shouldReturnBookByIsbn() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/books/isbn/9780132350884"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn", is("9780132350884")))
                .andExpect(jsonPath("$.title", is("Clean Code")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should create a new book when authenticated as librarian")
    void shouldCreateNewBookWhenAuthenticatedAsLibrarian() throws Exception {
        // Arrange
        BookCreateRequest request = createTestBookRequest();

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Test Driven Development")))
                .andExpect(jsonPath("$.author", is("Kent Beck")))
                .andExpect(jsonPath("$.isbn", is("9781234567890")));
    }

    @Test
    @DisplayName("Should fail to create a book when not authenticated")
    @WithAnonymousUser
    void shouldFailToCreateBookWhenNotAuthenticated() throws Exception {
        // Arrange
        BookCreateRequest request = createTestBookRequest();

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should return conflict when creating book with existing ISBN")
    void shouldReturnConflictWhenCreatingBookWithExistingIsbn() throws Exception {
        // Arrange - Aynı ISBN ile yeni bir kitap oluştur
        BookCreateRequest request = new BookCreateRequest(
                "9780132350884", // Var olan ISBN
                "Clean Code (Duplicate)",
                "Robert C. Martin",
                2008,
                "Prentice Hall",
                "SCIENCE",
                "http://example.com/cover.jpg",
                "Duplicate book test",
                1
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book already exists with ISBN: '9780132350884'"));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should update a book when authenticated as librarian")
    void shouldUpdateBookWhenAuthenticatedAsLibrarian() throws Exception {
        // Arrange
        BookUpdateRequest request = createTestBookUpdateRequest();
        Long bookId = bookRepository.findByIsbn("9780132350884").orElseThrow().getId();

        // Act & Assert
        mockMvc.perform(put("/api/v1/books/" + bookId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Clean Code: Updated Title")))
                .andExpect(jsonPath("$.description", is("Updated description for the book")))
                .andExpect(jsonPath("$.quantity", is(5)));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should delete a book when authenticated as librarian")
    void shouldDeleteBookWhenAuthenticatedAsLibrarian() throws Exception {
        // Arrange
        Long bookId = bookRepository.findByIsbn("9780132350884").orElseThrow().getId();

        // Act & Assert
        mockMvc.perform(delete("/api/v1/books/" + bookId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Clean Code")));

        // Kitabın silindiğini doğrula
        mockMvc.perform(get("/api/v1/books/" + bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should search books by title")
    void shouldSearchBooksByTitle() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/books/search/title/Clean")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")));
    }

    @Test
    @DisplayName("Should search books by author")
    void shouldSearchBooksByAuthor() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/books/search/author/Martin")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", is("Robert C. Martin")));
    }

    @Test
    @DisplayName("Should search books by genre")
    void shouldSearchBooksByGenre() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/books/search/genre/SCIENCE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].genre", is("SCIENCE")));
    }

    @Test
    @DisplayName("Should get available books")
    void shouldGetAvailableBooks() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/books/available")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].available", is(true)));
    }
}