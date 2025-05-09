package tr.com.eaaslan.library.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import tr.com.eaaslan.library.exception.ResourceAlreadyExistException;
import tr.com.eaaslan.library.exception.ResourceNotFoundException;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserRole;
import tr.com.eaaslan.library.model.UserStatus;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.model.dto.user.UserUpdateRequest;
import tr.com.eaaslan.library.model.mapper.UserMapper;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse testUserResponse;
    private UserCreateRequest testUserCreateRequest;
    private UserUpdateRequest testUserUpdateRequest;
    private List<User> testUsers;

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
                "system"
        );

        testUserCreateRequest = new UserCreateRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                "05501234567",
                "Test Address"
        );

        testUserUpdateRequest = new UserUpdateRequest(
                "UpdatedFirstName",
                "UpdatedLastName",
                "05509876543"
        );

        testUsers = List.of(testUser);
    }

    @Test
    @DisplayName("Create user - Success case")
    void createUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserCreateRequest.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        UserResponse actual = userService.createUser(testUserCreateRequest);

        // Assert
        assertNotNull(actual);
        assertEquals(testUserResponse.email(), actual.email());
        assertEquals(testUserResponse.firstName(), actual.firstName());

        // Verify
        verify(userRepository).existsByEmail(testUserCreateRequest.email());
        verify(userRepository).existsByPhoneNumber(testUserCreateRequest.phoneNumber());
        verify(userMapper).toEntity(testUserCreateRequest);
        verify(passwordEncoder).encode(testUser.getPassword());
        verify(userRepository).save(testUser);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Create user - Email already exists")
    void createUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(userMapper.toEntity(any(UserCreateRequest.class))).thenReturn(testUser);

        // Act & Assert
        assertThrows(ResourceAlreadyExistException.class, () -> userService.createUser(testUserCreateRequest));

        // Verify
        verify(userRepository).existsByEmail(testUserCreateRequest.email());
        verify(userRepository, never()).existsByPhoneNumber(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Create user - Phone number already exists")
    void createUser_PhoneNumberAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(true);
        when(userMapper.toEntity(any(UserCreateRequest.class))).thenReturn(testUser);

        // Act & Assert
        assertThrows(ResourceAlreadyExistException.class, () -> userService.createUser(testUserCreateRequest));

        // Verify
        verify(userRepository).existsByEmail(testUserCreateRequest.email());
        verify(userRepository).existsByPhoneNumber(testUser.getPhoneNumber());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Get user by ID - Success case")
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        UserResponse actual = userService.getUserById(1L);

        // Assert
        assertNotNull(actual);
        assertEquals(testUserResponse.id(), actual.id());
        assertEquals(testUserResponse.email(), actual.email());

        // Verify
        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Get user by ID - Not found")
    void getUserById_NotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));

        // Verify
        verify(userRepository).findById(1L);
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Get all users - Success case")
    void getAllUsers_Success() {
        // Arrange
        Page<User> userPage = new PageImpl<>(testUsers);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        Page<UserResponse> actual = userService.getAllUsers(0, 10, "id");

        // Assert
        assertNotNull(actual);
        assertEquals(1, actual.getTotalElements());
        assertEquals(testUserResponse.email(), actual.getContent().get(0).email());

        // Verify
        verify(userRepository).findAll(pageable);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Update user - Success case")
    void updateUser_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        UserResponse actual = userService.updateUser(1L, testUserUpdateRequest);

        // Assert
        assertNotNull(actual);
        assertEquals(testUserResponse.id(), actual.id());

        // Verify
        verify(userRepository).findById(1L);
        verify(userMapper).updateEntity(testUserUpdateRequest, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Update user - User not found")
    void updateUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, testUserUpdateRequest));

        // Verify
        verify(userRepository).findById(1L);
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete user - Success case")
    void deleteUser_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        UserResponse actual = userService.deleteUser(1L);

        // Assert
        assertNotNull(actual);
        assertEquals(testUserResponse.id(), actual.id());

        // Verify
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verify(userMapper).toResponse(testUser);

        // Additional verification for deleted status
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.isDeleted());
        assertNotNull(savedUser.getDeletedAt());
        assertEquals("SYSTEM", savedUser.getDeletedBy());
    }

    @Test
    @DisplayName("Delete user - User not found")
    void deleteUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));

        // Verify
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update user status - Success case")
    void updateUserStatus_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        UserResponse actual = userService.updateUserStatus(1L, "SUSPENDED");

        // Assert
        assertNotNull(actual);

        // Verify
        verify(userRepository).findById(1L);

        // Check that status was updated
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(UserStatus.SUSPENDED, savedUser.getStatus());

        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Update user status - User not found")
    void updateUserStatus_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserStatus(1L, "SUSPENDED"));

        // Verify
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Search users by name - Success case")
    void searchByName_Success() {
        // Arrange
        Page<User> userPage = new PageImpl<>(testUsers);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.searchByName(anyString(), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        Page<UserResponse> actual = userService.searchByName("John", 0, 10);

        // Assert
        assertNotNull(actual);
        assertEquals(1, actual.getTotalElements());
        assertEquals(testUserResponse.firstName(), actual.getContent().get(0).firstName());

        // Verify
        verify(userRepository).searchByName("John", pageable);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Get users by role - Success case")
    void getUsersByRole_Success() {
        // Arrange
        Page<User> userPage = new PageImpl<>(testUsers);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByRole(any(UserRole.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        Page<UserResponse> actual = userService.getUsersByRole("PATRON", 0, 10);

        // Assert
        assertNotNull(actual);
        assertEquals(1, actual.getTotalElements());
        assertEquals(testUserResponse.role(), actual.getContent().get(0).role());

        // Verify
        verify(userRepository).findByRole(UserRole.PATRON, pageable);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Get active users - Success case")
    void getActiveUsers_Success() {
        // Arrange
        Page<User> userPage = new PageImpl<>(testUsers);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAllByStatus(any(UserStatus.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // Act
        Page<UserResponse> actual = userService.getActiveUsers(0, 10);

        // Assert
        assertNotNull(actual);
        assertEquals(1, actual.getTotalElements());
        assertEquals(testUserResponse.status(), actual.getContent().getFirst().status());

        // Verify
        verify(userRepository).findAllByStatus(UserStatus.ACTIVE, pageable);
        verify(userMapper).toResponse(testUser);
    }
}