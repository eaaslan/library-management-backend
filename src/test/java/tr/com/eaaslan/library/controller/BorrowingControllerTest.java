package tr.com.eaaslan.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WithMockUser
class BorrowingControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BorrowingService borrowingService;

    @MockitoBean

    private SecurityService securityService;

    @Test
    @DisplayName("Should create a new borrowing successfully")
    void shouldCreateNewBorrowing() throws Exception {
        // Arrange
        BorrowingCreateRequest request = new BorrowingCreateRequest(
                1L,
                LocalDate.now().plusDays(14),
                null
        );

        BorrowingResponse response = new BorrowingResponse(
                1L,
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

        when(borrowingService.borrowBook(any(), anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/borrowings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.bookTitle", is("Book Title")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("Should get user's borrowings")
    void shouldGetUserBorrowings() throws Exception {
        // Arrange
        List<BorrowingResponse> borrowings = List.of(
                new BorrowingResponse(
                        1L, 1L, "user@example.com", "User Name",
                        1L, "Book Title", "1234567890",
                        LocalDate.now(), LocalDate.now().plusDays(14), null,
                        BorrowingStatus.ACTIVE.name(), false, LocalDateTime.now(), "system"
                ),
                new BorrowingResponse(
                        2L, 1L, "user@example.com", "User Name",
                        2L, "Another Book", "0987654321",
                        LocalDate.now(), LocalDate.now().plusDays(14), null,
                        BorrowingStatus.ACTIVE.name(), false, LocalDateTime.now(), "system"
                )
        );

        org.springframework.data.domain.Page<BorrowingResponse> page =
                new org.springframework.data.domain.PageImpl<>(borrowings);

        when(borrowingService.getBorrowingsByCurrentUser(anyString(), anyInt(), anyInt()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/borrowings/my-borrowings")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)));
    }

    @Test
    @DisplayName("Should get borrowing by ID")
    void shouldGetBorrowingById() throws Exception {
        // Arrange
        BorrowingResponse response = new BorrowingResponse(
                1L, 1L, "user@example.com", "User Name",
                1L, "Book Title", "1234567890",
                LocalDate.now(), LocalDate.now().plusDays(14), null,
                BorrowingStatus.ACTIVE.name(), false, LocalDateTime.now(), "system"
        );

        when(securityService.isCurrentUserBorrowing(anyLong())).thenReturn(true);
        when(borrowingService.getBorrowingById(anyLong())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/borrowings/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.bookTitle", is("Book Title")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("Should return a book successfully")
    void shouldReturnBookSuccessfully() throws Exception {
        // Arrange
        BorrowingReturnRequest request = new BorrowingReturnRequest(LocalDate.now());

        BorrowingResponse response = new BorrowingResponse(
                1L, 1L, "user@example.com", "User Name",
                1L, "Book Title", "1234567890",
                LocalDate.now().minusDays(14),
                LocalDate.now(),
                LocalDate.now(),
                BorrowingStatus.RETURNED.name(),
                false,
                LocalDateTime.now(),
                "system"
        );

        when(borrowingService.returnBook(anyLong(), any(), anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/borrowings/1/return")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("RETURNED")))
                .andExpect(jsonPath("$.returnDate").exists());
    }
}