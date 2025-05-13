package tr.com.eaaslan.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.eaaslan.library.model.dto.book.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.book.BookResponse;
import tr.com.eaaslan.library.model.dto.book.BookUpdateRequest;
import tr.com.eaaslan.library.service.BookService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test class - Tests the security configuration of BookController
 * in a real environment setting with containerized PostgreSQL.
 * <p>
 * This test class starts the full Spring Boot application context and loads all
 * application components including the security configuration and database.
 */

@AutoConfigureMockMvc
class BookControllerSecurityIntegrationTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;


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
                "Robert C. Martin",
                2008,
                "Prentice Hall",
                "SCIENCE",
                "http://example.com/cover.jpg",
                "Updated description for the book",
                5,
                true
        );
    }

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

    // Security tests

    @Test
    @DisplayName("Anonymous user cannot create a book")
    @WithAnonymousUser
    void anonymousUserCannotCreateBook() throws Exception {
        BookCreateRequest request = createTestBookRequest();

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("User with librarian role can create a book")
    @WithMockUser(roles = "LIBRARIAN")
    void librarianCanCreateBook() throws Exception {
        BookCreateRequest request = createTestBookRequest();
        BookResponse response = createTestBookResponse(1L);

        when(bookService.createBook(any(BookCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookService).createBook(any(BookCreateRequest.class));
    }

    @Test
    @DisplayName("User with admin role can create a book")
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateBook() throws Exception {
        BookCreateRequest request = createTestBookRequest();
        BookResponse response = createTestBookResponse(1L);

        when(bookService.createBook(any(BookCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookService).createBook(any(BookCreateRequest.class));
    }

    @Test
    @DisplayName("User with patron role cannot create a book")
    @WithMockUser(roles = "PATRON")
    void patronCannotCreateBook() throws Exception {
        BookCreateRequest request = createTestBookRequest();

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("Anonymous user cannot update a book")
    @WithAnonymousUser
    void anonymousUserCannotUpdateBook() throws Exception {
        BookUpdateRequest request = createTestBookUpdateRequest();

        mockMvc.perform(put("/api/v1/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("User with librarian role can update a book")
    @WithMockUser(roles = "LIBRARIAN")
    void librarianCanUpdateBook() throws Exception {
        BookUpdateRequest request = createTestBookUpdateRequest();
        BookResponse response = createTestBookResponse(1L);

        when(bookService.updateBook(eq(1L), any(BookUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookService).updateBook(eq(1L), any(BookUpdateRequest.class));
    }

    @Test
    @DisplayName("Anonymous user cannot delete a book")
    @WithAnonymousUser
    void anonymousUserCannotDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("User with librarian role can delete a book")
    @WithMockUser(roles = "LIBRARIAN")
    void librarianCanDeleteBook() throws Exception {
        BookResponse response = createTestBookResponse(1L);

        when(bookService.deleteBook(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/books/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookService).deleteBook(1L);
    }

    @Test
    @DisplayName("Anyone can view the book list")
    @WithAnonymousUser
    void anyoneCanListBooks() throws Exception {
        mockMvc.perform(get("/api/v1/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "title"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(bookService).getAllBooks(0, 10, "title");
    }

    @Test
    @DisplayName("Anyone can view book details")
    @WithAnonymousUser
    void anyoneCanViewBookDetails() throws Exception {
        BookResponse response = createTestBookResponse(1L);

        when(bookService.getBookById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/books/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookService).getBookById(1L);
    }
}