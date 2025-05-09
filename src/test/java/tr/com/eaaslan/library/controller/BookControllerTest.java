package tr.com.eaaslan.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.eaaslan.library.exception.ResourceAlreadyExistException;
import tr.com.eaaslan.library.exception.ResourceNotFoundException;
import tr.com.eaaslan.library.model.dto.Book.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.Book.BookResponse;
import tr.com.eaaslan.library.model.dto.Book.BookUpdateRequest;
import tr.com.eaaslan.library.security.JwtUtil;
import tr.com.eaaslan.library.service.BookService;
import tr.com.eaaslan.library.service.UserService;
import tr.com.eaaslan.library.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import({BookControllerTest.TestConfig.class, BookControllerTest.TestSecurityConfig.class})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookService bookService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserServiceImpl userService;

    // Test data preparation
    private BookResponse createTestBookResponse(Long id) {
        return new BookResponse(
                id,
                "9780132350884",
                "Clean Code",
                "Robert C. Martin",
                2008,
                "Prentice Hall",
                "SCIENCE",
                "http://example.com/cover.jpg",
                "A handbook of agile software craftsmanship",
                3,
                true,
                LocalDateTime.now(),
                "system"
        );
    }

    private BookCreateRequest createTestBookRequest() {
        return new BookCreateRequest(
                "9780132350884",
                "Clean Code",
                "Robert C. Martin",
                2008,
                "Prentice Hall",
                "SCIENCE",
                "http://example.com/cover.jpg",
                "A handbook of agile software craftsmanship",
                3
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
        // Arrange
        List<BookResponse> books = List.of(
                createTestBookResponse(1L),
                createTestBookResponse(2L)
        );
        when(bookService.getAllBooks(anyInt(), anyInt(), anyString())).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(bookService).getAllBooks(0, 10, "title");
    }

    @Test
    @DisplayName("Should return book by ID")
    void shouldReturnBookById() throws Exception {
        // Arrange
        BookResponse book = createTestBookResponse(1L);
        when(bookService.getBookById(1L)).thenReturn(book);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.author", is("Robert C. Martin")));

        verify(bookService).getBookById(1L);
    }

    @Test
    @DisplayName("Should return 404 when book ID not found")
    void shouldReturn404WhenBookIdNotFound() throws Exception {
        // Arrange
        when(bookService.getBookById(999L))
                .thenThrow(new ResourceNotFoundException("Book", "ID", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Book not found with ID: '999'")));

        verify(bookService).getBookById(999L);
    }

    @Test
    @DisplayName("Should return book by ISBN")
    void shouldReturnBookByIsbn() throws Exception {
        // Arrange
        BookResponse book = createTestBookResponse(1L);
        when(bookService.getBookByIsbn("9780132350884")).thenReturn(book);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/isbn/9780132350884"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.isbn", is("9780132350884")))
                .andExpect(jsonPath("$.title", is("Clean Code")));

        verify(bookService).getBookByIsbn("9780132350884");
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should create a new book when authenticated as librarian")
    void shouldCreateNewBookWhenAuthenticatedAsLibrarian() throws Exception {
        // Arrange
        BookCreateRequest request = createTestBookRequest();
        BookResponse response = createTestBookResponse(1L);
        when(bookService.createBook(any(BookCreateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.isbn", is("9780132350884")));

        verify(bookService).createBook(any(BookCreateRequest.class));
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
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403));

        verify(bookService, never()).createBook(any(BookCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should return conflict when creating book with existing ISBN")
    void shouldReturnConflictWhenCreatingBookWithExistingIsbn() throws Exception {
        // Arrange
        BookCreateRequest request = createTestBookRequest();
        when(bookService.createBook(any(BookCreateRequest.class)))
                .thenThrow(new ResourceAlreadyExistException("Book", "ISBN", "9780132350884"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Book already exists with ISBN: '9780132350884'")));

        verify(bookService).createBook(any(BookCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should update a book when authenticated as librarian")
    void shouldUpdateBookWhenAuthenticatedAsLibrarian() throws Exception {
        // Arrange
        BookUpdateRequest request = createTestBookUpdateRequest();
        BookResponse updatedBook = new BookResponse(
                1L,
                "9780132350884",
                "Clean Code: Updated Title",
                "Robert C. Martin",
                2008,
                "Prentice Hall",
                "SCIENCE",
                "http://example.com/cover.jpg",
                "Updated description for the book",
                5,
                true,
                LocalDateTime.now(),
                "system"
        );
        when(bookService.updateBook(eq(1L), any(BookUpdateRequest.class))).thenReturn(updatedBook);

        // Act & Assert
        mockMvc.perform(put("/api/v1/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Clean Code: Updated Title")))
                .andExpect(jsonPath("$.description", is("Updated description for the book")))
                .andExpect(jsonPath("$.quantity", is(5)));

        verify(bookService).updateBook(eq(1L), any(BookUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("Should delete a book when authenticated as librarian")
    void shouldDeleteBookWhenAuthenticatedAsLibrarian() throws Exception {
        // Arrange
        BookResponse deletedBook = createTestBookResponse(1L);
        when(bookService.deleteBook(1L)).thenReturn(deletedBook);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/books/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Clean Code")));

        verify(bookService).deleteBook(1L);
    }

    @Test
    @DisplayName("Should search books by title")
    void shouldSearchBooksByTitle() throws Exception {
        // Arrange
        List<BookResponse> books = List.of(createTestBookResponse(1L));
        when(bookService.searchBooksByTitle(eq("Clean"), anyInt(), anyInt())).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/search/title/Clean")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")));

        verify(bookService).searchBooksByTitle(eq("Clean"), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should search books by author")
    void shouldSearchBooksByAuthor() throws Exception {
        // Arrange
        List<BookResponse> books = List.of(createTestBookResponse(1L));
        when(bookService.searchBooksByAuthor(eq("Martin"), anyInt(), anyInt())).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/search/author/Martin")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", is("Robert C. Martin")));

        verify(bookService).searchBooksByAuthor(eq("Martin"), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should search books by genre")
    void shouldSearchBooksByGenre() throws Exception {
        // Arrange
        List<BookResponse> books = List.of(createTestBookResponse(1L));
        when(bookService.searchBooksByGenre(eq("SCIENCE"), anyInt(), anyInt())).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/search/genre/SCIENCE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].genre", is("SCIENCE")));

        verify(bookService).searchBooksByGenre(eq("SCIENCE"), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should get available books")
    void shouldGetAvailableBooks() throws Exception {
        // Arrange
        Page<BookResponse> books = new PageImpl<>(List.of(createTestBookResponse(1L)));
        when(bookService.getAvailableBooks(anyInt(), anyInt())).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/available")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].available", is(true)));

        verify(bookService).getAvailableBooks(anyInt(), anyInt());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BookService bookService() {
            return mock(BookService.class);
        }
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()) // Let method security do the work
                    .build();
        }
    }
//

//    @TestConfiguration
//    static class TestSecurityConfig {
//        @Bean
//        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//            return http
//                    .csrf(AbstractHttpConfigurer::disable)
//                    .authorizeHttpRequests(auth -> auth
//                            // Method-specific rules should come FIRST (more specific)
//                            .requestMatchers(POST, "/api/v1/books").hasAnyRole("LIBRARIAN", "ADMIN")
//                            .requestMatchers(PUT, "/api/v1/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
//                            .requestMatchers(DELETE, "/api/v1/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
//                            // Then general permitted access (less specific)
//                            .requestMatchers(GET, "/api/v1/books").permitAll()
//                            .requestMatchers(GET, "/api/v1/books/**").permitAll()
//                            .requestMatchers(GET, "/api/v1/books/search/**").permitAll()
//                            .requestMatchers(GET, "/api/v1/books/isbn/**").permitAll()
//                            .requestMatchers(GET, "/api/v1/books/available").permitAll()
//                            .anyRequest().authenticated()
//                    )
//                    .build();
//        }
//
//        @Bean
//        public BookService bookService() {
//            return mock(BookService.class);
//        }
//    }
}