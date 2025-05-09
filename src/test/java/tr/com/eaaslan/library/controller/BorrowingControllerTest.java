package tr.com.eaaslan.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.eaaslan.library.model.BorrowingStatus;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingCreateRequest;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingResponse;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingReturnRequest;
import tr.com.eaaslan.library.security.JwtAuthenticationEntryPoint;
import tr.com.eaaslan.library.security.JwtAuthenticationFilter;
import tr.com.eaaslan.library.security.JwtUtil;
import tr.com.eaaslan.library.security.SecurityService;
import tr.com.eaaslan.library.service.BorrowingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BorrowingController.class)
@Import({BorrowingControllerTest.TestConfig.class, BorrowingControllerTest.TestSecurityConfig.class})
class BorrowingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private SecurityService securityService;

    // Configuration class to provide mock beans
    @TestConfiguration
    @EnableMethodSecurity
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
        public SecurityService securityService() {
            SecurityService mock = mock(SecurityService.class);
            when(mock.isCurrentUserBorrowing(anyLong())).thenReturn(true);
            when(mock.isCurrentUser(anyLong())).thenReturn(true);
            return mock;
        }

        @Bean
        public BorrowingService borrowingService() {
            BorrowingService mock = mock(BorrowingService.class);

            // Set up default responses
            BorrowingResponse defaultResponse = new BorrowingResponse(
                    1L, 1L, "user@example.com", "User Name",
                    1L, "Book Title", "1234567890",
                    LocalDate.now(), LocalDate.now().plusDays(14), null,
                    BorrowingStatus.ACTIVE.name(), false,
                    LocalDateTime.now(), "system"
            );

            // Mock methods
            when(mock.borrowBook(any(BorrowingCreateRequest.class), anyString()))
                    .thenReturn(defaultResponse);

            when(mock.returnBook(anyLong(), any(BorrowingReturnRequest.class), anyString()))
                    .thenReturn(defaultResponse);

            when(mock.getBorrowingById(anyLong()))
                    .thenReturn(defaultResponse);

            List<BorrowingResponse> borrowings = List.of(
                    new BorrowingResponse(1L, 1L, "user1@example.com", "User One",
                            1L, "Book One", "1234567890",
                            LocalDate.now().minusDays(7), LocalDate.now().plusDays(7), null,
                            BorrowingStatus.ACTIVE.name(), false, LocalDateTime.now(), "system"),
                    new BorrowingResponse(2L, 2L, "user2@example.com", "User Two",
                            2L, "Book Two", "0987654321",
                            LocalDate.now().minusDays(14), LocalDate.now().minusDays(7), LocalDate.now().minusDays(5),
                            BorrowingStatus.RETURNED.name(), false, LocalDateTime.now(), "system")
            );

            PageImpl<BorrowingResponse> pageResponse = new PageImpl<>(borrowings, PageRequest.of(0, 10), 2);

            when(mock.getAllBorrowings(anyInt(), anyInt(), anyString()))
                    .thenReturn(pageResponse);

            when(mock.getBorrowingsByCurrentUser(anyString(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            when(mock.getBorrowingsByUser(anyLong(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            when(mock.getBorrowingsByBook(anyLong(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            when(mock.getOverdueBorrowings(anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            return mock;
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
                            auth.anyRequest().authenticated()
                    )
                    .build();
        }
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("Should borrow a book when user is authenticated")
    void shouldBorrowBookWhenUserIsAuthenticated() throws Exception {
        // Arrange
        BorrowingCreateRequest createRequest = new BorrowingCreateRequest(1L, LocalDate.now().plusDays(14), null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/borrowings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookTitle", is("Book Title")))
                .andExpect(jsonPath("$.status", is(BorrowingStatus.ACTIVE.name())));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("Should return a book when user is authenticated")
    void shouldReturnBookWhenUserIsAuthenticated() throws Exception {
        // Arrange
        BorrowingReturnRequest returnRequest = new BorrowingReturnRequest(LocalDate.now());

        // Act & Assert
        mockMvc.perform(put("/api/v1/borrowings/1/return")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle", is("Book Title")));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    @DisplayName("Should get all borrowings when librarian is authenticated")
    void shouldGetAllBorrowingsWhenLibrarianIsAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/borrowings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "borrowDate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("Should get my borrowings when user is authenticated")
    void shouldGetMyBorrowingsWhenUserIsAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/borrowings/my-borrowings")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("Should get borrowings by user ID when authorized (self)")
    void shouldGetBorrowingsByUserIdWhenAuthorizedSelf() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/borrowings/user/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    @DisplayName("Should get borrowings by book ID when librarian is authenticated")
    void shouldGetBorrowingsByBookIdWhenLibrarianIsAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/borrowings/book/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    @DisplayName("Should get overdue borrowings when librarian is authenticated")
    void shouldGetOverdueBorrowingsWhenLibrarianIsAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/borrowings/overdue")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = {"PATRON"})
    @DisplayName("Should forbid access to all borrowings when patron is authenticated")
    void shouldForbidAccessToAllBorrowingsWhenPatronIsAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/borrowings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "borrowDate"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("Should get borrowing by ID when authorized")
    void shouldGetBorrowingByIdWhenAuthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/borrowings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle", is("Book Title")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Should allow admin to access any borrowing")
    void shouldAllowAdminToAccessAnyBorrowing() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/borrowings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle", is("Book Title")));
    }
}