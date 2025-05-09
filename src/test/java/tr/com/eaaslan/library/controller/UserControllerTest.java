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
import tr.com.eaaslan.library.config.WebConfig;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.security.JwtAuthenticationEntryPoint;
import tr.com.eaaslan.library.security.JwtAuthenticationFilter;
import tr.com.eaaslan.library.security.JwtUtil;
import tr.com.eaaslan.library.security.SecurityService;
import tr.com.eaaslan.library.service.UserService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({UserControllerTest.TestConfig.class, UserControllerTest.TestSecurityConfig.class, WebConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

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
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public SecurityService securityService() {
            return mock(SecurityService.class);
        }
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth ->
                            auth.anyRequest().permitAll() // Test için tüm isteklere izin ver
                    )
                    .build();
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Kullanıcı oluşturma başarılı olmalı")
    void createUser_Success() throws Exception {
        // Test verileri
        UserCreateRequest createRequest = new UserCreateRequest(
                "test@example.com", "password123", "Test", "User", "05501234567", "Test Address");

        UserResponse userResponse = new UserResponse(
                1L, "test@example.com", "Test", "User", "05501234567",
                "PATRON", "ACTIVE", 3, LocalDateTime.now(), "system");

        // Mock davranışı
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userResponse);

        // API isteği ve doğrulama
        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print()) // Tüm yanıtı ve isteği yazdır
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("Test")));
        // Servis çağrısını doğrula
        verify(userService).createUser(any(UserCreateRequest.class));
    }

//    @Test
//    @WithMockUser(roles = "ADMIN")
//    @DisplayName("Tüm kullanıcıları getirme başarılı olmalı")
//    void getAllUsers_Success() throws Exception {
//        // Test verileri hazırlama ve servis davranışı belirleme
//        // ...
//
//        // API isteği ve doğrulama
//        mockMvc.perform(get("/api/v1/users")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .param("sortBy", "id"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content", hasSize(1)))
//                .andExpect(jsonPath("$.content[0].email", is("test@example.com")));
//    }
}