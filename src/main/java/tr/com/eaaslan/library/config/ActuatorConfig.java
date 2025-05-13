package tr.com.eaaslan.library.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import tr.com.eaaslan.library.repository.BookRepository;
import tr.com.eaaslan.library.repository.UserRepository;
import tr.com.eaaslan.library.repository.BorrowingRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Actuator configuration for health checks and info endpoints.
 * This class provides custom health indicators and info contributors
 * specifically tailored for the Library Management System.
 */
@Configuration
public class ActuatorConfig {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;
    private final Optional<BuildProperties> buildProperties;

    public ActuatorConfig(JdbcTemplate jdbcTemplate,
                          UserRepository userRepository,
                          BookRepository bookRepository,
                          BorrowingRepository borrowingRepository,
                          Optional<BuildProperties> buildProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.borrowingRepository = borrowingRepository;
        this.buildProperties = buildProperties;
    }

    /**
     * Enhanced database health indicator that performs multiple checks.
     * This health indicator not only checks connectivity but also validates
     * that our main tables are accessible.
     */
    @Bean
    public HealthIndicator databaseHealthIndicator() {
        return () -> {
            try {
                // Basic connectivity check
                Integer connectivityResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

                // Check if main tables are accessible
                long userCount = userRepository.count();
                long bookCount = bookRepository.count();
                long borrowingCount = borrowingRepository.count();

                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("connectivity", "OK")
                        .withDetail("connection_check_result", connectivityResult)
                        .withDetail("tables_accessible", true)
                        .withDetail("user_count", userCount)
                        .withDetail("book_count", bookCount)
                        .withDetail("borrowing_count", borrowingCount)
                        .withDetail("checked_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("connectivity", "FAILED")
                        .withDetail("error", e.getMessage())
                        .withDetail("error_type", e.getClass().getSimpleName())
                        .withDetail("checked_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .build();
            }
        };
    }

    /**
     * Enhanced info contributor that provides comprehensive application information.
     * This includes build information, environment details, and system statistics.
     */
    @Bean
    public InfoContributor libraryInfoContributor() {
        return builder -> {
            // Application details
            builder.withDetail("application", Map.of(
                    "name", "Library Management System",
                    "description", "Spring Boot Library Management API with Book Borrowing System",
                    "version", buildProperties.map(BuildProperties::getVersion).orElse("1.0.0"),
                    "developer", "eaaslan"
            ));

            // Project information
            builder.withDetail("project", Map.of(
                    "bootcamp", "Patika.dev & Getir Java Spring Boot Bootcamp",
                    "type", "Final Project (Bitirme Projesi)",
                    "completion_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            ));

            // Build information if available
            buildProperties.ifPresent(props ->
                    builder.withDetail("build", Map.of(
                            "time", props.getTime() != null ? props.getTime().toString() : "Unknown",
                            "artifact", props.getArtifact() != null ? props.getArtifact() : "library-management-backend",
                            "group", props.getGroup() != null ? props.getGroup() : "tr.com.eaaslan"
                    ))
            );

            // Technical stack information
            builder.withDetail("tech_stack", Map.of(
                    "java_version", System.getProperty("java.version"),
                    "spring_boot_version", buildProperties.map(props -> props.get("spring-boot.version")).orElse("Unknown"),
                    "database", "PostgreSQL",
                    "authentication", "JWT with Spring Security",
                    "documentation", "OpenAPI 3.0 (Swagger)",
                    "monitoring", "Spring Boot Actuator"
            ));

            // System information
            builder.withDetail("system", Map.of(
                    "os_name", System.getProperty("os.name"),
                    "os_version", System.getProperty("os.version"),
                    "java_home", System.getProperty("java.home"),
                    "user_timezone", System.getProperty("user.timezone")
            ));
        };
    }

    /**
     * Custom health indicator for application-specific business logic.
     * This checks if the core business functionality is working correctly.
     */
    @Bean
    public HealthIndicator libraryBusinessHealthIndicator() {
        return () -> {
            try {
                // Check if we have at least some data in the system
                long userCount = userRepository.count();
                long bookCount = bookRepository.count();

                // Basic business logic check
                if (bookCount == 0) {
                    return Health.down()
                            .withDetail("business_status", "No books available in the system")
                            .withDetail("recommendation", "Add books to the library system")
                            .build();
                }

                // Check for system admin existence
                boolean hasAdmin = userRepository.findAll().stream()
                        .anyMatch(user -> user.getRole().name().equals("ADMIN"));

                return Health.up()
                        .withDetail("business_status", "System operational")
                        .withDetail("total_users", userCount)
                        .withDetail("total_books", bookCount)
                        .withDetail("admin_exists", hasAdmin)
                        .withDetail("system_ready", userCount > 0 && bookCount > 0)
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("business_status", "Business logic check failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
}