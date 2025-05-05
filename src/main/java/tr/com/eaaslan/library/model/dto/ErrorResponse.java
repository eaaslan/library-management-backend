
package tr.com.eaaslan.library.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tr.com.eaaslan.library.util.ExampleProviders;

import java.time.LocalDateTime;

/**
 * Standardized error response for API errors.
 * Used to provide consistent error information across the API.
 */
@Schema(description = "Standard error response",
        example = ExampleProviders.AUTH_FAILURE_EXAMPLE)
public record ErrorResponse(
        @Schema(description = "HTTP status code", example = "401")
        int status,

        @Schema(description = "Error message", example = "Authentication failed: Invalid credentials")
        String message,

        @Schema(description = "Error timestamp", example = "2023-06-15T10:15:30")
        LocalDateTime timestamp,

        @Schema(description = "Request path that caused the error", example = "/api/v1/auth/login")
        String path
) {
    /**
     * Convenience constructor that automatically sets the current timestamp
     */
    public ErrorResponse(int status, String message, String path) {
        this(status, message, LocalDateTime.now(), path);
    }
}