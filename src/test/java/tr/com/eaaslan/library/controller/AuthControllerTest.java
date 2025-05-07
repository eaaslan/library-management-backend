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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.eaaslan.library.model.dto.auth.LoginRequest;
import tr.com.eaaslan.library.security.JwtAuthenticationEntryPoint;
import tr.com.eaaslan.library.security.JwtAuthenticationFilter;
import tr.com.eaaslan.library.security.JwtUtil;
import tr.com.eaaslan.library.security.LibraryUserDetails;
import tr.com.eaaslan.library.service.UserService;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({AuthControllerTest.TestConfig.class, AuthControllerTest.TestSecurityConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
            JwtUtil jwtUtil = mock(JwtUtil.class);
            when(jwtUtil.generateToken(any(Authentication.class))).thenReturn("mock.jwt.token");
            when(jwtUtil.getExpirationTimeInSeconds("mock.jwt.token")).thenReturn(86400L);
            return jwtUtil;
        }

        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public AuthenticationManager authenticationManager() {
            // First create and set up our mocks
            Authentication auth = mock(Authentication.class);
            LibraryUserDetails userDetails = mock(LibraryUserDetails.class);

            // Set up the LibraryUserDetails mock with proper method responses
            when(userDetails.getUsername()).thenReturn("admin@library.com");

            // Create a single authority and use doReturn() instead of when().thenReturn()
            SimpleGrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
            doReturn(Collections.singletonList(adminAuthority)).when(userDetails).getAuthorities();

            // Complete the authentication chain
            when(auth.getPrincipal()).thenReturn(userDetails);

            // Create the AuthenticationManager mock
            AuthenticationManager authManager = mock(AuthenticationManager.class);
            when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);

            return authManager;
        }
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth ->
                            auth.requestMatchers("/api/v1/auth/**").permitAll()
                                    .anyRequest().authenticated()
                    )
                    .build();
        }
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@library.com", "admin123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("mock.jwt.token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.email", is("admin@library.com")))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$.expiresIn", is(86400)));
    }
}