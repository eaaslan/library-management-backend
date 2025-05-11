package tr.com.eaaslan.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.eaaslan.library.model.BorrowingStatus;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingCreateRequest;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingResponse;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingReturnRequest;
import tr.com.eaaslan.library.security.SecurityService;
import tr.com.eaaslan.library.service.BorrowingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test class - Tests the security configuration of BorrowingController
 * in a real environment setting.
 * <p>
 * This test class starts the full Spring Boot application context and loads all
 * application components including the security configuration.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BorrowingControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BorrowingService borrowingService;

    @MockitoBean
    private SecurityService securityService;

    // Helper methods for creating test data

    private BorrowingCreateRequest createBorrowingRequest() {
        return new BorrowingCreateRequest(
                1L,
                LocalDate.now().plusDays(14),
                null
        );
    }

    private BorrowingReturnRequest createReturnRequest() {
        return new BorrowingReturnRequest(
                LocalDate.now()
        );
    }

    private BorrowingResponse createBorrowingResponse(Long id) {
        return new BorrowingResponse(
                id,
                1L,
                "user@example.com",
                "User Name",
                1L,
                "Book Title",
                "1234567890",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                null,
                BorrowingStatus.ACTIVE.name(),
                false,
                LocalDateTime.now(),
                "system"
        );
    }

    private List<BorrowingResponse> createBorrowingResponseList() {
        return List.of(
                createBorrowingResponse(1L),
                createBorrowingResponse(2L)
        );
    }

    // Security tests

    @Test
    @DisplayName("Anonymous user cannot borrow a book")
    @WithAnonymousUser
    void anonymousUserCannotBorrowBook() throws Exception {
        BorrowingCreateRequest request = createBorrowingRequest();

        mockMvc.perform(post("/api/v1/borrowings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Patron can borrow a book")
    @WithMockUser(roles = "PATRON", username = "patron@example.com")
    void patronCanBorrowBook() throws Exception {
        BorrowingCreateRequest request = createBorrowingRequest();
        BorrowingResponse response = createBorrowingResponse(1L);

        when(borrowingService.borrowBook(any(BorrowingCreateRequest.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/borrowings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(borrowingService).borrowBook(any(BorrowingCreateRequest.class), eq("patron@example.com"));
    }

    @Test
    @DisplayName("Anonymous user cannot return a book")
    @WithAnonymousUser
    void anonymousUserCannotReturnBook() throws Exception {
        BorrowingReturnRequest request = createReturnRequest();

        mockMvc.perform(put("/api/v1/borrowings/1/return")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Patron can return their own borrowed book")
    @WithMockUser(roles = "PATRON", username = "patron@example.com")
    void patronCanReturnTheirBook() throws Exception {
        BorrowingReturnRequest request = createReturnRequest();
        BorrowingResponse response = createBorrowingResponse(1L);

        when(borrowingService.returnBook(eq(1L), any(BorrowingReturnRequest.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/borrowings/1/return")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(borrowingService).returnBook(eq(1L), any(BorrowingReturnRequest.class), eq("patron@example.com"));
    }

    @Test
    @DisplayName("Anonymous user cannot see all borrowings")
    @WithAnonymousUser
    void anonymousUserCannotSeeAllBorrowings() throws Exception {
        mockMvc.perform(get("/api/v1/borrowings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "borrowDate"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Patron cannot see all borrowings")
    @WithMockUser(roles = "PATRON")
    void patronCannotSeeAllBorrowings() throws Exception {
        mockMvc.perform(get("/api/v1/borrowings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "borrowDate"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Librarian can see all borrowings")
    @WithMockUser(roles = "LIBRARIAN")
    void librarianCanSeeAllBorrowings() throws Exception {
        PageImpl<BorrowingResponse> page = new PageImpl<>(
                createBorrowingResponseList(),
                PageRequest.of(0, 10),
                2
        );

        when(borrowingService.getAllBorrowings(anyInt(), anyInt(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/borrowings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "borrowDate"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(borrowingService).getAllBorrowings(0, 10, "borrowDate");
    }

    @Test
    @DisplayName("Anonymous user cannot see their borrowings")
    @WithAnonymousUser
    void anonymousUserCannotSeeTheirBorrowings() throws Exception {
        mockMvc.perform(get("/api/v1/borrowings/my-borrowings")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Patron can see their own borrowings")
    @WithMockUser(roles = "PATRON", username = "patron@example.com")
    void patronCanSeeTheirOwnBorrowings() throws Exception {
        PageImpl<BorrowingResponse> page = new PageImpl<>(
                createBorrowingResponseList(),
                PageRequest.of(0, 10),
                2
        );

        when(borrowingService.getBorrowingsByCurrentUser(anyString(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/borrowings/my-borrowings")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(borrowingService).getBorrowingsByCurrentUser("patron@example.com", 0, 10);
    }

    @Test
    @DisplayName("Patron cannot see other user's borrowings")
    @WithMockUser(roles = "PATRON")
    void patronCannotSeeOtherUserBorrowings() throws Exception {
        when(securityService.isCurrentUser(anyLong())).thenReturn(false);

        mockMvc.perform(get("/api/v1/borrowings/user/2")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(securityService).isCurrentUser(2L);
        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Patron can see their own user borrowings")
    @WithMockUser(roles = "PATRON")
    void patronCanSeeTheirOwnUserBorrowings() throws Exception {
        when(securityService.isCurrentUser(anyLong())).thenReturn(true);

        PageImpl<BorrowingResponse> page = new PageImpl<>(
                createBorrowingResponseList(),
                PageRequest.of(0, 10),
                2
        );

        when(borrowingService.getBorrowingsByUser(anyLong(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/borrowings/user/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(securityService).isCurrentUser(1L);
        verify(borrowingService).getBorrowingsByUser(1L, 0, 10);
    }

    @Test
    @DisplayName("Librarian can see any user's borrowings")
    @WithMockUser(roles = "LIBRARIAN")
    void librarianCanSeeAnyUserBorrowings() throws Exception {
        PageImpl<BorrowingResponse> page = new PageImpl<>(
                createBorrowingResponseList(),
                PageRequest.of(0, 10),
                2
        );

        when(borrowingService.getBorrowingsByUser(anyLong(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/borrowings/user/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(borrowingService).getBorrowingsByUser(1L, 0, 10);
    }

    @Test
    @DisplayName("Anonymous user cannot see borrowings by book")
    @WithAnonymousUser
    void anonymousUserCannotSeeBorrowingsByBook() throws Exception {
        mockMvc.perform(get("/api/v1/borrowings/book/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Patron cannot see borrowings by book")
    @WithMockUser(roles = "PATRON")
    void patronCannotSeeBorrowingsByBook() throws Exception {
        mockMvc.perform(get("/api/v1/borrowings/book/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Librarian can see borrowings by book")
    @WithMockUser(roles = "LIBRARIAN")
    void librarianCanSeeBorrowingsByBook() throws Exception {
        PageImpl<BorrowingResponse> page = new PageImpl<>(
                createBorrowingResponseList(),
                PageRequest.of(0, 10),
                2
        );

        when(borrowingService.getBorrowingsByBook(anyLong(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/borrowings/book/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(borrowingService).getBorrowingsByBook(1L, 0, 10);
    }

    @Test
    @DisplayName("Anonymous user cannot see overdue borrowings")
    @WithAnonymousUser
    void anonymousUserCannotSeeOverdueBorrowings() throws Exception {
        mockMvc.perform(get("/api/v1/borrowings/overdue")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Patron cannot see overdue borrowings")
    @WithMockUser(roles = "PATRON")
    void patronCannotSeeOverdueBorrowings() throws Exception {
        mockMvc.perform(get("/api/v1/borrowings/overdue")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Librarian can see overdue borrowings")
    @WithMockUser(roles = "LIBRARIAN")
    void librarianCanSeeOverdueBorrowings() throws Exception {
        PageImpl<BorrowingResponse> page = new PageImpl<>(
                createBorrowingResponseList(),
                PageRequest.of(0, 10),
                2
        );

        when(borrowingService.getOverdueBorrowings(anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/borrowings/overdue")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(borrowingService).getOverdueBorrowings(0, 10);
    }

    @Test
    @DisplayName("Anonymous user cannot see borrowing by ID")
    @WithAnonymousUser
    void anonymousUserCannotSeeBorrowingById() throws Exception {
        mockMvc.perform(get("/api/v1/borrowings/1"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Patron cannot see other patron's borrowing by ID")
    @WithMockUser(roles = "PATRON")
    void patronCannotSeeOtherPatronBorrowingById() throws Exception {
        when(securityService.isCurrentUserBorrowing(anyLong())).thenReturn(false);

        mockMvc.perform(get("/api/v1/borrowings/1"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(securityService).isCurrentUserBorrowing(1L);
        verifyNoInteractions(borrowingService);
    }

    @Test
    @DisplayName("Patron can see their own borrowing by ID")
    @WithMockUser(roles = "PATRON")
    void patronCanSeeTheirOwnBorrowingById() throws Exception {
        when(securityService.isCurrentUserBorrowing(anyLong())).thenReturn(true);

        BorrowingResponse response = createBorrowingResponse(1L);
        when(borrowingService.getBorrowingById(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/borrowings/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(securityService).isCurrentUserBorrowing(1L);
        verify(borrowingService).getBorrowingById(1L);
    }

    @Test
    @DisplayName("Librarian can see any borrowing by ID")
    @WithMockUser(roles = "LIBRARIAN")
    void librarianCanSeeAnyBorrowingById() throws Exception {
        BorrowingResponse response = createBorrowingResponse(1L);
        when(borrowingService.getBorrowingById(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/borrowings/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(borrowingService).getBorrowingById(1L);
    }
}