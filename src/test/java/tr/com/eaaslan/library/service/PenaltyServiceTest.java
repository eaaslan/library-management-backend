package tr.com.eaaslan.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserStatus;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenaltyServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @InjectMocks
    private PenaltyService penaltyService;

    private User userWithManyLateReturns;
    private User userWithFewLateReturns;
    private User suspendedUser;

    @BeforeEach
    void setUp() {
        // User with 3+ late returns
        userWithManyLateReturns = User.builder()

                .email("manylate@example.com")
                .status(UserStatus.ACTIVE)
                .build();
        userWithManyLateReturns.setId(1L);

        // User with fewer late returns
        userWithFewLateReturns = User.builder()
                .email("fewlate@example.com")
                .status(UserStatus.ACTIVE)
                .build();
        userWithFewLateReturns.setId(2L);

        // User with suspension that should be expired
        suspendedUser = User.builder()
                .email("suspended@example.com")
                .status(UserStatus.SUSPENDED)
                .suspensionEndDate(LocalDate.now().minusDays(1))
                .build();
        suspendedUser.setId(3L);
    }

    @Test
    @DisplayName("Should suspend users with 3+ late returns")
    void shouldSuspendUsersWithThreeOrMoreLateReturns() {
        // Arrange
        when(userRepository.findAllByStatus(UserStatus.ACTIVE))
                .thenReturn(Arrays.asList(userWithManyLateReturns, userWithFewLateReturns));

        // User 1 has 3 late returns in the last month
        when(borrowingRepository.countByUserIdAndReturnedLateAndReturnDateBetween(
                eq(1L), eq(true), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(3L);

        // User 2 has only 1 late return in the last month
        when(borrowingRepository.countByUserIdAndReturnedLateAndReturnDateBetween(
                eq(2L), eq(true), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(1L);

        // Act
        penaltyService.applyLatePenalties();

        // Assert
        verify(userRepository).save(argThat(user ->
                user.getId().equals(1L) &&
                        user.getStatus() == UserStatus.SUSPENDED &&
                        user.getSuspensionEndDate() != null));

        // User 2 should not be suspended
        verify(userRepository, never()).save(argThat(user -> user.getId().equals(2L)));
    }

    @Test
    @DisplayName("Should restore users whose suspension has expired")
    void shouldRestoreUsersWhoseSuspensionHasExpired() {
        // Arrange
        when(userRepository.findByStatusAndSuspensionEndDateBefore(
                eq(UserStatus.SUSPENDED), any(LocalDate.class)))
                .thenReturn(List.of(suspendedUser));

        // Act
        penaltyService.checkSuspensionExpirations();

        // Assert
        verify(userRepository).save(argThat(user ->
                user.getId().equals(3L) &&
                        user.getStatus() == UserStatus.ACTIVE &&
                        user.getSuspensionEndDate() == null));
    }
}