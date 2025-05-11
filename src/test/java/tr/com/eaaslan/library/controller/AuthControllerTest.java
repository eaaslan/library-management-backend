package tr.com.eaaslan.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.eaaslan.library.model.dto.auth.LoginRequest;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;
import tr.com.eaaslan.library.security.JwtUtil;
import tr.com.eaaslan.library.service.UserService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @BeforeEach
    void setUp() {
        borrowingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Arrange
        UserCreateRequest registerRequest = new UserCreateRequest(
                "newuser@test.com",
                "password123",
                "John",
                "Doe",
                "05501234567",
                "123 Main St"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("PATRON"));
    }

    @Test
    @DisplayName("Should return 409 Conflict when registering with existing email")
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        // Arrange - Create a user first
        UserCreateRequest initialUser = new UserCreateRequest(
                "existing@test.com",
                "password123",
                "Existing",
                "User",
                "05501234567",
                "123 Main St"
        );

        // Register the user first
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialUser)))
                .andExpect(status().isCreated());

        // Try to register with the same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialUser)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with email: 'existing@test.com'"));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        // Arrange - Create a user first
        UserCreateRequest userRequest = new UserCreateRequest(
                "login@test.com",
                "securePass",
                "Login",
                "User",
                "05501234568",
                "123 Main St"
        );

        // Register the user
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        // Login attempt
        LoginRequest loginReq = new LoginRequest("login@test.com", "securePass");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("login@test.com"))
                .andExpect(jsonPath("$.role").value("ROLE_PATRON"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when credentials are invalid")
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // Arrange - Create a user first
        UserCreateRequest userRequest = new UserCreateRequest(
                "auth@test.com",
                "correctPass",
                "Auth",
                "User",
                "05501234569",
                "123 Main St"
        );

        // Register the user
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        // Wrong login attempt
        LoginRequest loginReq = new LoginRequest("auth@test.com", "wrongPass");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}