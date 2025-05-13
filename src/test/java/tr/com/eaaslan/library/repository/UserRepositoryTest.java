package tr.com.eaaslan.library.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tr.com.eaaslan.library.config.TestJpaConfig;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserRole;
import tr.com.eaaslan.library.model.UserStatus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User admin;
    private User librarian;
    private User activePatron;
    private User suspendedPatron;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        admin = User.builder()
                .email("admin@library.com")
                .password("password")
                .firstName("Admin")
                .lastName("User")
                .phoneNumber("05501234567")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        librarian = User.builder()
                .email("librarian@library.com")
                .password("password")
                .firstName("Librarian")
                .lastName("User")
                .phoneNumber("05501234568")
                .role(UserRole.LIBRARIAN)
                .status(UserStatus.ACTIVE)
                .build();

        activePatron = User.builder()
                .email("patron@library.com")
                .password("password")
                .firstName("Patron")
                .lastName("User")
                .phoneNumber("05501234569")
                .role(UserRole.PATRON)
                .status(UserStatus.ACTIVE)
                .build();

        suspendedPatron = User.builder()
                .email("suspended@library.com")
                .password("password")
                .firstName("Suspended")
                .lastName("User")
                .phoneNumber("05501234570")
                .role(UserRole.PATRON)
                .status(UserStatus.SUSPENDED)
                .suspensionEndDate(LocalDate.now().plusDays(7))
                .build();

        userRepository.saveAll(Arrays.asList(admin, librarian, activePatron, suspendedPatron));
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        Optional<User> foundUser = userRepository.findByEmail("admin@library.com");

        assertTrue(foundUser.isPresent(), "User should be found by email");
        assertEquals(admin.getFirstName(), foundUser.get().getFirstName());
        assertEquals(admin.getRole(), foundUser.get().getRole());
    }

    @Test
    @DisplayName("Should find users by role")
    void shouldFindUsersByRole() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> adminUsers = userRepository.findByRole(UserRole.ADMIN, pageable);
        Page<User> patronUsers = userRepository.findByRole(UserRole.PATRON, pageable);

        assertEquals(1, adminUsers.getTotalElements(), "Should find one admin user");
        assertEquals(2, patronUsers.getTotalElements(), "Should find two patron users");
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        assertTrue(userRepository.existsByEmail("admin@library.com"), "Email should exist");
        assertFalse(userRepository.existsByEmail("nonexistent@library.com"), "Email should not exist");
    }

    @Test
    @DisplayName("Should find users with suspended status and past end date")
    void shouldFindUsersWithSuspendedStatusAndPastEndDate() {

        User pastSuspension = User.builder()
                .email("past@library.com")
                .password("password")
                .firstName("Past")
                .lastName("Suspension")
                .phoneNumber("05501234571")
                .role(UserRole.PATRON)
                .status(UserStatus.SUSPENDED)
                .suspensionEndDate(LocalDate.now().minusDays(1))
                .build();
        userRepository.save(pastSuspension);

        List<User> usersToRestore = userRepository.findByStatusAndSuspensionEndDateBefore(
                UserStatus.SUSPENDED, LocalDate.now());

        assertEquals(1, usersToRestore.size(), "Should find one user with expired suspension");
        assertEquals("past@library.com", usersToRestore.getFirst().getEmail());
    }

    @Test
    @DisplayName("Should search users by name")
    void shouldSearchUsersByName() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> adminSearch = userRepository.searchByName("Admin", pageable);
        Page<User> suspendedSearch = userRepository.searchByName("Suspended", pageable);
        Page<User> userSearch = userRepository.searchByName("User", pageable);

        assertEquals(1, adminSearch.getTotalElements(), "Should find one user with 'Admin' in name");
        assertEquals(1, suspendedSearch.getTotalElements(), "Should find one user with 'Suspended' in name");
        assertEquals(4, userSearch.getTotalElements(), "Should find all users with 'User' in lastname");
    }

    @Test
    @DisplayName("Should search users by status")
    void shouldSearchUsersByStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> activeSearch = userRepository.findAllByStatus(UserStatus.ACTIVE, pageable);
        Page<User> suspendedSearch = userRepository.findAllByStatus(UserStatus.SUSPENDED, pageable);

        assertEquals(3, activeSearch.getTotalElements(), "Should find all active users");
        assertEquals(1, suspendedSearch.getTotalElements(), "Should find one suspended user");
    }
}