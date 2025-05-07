package tr.com.eaaslan.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.eaaslan.library.model.dto.Book.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.Book.BookResponse;
import tr.com.eaaslan.library.model.dto.Book.BookUpdateRequest;
import tr.com.eaaslan.library.security.JwtAuthenticationEntryPoint;
import tr.com.eaaslan.library.security.JwtAuthenticationFilter;
import tr.com.eaaslan.library.security.JwtUtil;
import tr.com.eaaslan.library.service.BookService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import({BookControllerTest.TestConfig.class, BookControllerTest.TestSecurityConfig.class})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookService bookService;

    // Configuration class to provide mock beans
    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return mock(JwtAuthenticationFilter.class);
        }

        @Bean
        public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
            return mock(JwtAuthenticationEntryPoint.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean
        public BookService bookService() {
            return mock(BookService.class);
        }
    }

    // Test security configuration to simplify security for tests
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for tests
                    .authorizeHttpRequests(auth ->
                            auth.requestMatchers("/api/v1/books/**").permitAll() // Allow anonymous access to book endpoints
                                    .anyRequest().authenticated()
                    )
                    .build();
        }
    }

    @Test
    @DisplayName("Should return all books when no authentication required")
    void shouldReturnAllBooksWhenNoAuthRequired() throws Exception {
        // Arrange
        List<BookResponse> books = List.of(
                new BookResponse(1L, "1234567890", "Book 1", "Author 1",
                        2020, "Publisher", "FICTION", null, "Description",
                        1, true, LocalDateTime.now(), "system"),
                new BookResponse(2L, "0987654321", "Book 2", "Author 2",
                        2021, "Publisher", "SCIENCE", null, "Description",
                        1, true, LocalDateTime.now(), "system")
        );

        when(bookService.getAllBooks(eq(0), eq(5), eq("title"))).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Book 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Book 2")));
    }

    @Test
    @DisplayName("Should return book by ID when no authentication required")
    void shouldReturnBookByIdWhenNoAuthRequired() throws Exception {
        // Arrange
        BookResponse book = new BookResponse(1L, "1234567890", "Book 1", "Author 1",
                2020, "Publisher", "FICTION", null, "Description",
                1, true, LocalDateTime.now(), "system");

        when(bookService.getBookById(1L)).thenReturn(book);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Book 1")))
                .andExpect(jsonPath("$.author", is("Author 1")));
    }

    //TODO CHECK HERE
    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should create a book when librarian is authenticated")
    void shouldCreateBookWhenLibrarianIsAuthenticated() throws Exception {
        // Arrange
        BookCreateRequest createRequest = new BookCreateRequest(
                "1234567890", "New Book", "New Author", 2022,
                "Publisher", "FICTION", null, "Description", 1);

        BookResponse createdBook = new BookResponse(1L, "1234567890", "New Book", "New Author",
                2022, "Publisher", "FICTION", null, "Description",
                1, true, LocalDateTime.now(), "librarian@library.com");

        when(bookService.createBook(any(BookCreateRequest.class))).thenReturn(createdBook);

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("New Book")))
                .andExpect(jsonPath("$.author", is("New Author")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should update a book when librarian is authenticated")
    void shouldUpdateBookWhenLibrarianIsAuthenticated() throws Exception {
        // Arrange
        var updateRequest = new BookUpdateRequest(
                null, "Updated Title", null, null,
                null, null, null, "Updated description", null, null);

        BookResponse updatedBook = new BookResponse(
                1L, "1234567890", "Updated Title", "Author 1",
                2020, "Publisher", "FICTION", null, "Updated description",
                1, true, LocalDateTime.now(), "librarian@library.com");

        when(bookService.updateBook(eq(1L), any())).thenReturn(updatedBook);

        // Act & Assert
        mockMvc.perform(put("/api/v1/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should delete a book when librarian is authenticated")
    void shouldDeleteBookWhenLibrarianIsAuthenticated() throws Exception {
        // Arrange
        BookResponse deletedBook = new BookResponse(
                1L, "1234567890", "Book 1", "Author 1",
                2020, "Publisher", "FICTION", null, "Description",
                1, true, LocalDateTime.now(), "librarian@library.com");

        when(bookService.deleteBook(1L)).thenReturn(deletedBook);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/books/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Book 1")));
    }

    @Test
    @DisplayName("Should search books by title when no authentication required")
    void shouldSearchBooksByTitleWhenNoAuthRequired() throws Exception {
        // Arrange
        List<BookResponse> books = List.of(
                new BookResponse(1L, "1234567890", "Spring Boot Book", "Author 1",
                        2020, "Publisher", "FICTION", null, "Description",
                        1, true, LocalDateTime.now(), "system")
        );

        when(bookService.searchBooksByTitle(eq("Spring"), eq(0), eq(10))).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/search/title/Spring")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Spring Boot Book")));
    }
}