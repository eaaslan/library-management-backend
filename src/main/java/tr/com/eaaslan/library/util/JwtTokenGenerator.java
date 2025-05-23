package tr.com.eaaslan.library.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("dev")
public class JwtTokenGenerator {

    @Value("${spring.jwt.secret:defaultsecretkeyforlibraryapplicationdevelopment}")
    private String secretKey;

    // 10 years expiration - effectively permanent for testing
    private static final long PERMANENT_TOKEN_EXPIRATION = 10 * 365 * 24 * 60 * 60 * 1000L;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JwtTokenGenerator.class);

    @Bean
    public CommandLineRunner generateTestTokens() {
        return args -> {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

            // Generate Admin token
            String adminToken = generatePermanentToken(key, "admin@library.com", "ROLE_ADMIN");
            log.info("\n==================================================");
            log.info("PERMANENT ADMIN TOKEN FOR TESTING:");
            log.info(adminToken);

            // Generate Librarian token
            String librarianToken = generatePermanentToken(key, "librarian@library.com", "ROLE_LIBRARIAN");
            log.info("\nPERMANENT LIBRARIAN TOKEN FOR TESTING:");
            log.info(librarianToken);
            log.info("==================================================\n");

            // Generate patron token
            String patronToken = generatePermanentToken(key, "patron@library.com", "ROLE_PATRON");
            log.info("\nPERMANENT PATRON TOKEN FOR TESTING:");
            log.info(patronToken);
            log.info("==================================================\n");
            // Generate patron token
            String patron2Token = generatePermanentToken(key, "patron2@library.com", "ROLE_PATRON");
            log.info("\nPERMANENT PATRON2 TOKEN FOR TESTING:");
            log.info(patron2Token);
            log.info("==================================================\n");
            //
            // Generate patron token
            String suspended = generatePermanentToken(key, "suspended@library.com", "ROLE_PATRON");
            log.info("\nPERMANENT suspended@library.com TOKEN FOR TESTING:");
            log.info(suspended);
            log.info("==================================================\n");


            // Generate patron token
            String deleted = generatePermanentToken(key, "deleted@library.com", "ROLE_PATRON");
            log.info("\nPERMANENT deleted@library.com TOKEN FOR TESTING:");
            log.info(deleted);
            log.info("==================================================\n");

            // Generate pre-patron token
            String patron3 = generatePermanentToken(key, "patron3@library.com", "ROLE_PATRON");
            log.info("\nPERMANENT patron3@library.com TOKEN FOR TESTING:");
            log.info(patron3);
            log.info("==================================================\n");

            // Generate pre-patron token
            String patron4 = generatePermanentToken(key, "patron4@library.com", "ROLE_PATRON");
            log.info("\nPERMANENT patron4@library.com TOKEN FOR TESTING:");
            log.info(patron4);
            log.info("==================================================\n");


        };
    }

    private String generatePermanentToken(Key key, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + PERMANENT_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}