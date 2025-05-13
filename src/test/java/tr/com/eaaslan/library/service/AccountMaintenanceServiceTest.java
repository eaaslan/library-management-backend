package tr.com.eaaslan.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserRole;
import tr.com.eaaslan.library.model.UserStatus;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountMaintenanceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @InjectMocks
    private AccountMaintenanceService accountMaintenanceService;

    private User activePatronWithRecentActivity;
    private User activePatronWithoutRecentActivity;
    private User adminUser;
    private User librarianUser;

    @BeforeEach
    void setUp() {

        activePatronWithRecentActivity = User.builder()

                .email("recent@example.com")
                .status(UserStatus.ACTIVE)
                .role(UserRole.PATRON)
                .build();

        activePatronWithRecentActivity.setId(1L);

        activePatronWithoutRecentActivity = User.builder()

                .email("inactive@example.com")
                .status(UserStatus.ACTIVE)
                .role(UserRole.PATRON)
                .build();

        activePatronWithoutRecentActivity.setId(2L);

        adminUser = User.builder()
                .email("admin@example.com")
                .status(UserStatus.ACTIVE)
                .role(UserRole.ADMIN)
                .build();

        adminUser.setId(3L);

        librarianUser = User.builder()
                .email("librarian@example.com")
                .status(UserStatus.ACTIVE)
                .role(UserRole.LIBRARIAN)
                .build();

        librarianUser.setId(4L);
    }

    @Test
    @DisplayName("Should mark inactive patron accounts as deleted")
    void shouldMarkInactivePatronAccountsAsDeleted() {

        when(userRepository.findAllByStatus(UserStatus.ACTIVE))
                .thenReturn(Arrays.asList(
                        activePatronWithRecentActivity,
                        activePatronWithoutRecentActivity,
                        adminUser,
                        librarianUser));

        when(borrowingRepository.findLatestActivityDateByUserId(1L))
                .thenReturn(LocalDate.now().minusDays(15)); // Active within last month

        when(borrowingRepository.findLatestActivityDateByUserId(2L))
                .thenReturn(LocalDate.now().minusDays(45)); // Not active for more than a month

        accountMaintenanceService.handleInactiveAccounts();

        verify(userRepository, never()).save(argThat(user -> user.getId().equals(1L) && user.isDeleted()));

        verify(userRepository, never()).save(argThat(user ->
                (user.getId().equals(3L) || user.getId().equals(4L)) && user.isDeleted()));

        verify(userRepository).saveAll(argThat(users -> {
            List<User> userList = new ArrayList<>();
            users.forEach(userList::add);

            return userList.size() == 1 &&
                    userList.getFirst().getId().equals(2L) &&
                    userList.getFirst().isDeleted() &&
                    userList.getFirst().getStatus() == UserStatus.DELETED &&
                    userList.getFirst().getDeletedAt() != null &&
                    "system".equals(userList.getFirst().getDeletedBy());
        }));
    }
}
