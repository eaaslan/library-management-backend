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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.eaaslan.library.exception.ResourceAlreadyExistException;
import tr.com.eaaslan.library.model.dto.auth.LoginRequest;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.security.JwtUtil;
import tr.com.eaaslan.library.security.LibraryUserDetails;
import tr.com.eaaslan.library.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({AuthControllerTest.TestSecurityConfig.class, AuthControllerTest.TestConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        // Arrange
        var loginReq = new LoginRequest("admin@test.com", "securePass");
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        LibraryUserDetails userDetails = mock(LibraryUserDetails.class);
        when(userDetails.getUsername()).thenReturn("admin@test.com");

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        doReturn(authorities).when(userDetails).getAuthorities();

        when(auth.getPrincipal()).thenReturn(userDetails);

        when(jwtUtil.generateToken(auth)).thenReturn("fake-jwt-token");
        when(jwtUtil.getExpirationTimeInSeconds("fake-jwt-token")).thenReturn(3600L);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }


    @Test
    @DisplayName("Should return 401 Unauthorized when credentials are invalid")
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // Arrange
        var loginReq = new LoginRequest("user@test.com", "wrongPass");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
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

        UserResponse createdUser = new UserResponse(
                1L,
                "newuser@test.com",
                "John",
                "Doe",
                "05501234567",
                "PATRON",
                "ACTIVE",
                3,
                LocalDateTime.now(),
                "system"
        );

        when(userService.createPatronUser(any(UserCreateRequest.class))).thenReturn(createdUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("PATRON"));
    }

    @Test
    @DisplayName("Should return 409 Conflict when registering with existing email")
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        // Arrange
        UserCreateRequest registerRequest = new UserCreateRequest(
                "existing@test.com",
                "password123",
                "John",
                "Doe",
                "05501234567",
                "123 Main St"
        );

        when(userService.createPatronUser(any(UserCreateRequest.class)))
                .thenThrow(new ResourceAlreadyExistException("User", "email", "existing@test.com"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with email: 'existing@test.com'"));
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .anyRequest().authenticated()
                    )
                    .build();
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}
