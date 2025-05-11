package tr.com.eaaslan.library.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tr.com.eaaslan.library.exception.ResourceAlreadyExistException;
import tr.com.eaaslan.library.exception.ResourceNotFoundException;
import tr.com.eaaslan.library.model.*;
import tr.com.eaaslan.library.model.dto.user.*;
import tr.com.eaaslan.library.model.mapper.UserMapper;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Use lenient strictness for all tests
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;
    private User testUser;
    private User adminUser;
    private User librarianUser;
    private UserResponse testUserResponse;
    private UserCreateRequest testUserCreateRequest;
    private UserUpdateRequest testUserUpdateRequest;
    private UserUpdateResponse testUserUpdateResponse;
    private LibrarianCreateResponse librarianCreateResponse;
    private List<User> testUsers;
    private Page<User> userPage;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("05501234567")
                .role(UserRole.PATRON)
                .status(UserStatus.ACTIVE)
                .maxAllowedBorrows(3)
                .build();
        testUser.setId(1L);

        adminUser = User.builder()
                .email("admin@example.com")
                .password("encodedPassword")
                .firstName("Admin")
                .lastName("User")
                .phoneNumber("05501234568")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        adminUser.setId(2L);

        librarianUser = User.builder()
                .email("librarian@example.com")
                .password("encodedPassword")
                .firstName("Librarian")
                .lastName("User")
                .phoneNumber("05501234569")
                .role(UserRole.LIBRARIAN)
                .status(UserStatus.ACTIVE)
                .build();
        librarianUser.setId(3L);

        testUserResponse = new UserResponse(
                1L,
                "test@example.com",
                "John",
                "Doe",
                "05501234567",
                "PATRON",
                "ACTIVE",
                3,
                LocalDateTime.now(),
                "system",
                false
        );

        testUserUpdateResponse = new UserUpdateResponse(
                1L,
                "test@example.com",
                "John",
                "Doe",
                "05501234567",
                "PATRON",
                "ACTIVE",
                3,
                LocalDateTime.now(),
                "system",
                false,
                LocalDateTime.now(),
                "system"
        );

        librarianCreateResponse = new LibrarianCreateResponse(
                3L,
                "librarian@example.com",
                "Librarian",
                "User",
                "05501234569",
                "LIBRARIAN",
                LocalDateTime.now(),
                "system"
        );

        testUserCreateRequest = new UserCreateRequest(
                "librarian@example.com", // Changed to match what implementation expects
                "password123",
                "Librarian",
                "User",
                "05501234569",
                "Test Address"
        );

        testUserUpdateRequest = new UserUpdateRequest(
                "UpdatedFirstName",
                "UpdatedLastName",
                "05509876543"
        );

        testUsers = List.of(testUser, adminUser, librarianUser);
        userPage = new PageImpl<>(testUsers);

        // Setup SecurityContextHolder mock for all tests
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        // Setup default borrowing repository behavior
        when(borrowingRepository.findByUserIdAndStatus(anyLong(), any(BorrowingStatus.class), any(Pageable.class)))
                .thenReturn(Page.empty());
    }

    @AfterEach
    void tearDown() {
        if (securityContextHolderMock != null) {
            securityContextHolderMock.close();
        }
    }

    @Test
    @DisplayName("Should create a librarian user successfully")
    void shouldCreateLibrarianUser() {
        // Arrange
        when(userRepository.existsByEmail(testUserCreateRequest.email())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(testUserCreateRequest.phoneNumber())).thenReturn(false);
        when(userMapper.toEntity(any(UserCreateRequest.class))).thenReturn(librarianUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(librarianUser);
        when(userMapper.toLibrarianResponse(any(User.class))).thenReturn(librarianCreateResponse);

        // Act
        LibrarianCreateResponse response = userService.createLibrarianUser(testUserCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(librarianCreateResponse.email(), response.email());
        assertEquals(librarianCreateResponse.firstName(), response.firstName());
        assertEquals("LIBRARIAN", response.role());

        // Verify
        verify(userRepository).existsByEmail(testUserCreateRequest.email());
        verify(userRepository).existsByPhoneNumber(testUserCreateRequest.phoneNumber());
        verify(userMapper).toEntity(testUserCreateRequest);
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toLibrarianResponse(any(User.class));
    }

    @Test
    @DisplayName("Should create a patron user successfully")
    void shouldCreatePatronUser() {
        // Arrange
        UserCreateRequest patronRequest = new UserCreateRequest(
                "patron@example.com",
                "password123",
                "Patron",
                "User",
                "05501234570",
                "Test Address"
        );

        User patronUser = User.builder()
                .email("patron@example.com")
                .password("encodedPassword")
                .firstName("Patron")
                .lastName("User")
                .phoneNumber("05501234570")
                .role(UserRole.PATRON)
                .status(UserStatus.PENDING)
                .build();

        UserResponse patronResponse = new UserResponse(
                4L,
                "patron@example.com",
                "Patron",
                "User",
                "05501234570",
                "PATRON",
                "PENDING",
                3,
                LocalDateTime.now(),
                "system",
                false
        );

        when(userRepository.existsByEmail(patronRequest.email())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(patronRequest.phoneNumber())).thenReturn(false);
        when(userMapper.toEntity(patronRequest)).thenReturn(patronUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(patronUser);
        when(userMapper.toResponse(patronUser)).thenReturn(patronResponse);

        // Act
        UserResponse response = userService.createPatronUser(patronRequest);

        // Assert
        assertNotNull(response);
        assertEquals(patronResponse.email(), response.email());
        assertEquals(patronResponse.firstName(), response.firstName());
        assertEquals("PATRON", response.role());

        // Verify
        verify(userRepository).existsByEmail(patronRequest.email());
        verify(userRepository).existsByPhoneNumber(patronRequest.phoneNumber());
        verify(userMapper).toEntity(patronRequest);
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with existing email")
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        // Arrange
        when(userRepository.existsByEmail(testUserCreateRequest.email())).thenReturn(true);
        when(userMapper.toEntity(testUserCreateRequest)).thenReturn(librarianUser);

        // Act & Assert
        assertThrows(ResourceAlreadyExistException.class, () -> userService.createPatronUser(testUserCreateRequest));

        // Verify
        verify(userRepository).existsByEmail(testUserCreateRequest.email());
        verify(userRepository, never()).existsByPhoneNumber(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with existing phone number")
    void shouldThrowExceptionWhenCreatingUserWithExistingPhoneNumber() {
        // Arrange
        when(userRepository.existsByEmail(testUserCreateRequest.email())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(testUserCreateRequest.phoneNumber())).thenReturn(true);
        when(userMapper.toEntity(testUserCreateRequest)).thenReturn(librarianUser); // Use librarian user to match phone

        // Act & Assert
        assertThrows(ResourceAlreadyExistException.class, () -> userService.createPatronUser(testUserCreateRequest));

        // Verify
        verify(userRepository).existsByEmail(testUserCreateRequest.email());
        verify(userRepository).existsByPhoneNumber(testUserCreateRequest.phoneNumber());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserById() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        UserResponse response = userService.getUserById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(testUserResponse.id(), response.id());
        assertEquals(testUserResponse.email(), response.email());

        // Verify
        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));

        // Verify
        verify(userRepository).findById(999L);
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Should get all active users")
    void shouldGetAllActiveUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        when(userRepository.findByDeletedFalse(pageable)).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        Page<UserResponse> response = userService.getAllActiveUsers(0, 10, "id");

        // Assert
        assertNotNull(response);
        assertEquals(testUsers.size(), response.getTotalElements());

        // Verify
        verify(userRepository).findByDeletedFalse(pageable);
        verify(userMapper, times(testUsers.size())).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Should get all users including deleted")
    void shouldGetAllUsersIncludingDeleted() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        Page<UserResponse> response = userService.getAllUsersIncludingDeleted(0, 10, "id");

        // Assert
        assertNotNull(response);
        assertEquals(testUsers.size(), response.getTotalElements());

        // Verify
        verify(userRepository).findAll(pageable);
        verify(userMapper, times(testUsers.size())).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUpdateResponse(testUser)).thenReturn(testUserUpdateResponse);
        doNothing().when(userMapper).updateEntity(any(UserUpdateRequest.class), any(User.class));

        // Act
        UserUpdateResponse response = userService.updateUser(1L, testUserUpdateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUserUpdateResponse.id(), response.id());

        // Verify
        verify(userRepository).findById(1L);
        verify(userMapper).updateEntity(testUserUpdateRequest, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).toUpdateResponse(testUser);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(999L, testUserUpdateRequest));

        // Verify
        verify(userRepository).findById(999L);
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete user successfully")
    void shouldSoftDeleteUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Mock the borrowing repository to return an empty page
        when(borrowingRepository.findByUserIdAndStatus(
                eq(1L), eq(BorrowingStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(Page.empty());

        // Act
        UserResponse response = userService.deleteUser(1L, "admin");

        // Assert
        assertNotNull(response);
        assertEquals(testUserResponse.id(), response.id());

        // Verify
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verify(userMapper).toResponse(testUser);

        // Additional verification for proper deletion
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.isDeleted());
        assertNotNull(savedUser.getDeletedAt());
        assertEquals("admin", savedUser.getDeletedBy());
    }

    @Test
    @DisplayName("Should hard delete user successfully")
    void shouldHardDeleteUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Mock any active borrowings that might be checked
        when(borrowingRepository.findByUserIdAndStatus(
                eq(1L), eq(BorrowingStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(Page.empty());

        // Act
        UserResponse response = userService.hardDeleteUser(1L, "admin");

        // Assert
        assertNotNull(response);
        assertEquals(testUserResponse.id(), response.id());

        // Verify
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Should update user status successfully")
    void shouldUpdateUserStatus() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUpdateResponse(testUser)).thenReturn(testUserUpdateResponse);

        // Act
        UserUpdateResponse response = userService.updateUserStatus(1L, "SUSPENDED");

        // Assert
        assertNotNull(response);
        assertEquals(testUserUpdateResponse.id(), response.id());

        // Verify
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verify(userMapper).toUpdateResponse(testUser);

        // Check that status was updated
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(UserStatus.SUSPENDED, savedUser.getStatus());
    }

    // Other tests remain the same...
}