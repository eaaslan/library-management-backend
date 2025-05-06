package tr.com.eaaslan.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserStatus;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyService {

    private final UserRepository userRepository;
    private final BorrowingRepository borrowingRepository;

    /**
     * Check for users with 3+ late returns in the last month and apply suspensions.
     * Runs daily at 1:00 AM.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void applyLatePenalties() {
        log.info("Running late penalties check");
        LocalDate now = LocalDate.now();
        LocalDate oneMonthAgo = now.minusDays(30);

        // Check active users for late returns
        List<User> activeUsers = userRepository.findAllByStatus(UserStatus.ACTIVE);
        int suspendedCount = 0;

        for (User user : activeUsers) {
            // Count late returns in the last month
            long lateReturnsCount = borrowingRepository.countByUserIdAndReturnedLateAndReturnDateBetween(
                    user.getId(), true, oneMonthAgo, now);

            if (lateReturnsCount >= 3) {
                // Apply suspension for 2 weeks
                user.setStatus(UserStatus.SUSPENDED);
                user.setSuspensionEndDate(now.plusDays(14));
                userRepository.save(user);

                log.info("User suspended for 2 weeks due to excessive late returns: {}", user.getEmail());
                suspendedCount++;

                // In a real system, you would send a notification here
                // notificationService.sendSuspensionNotification(user, "late returns", 14);
            }
        }

        log.info("Late penalty check complete: {} users suspended", suspendedCount);
    }

    /**
     * Check for expired suspensions and restore user status.
     * Runs daily at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkSuspensionExpirations() {
        log.info("Checking for expired suspensions");
        LocalDate today = LocalDate.now();

        List<User> suspendedUsers = userRepository.findByStatusAndSuspensionEndDateBefore(
                UserStatus.SUSPENDED, today);

        for (User user : suspendedUsers) {
            user.setStatus(UserStatus.ACTIVE);
            user.setSuspensionEndDate(null);
            userRepository.save(user);

            log.info("User suspension expired, status restored to ACTIVE: {}", user.getEmail());
            // notificationService.sendStatusRestorationNotification(user);
        }

        log.info("Suspension expiration check complete: {} users restored", suspendedUsers.size());
    }
}