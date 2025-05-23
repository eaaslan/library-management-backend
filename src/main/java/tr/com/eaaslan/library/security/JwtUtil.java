package tr.com.eaaslan.library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import tr.com.eaaslan.library.service.BookServiceImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${spring.jwt.secret:}")
    private String configuredSecretKey;

    private String secretKey;

    @Value("${spring.jwt.expiration-ms:86400000}") // 24 hours by default
    private long jwtExpirationMs;

    private Key key;

    private final static Logger logger = LoggerFactory.getLogger(JwtUtil.class);


    @PostConstruct
    public void init() {

        try {
            Path secretPath = Paths.get("/run/secrets/jwt_secret");
            if (Files.exists(secretPath)) {
                secretKey = Files.readString(secretPath).trim();
                logger.info("JWT secret loaded from Docker secrets");
            } else if (!configuredSecretKey.isEmpty()) {

                secretKey = configuredSecretKey;
                logger.info("JWT secret loaded from configuration");
            } else {

                secretKey = "defaultsecretkeyforlibraryapplicationdevelopment";
                logger.warn("Using default JWT secret key. This is NOT recommended for production!");
            }
            this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        } catch (Exception e) {
            logger.error("Error loading JWT secret: {}", e.getMessage());
          
            secretKey = "defaultsecretkeyforlibraryapplicationdevelopment";
            this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        }
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userDetails.getUsername());
        claims.put("role", userDetails.getAuthorities().stream().findFirst().orElseThrow().getAuthority());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public long getExpirationTimeInSeconds(String token) {
        Date expiration = extractExpiration(token);
        long expirationTimeInMillis = expiration.getTime() - System.currentTimeMillis();
        return expirationTimeInMillis / 1000;
    }
}