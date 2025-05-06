
package tr.com.eaaslan.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserRole;
import tr.com.eaaslan.library.model.UserStatus;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountMaintenanceService {

    private final UserRepository userRepository;
    private final BorrowingRepository borrowingRepository;

    /**
     * Check for inactive accounts and mark them as deleted.
     * Runs weekly on Sunday at 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * 0")
    @Transactional
    public void handleInactiveAccounts() {
        log.info("Running inactive accounts check");
        LocalDate now = LocalDate.now();
        LocalDate oneMonthAgo = now.minusDays(30);

        List<User> activeUsers = userRepository.findAllByStatus(UserStatus.ACTIVE);
        List<User> usersToDelete = new ArrayList<>();

        for (User user : activeUsers) {
            // Skip admin and librarian accounts
            if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.LIBRARIAN) {
                continue;
            }

            // Check latest activity date
            LocalDate lastActivity = borrowingRepository.findLatestActivityDateByUserId(user.getId());

            // If no activity found or last activity older than one month
            if (lastActivity == null || lastActivity.isBefore(oneMonthAgo)) {
                user.setStatus(UserStatus.DELETED);
                user.setDeleted(true);
                user.setDeletedAt(LocalDateTime.now());
                user.setDeletedBy("system");
                usersToDelete.add(user);

                log.info("User marked as deleted due to inactivity: {}", user.getEmail());
                // notificationService.sendAccountDeletionNotification(user);
            }
        }

        if (!usersToDelete.isEmpty()) {
            userRepository.saveAll(usersToDelete);
        }

        log.info("Inactive accounts check complete: {} accounts deleted", usersToDelete.size());
    }
}