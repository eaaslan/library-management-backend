package tr.com.eaaslan.library.config;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import tr.com.eaaslan.library.model.dto.ErrorResponse;
import tr.com.eaaslan.library.model.dto.book.BookResponse;
import tr.com.eaaslan.library.model.dto.auth.JwtAuthResponse;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingResponse;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.util.ExampleProviders;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Contains custom annotations for commonly used API response patterns.
 * These annotations can be applied to controller methods to standardize
 * OpenAPI/Swagger documentation.
 */
public final class ApiResponseAnnotations {

    private ApiResponseAnnotations() {
    }

    /**
     * Standard responses for login endpoint
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JwtAuthResponse.class),
                            examples = @ExampleObject(value = ExampleProviders.LOGIN_SUCCESS_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ExampleProviders.AUTH_FAILURE_EXAMPLE)
                    )
            )
    })
    public @interface LoginResponses {
    }

    /**
     * Standard responses for user registration endpoint
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ExampleProviders.VALIDATION_ERROR_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public @interface RegistrationResponses {
    }

    /**
     * Standard responses for book creation endpoint
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Book successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookResponse.class),
                            examples = @ExampleObject(value = ExampleProviders.BOOK_RESPONSE_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ExampleProviders.VALIDATION_ERROR_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Book with same ISBN already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public @interface BookCreationResponses {
    }

    /**
     * Standard responses for retrieving a single resource by ID
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ExampleProviders.NOT_FOUND_EXAMPLE)
                    )
            )
    })
    public @interface GetByIdResponses {
    }

    /**
     * Standard responses for updating a resource
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource successfully updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ExampleProviders.VALIDATION_ERROR_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ExampleProviders.NOT_FOUND_EXAMPLE)
                    )
            )
    })
    public @interface UpdateResponses {
    }

    /**
     * Standard responses for deleting a resource
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ExampleProviders.NOT_FOUND_EXAMPLE)
                    )
            )
    })
    public @interface DeleteResponses {
    }

    /**
     * Standard responses for borrowing a book
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Book successfully borrowed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BorrowingResponse.class),
                            examples = @ExampleObject(value = ExampleProviders.BORROWING_RESPONSE_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Book not available or user has reached borrowing limit",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public @interface BorrowingResponses {
    }
}