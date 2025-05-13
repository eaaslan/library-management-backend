package tr.com.eaaslan.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.repository.UserRepository;
import tr.com.eaaslan.library.security.SecurityService;
import tr.com.eaaslan.library.service.UserService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class UserControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private SecurityService securityService;

    @BeforeEach
    void setUp() {

        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("User creation should be successful")
    void createUser_Success() throws Exception {

        UserCreateRequest createRequest = new UserCreateRequest(
                "test@example.com", "password123", "Test", "User", "05501234567", "Test Address");


        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should not create user with existing email")
    void shouldNotCreateUserWithExistingEmail() throws Exception {

        UserCreateRequest firstUser = new UserCreateRequest(
                "duplicate@example.com", "password123", "First", "User", "05501234568", "Test Address");

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        UserCreateRequest duplicateUser = new UserCreateRequest(
                "duplicate@example.com", "password456", "Second", "User", "05501234569", "Another Address");

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with email: 'duplicate@example.com'"));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("User with Librarian role should be able to create a librarian")
    void librarianShouldCreateLibrarianUser() throws Exception {

        UserCreateRequest createRequest = new UserCreateRequest(
                "librarian@example.com", "password123", "New", "Librarian", "05501234570", "Test Address");


        mockMvc.perform(post("/api/v1/users/librarians")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("librarian@example.com"))
                .andExpect(jsonPath("$.role").value("LIBRARIAN"));
    }

    @Test
    @WithMockUser(roles = "PATRON")
    @DisplayName("User with Patron role should not be able to create a librarian")
    void patronShouldNotCreateLibrarianUser() throws Exception {

        UserCreateRequest createRequest = new UserCreateRequest(
                "librarian2@example.com", "password123", "Blocked", "Librarian", "05501234571", "Test Address");

        mockMvc.perform(post("/api/v1/users/librarians")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}