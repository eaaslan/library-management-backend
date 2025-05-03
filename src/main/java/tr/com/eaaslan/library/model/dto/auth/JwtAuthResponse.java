package tr.com.eaaslan.library.model.dto.auth;

public record JwtAuthResponse(
        String token,
        String tokenType,
        String email,
        String role,
        Long expiresIn
) {
}