package tr.com.eaaslan.library.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tr.com.eaaslan.library.model.Borrowing;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.util.Optional;

@Service
public class SecurityService {

    private final UserRepository userRepository;
    private final BorrowingRepository borrowingRepository;

    public SecurityService(UserRepository userRepository, BorrowingRepository borrowingRepository) {
        this.userRepository = userRepository;
        this.borrowingRepository = borrowingRepository;
    }

    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUserEmail = authentication.getName();
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(currentUserEmail))
                .orElse(false);
    }

    public boolean isCurrentUserBorrowing(Long borrowingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof LibraryUserDetails userDetails)) {
            return false;
        }

        Optional<User> user = userRepository.findByEmail(userDetails.getUsername());

        if (user.isEmpty()) {
            return false;
        }

        Optional<Borrowing> borrowing = borrowingRepository.findById(borrowingId);
        return borrowing.isPresent() && borrowing.get().getUser().getId().equals(user.get().getId());
    }
}