package tr.com.eaaslan.library.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserRole;
import tr.com.eaaslan.library.model.UserStatus;
import tr.com.eaaslan.library.repository.UserRepository;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Check if admin already exists
        if (!userRepository.existsByEmail("admin@library.com")) {
            log.info("Admin user not found. Creating admin user...");
            User adminUser = User.builder()
                    .email("admin@library.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .phoneNumber("05501234567")
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(adminUser);
        }

        // Check if librarian already exists
        if (!userRepository.existsByEmail("librarian@library.com")) {
            log.info("Librarian user not found. Creating librarian user...");
            User librarianUser = User.builder()
                    .email("librarian@library.com")
                    .password(passwordEncoder.encode("librarian123"))
                    .firstName("Librarian")
                    .lastName("User")
                    .phoneNumber("05551234567")
                    .role(UserRole.LIBRARIAN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(librarianUser);
        }
    }
}