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
import tr.com.eaaslan.library.repository.BorrowingRepository;
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
    private BorrowingRepository borrowingRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        borrowingRepository.deleteAll();
        bookRepository.deleteAll();

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

    private BookCreateRequest createTestBookRequest() {
        return new BookCreateRequest(
                "9781234567890",
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
                null,
                "Clean Code: Updated Title",
                null,
                null,
                null,
                null,
                null,
                "Updated description for the book",
                5,
                true
        );
    }

    @Test
    @DisplayName("Should return all books with pagination")
    void shouldReturnAllBooksWithPagination() throws Exception {

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

        Long bookId = bookRepository.findByIsbn("9780132350884").orElseThrow().getId();

        mockMvc.perform(get("/api/v1/books/" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.author", is("Robert C. Martin")));
    }

    @Test
    @DisplayName("Should return 404 when book ID not found")
    void shouldReturn404WhenBookIdNotFound() throws Exception {

        mockMvc.perform(get("/api/v1/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with ID: '999'"));
    }

    @Test
    @DisplayName("Should return book by ISBN")
    void shouldReturnBookByIsbn() throws Exception {

        mockMvc.perform(get("/api/v1/books/isbn/9780132350884"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn", is("9780132350884")))
                .andExpect(jsonPath("$.title", is("Clean Code")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should create a new book when authenticated as librarian")
    void shouldCreateNewBookWhenAuthenticatedAsLibrarian() throws Exception {

        BookCreateRequest request = createTestBookRequest();

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

        BookCreateRequest request = createTestBookRequest();

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should return conflict when creating book with existing ISBN")
    void shouldReturnConflictWhenCreatingBookWithExistingIsbn() throws Exception {

        BookCreateRequest request = new BookCreateRequest(
                "9780132350884",
                "Clean Code (Duplicate)",
                "Robert C. Martin",
                2008,
                "Prentice Hall",
                "SCIENCE",
                "http://example.com/cover.jpg",
                "Duplicate book test",
                1
        );

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

        BookUpdateRequest request = createTestBookUpdateRequest();
        Long bookId = bookRepository.findByIsbn("9780132350884").orElseThrow().getId();


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

        Long bookId = bookRepository.findByIsbn("9780132350884").orElseThrow().getId();

        mockMvc.perform(delete("/api/v1/books/" + bookId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Clean Code")));

        mockMvc.perform(get("/api/v1/books/" + bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should search books by title")
    void shouldSearchBooksByTitle() throws Exception {

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

        mockMvc.perform(get("/api/v1/books/available")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].available", is(true)));
    }
}