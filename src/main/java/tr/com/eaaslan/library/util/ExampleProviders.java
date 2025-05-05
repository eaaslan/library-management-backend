package tr.com.eaaslan.library.util;

/**
 * Centralized storage for OpenAPI/Swagger example values.
 * This class contains all example JSON strings used in API documentation
 * to ensure consistency and avoid duplication.
 */
public final class ExampleProviders {

    private ExampleProviders() {
        // Private constructor to prevent instantiation
    }

    // Authentication examples
    public static final String LOGIN_SUCCESS_EXAMPLE =
            "{\"token\":\"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBsaWJyYXJ5LmNvbSJ9.signature\","
                    + "\"tokenType\":\"Bearer\","
                    + "\"email\":\"admin@library.com\","
                    + "\"role\":\"ROLE_ADMIN\","
                    + "\"expiresIn\":86400}";

    public static final String AUTH_FAILURE_EXAMPLE =
            "{\"status\":401,"
                    + "\"message\":\"Authentication failed: Invalid credentials\","
                    + "\"timestamp\":\"2023-06-15T10:15:30\","
                    + "\"path\":\"/api/v1/auth/login\"}";

    public static final String ADMIN_LOGIN_REQUEST_EXAMPLE =
            "{\"email\": \"admin@library.com\", \"password\": \"admin123\"}";

    public static final String LIBRARIAN_LOGIN_REQUEST_EXAMPLE =
            "{\"email\": \"librarian@library.com\", \"password\": \"librarian123\"}";

    public static final String PATRON_LOGIN_REQUEST_EXAMPLE =
            "{\"email\": \"patron@library.com\", \"password\": \"patron123\"}";

    // Book examples
    public static final String BOOK_CREATE_REQUEST_EXAMPLE =
            "{\"isbn\":\"9780132350884\","
                    + "\"title\":\"Clean Code\","
                    + "\"author\":\"Robert C. Martin\","
                    + "\"publicationYear\":2008,"
                    + "\"publisher\":\"Prentice Hall\","
                    + "\"genre\":\"SCIENCE\","
                    + "\"imageUrl\":\"http://images.example.com/covers/clean-code.jpg\","
                    + "\"description\":\"A handbook of agile software craftsmanship\","
                    + "\"quantity\":3}";

    public static final String BOOK_RESPONSE_EXAMPLE =
            "{\"id\":1,"
                    + "\"isbn\":\"9780132350884\","
                    + "\"title\":\"Clean Code\","
                    + "\"author\":\"Robert C. Martin\","
                    + "\"publicationYear\":2008,"
                    + "\"publisher\":\"Prentice Hall\","
                    + "\"genre\":\"SCIENCE\","
                    + "\"imageUrl\":\"http://images.example.com/covers/clean-code.jpg\","
                    + "\"description\":\"A handbook of agile software craftsmanship\","
                    + "\"quantity\":3,"
                    + "\"available\":true,"
                    + "\"createdAt\":\"2023-06-15T10:30:00\","
                    + "\"createdBy\":\"admin@library.com\"}";

    public static final String BOOK_UPDATE_REQUEST_EXAMPLE =
            "{\"title\":\"Clean Code: A Handbook of Agile Software Craftsmanship\","
                    + "\"description\":\"Updated description with more details\","
                    + "\"quantity\":5}";

    // Borrowing examples
    public static final String BORROWING_CREATE_REQUEST_EXAMPLE =
            "{\"bookId\":1,\"dueDate\":\"2023-07-15\"}";

    public static final String BORROWING_RESPONSE_EXAMPLE =
            "{\"id\":1,"
                    + "\"userId\":2,"
                    + "\"userEmail\":\"patron@library.com\","
                    + "\"userName\":\"Patron User\","
                    + "\"bookId\":1,"
                    + "\"bookTitle\":\"Clean Code\","
                    + "\"bookIsbn\":\"9780132350884\","
                    + "\"borrowDate\":\"2023-06-15T10:30:00\","
                    + "\"dueDate\":\"2023-07-15T10:30:00\","
                    + "\"returnDate\":null,"
                    + "\"status\":\"ACTIVE\","
                    + "\"isOverdue\":false,"
                    + "\"createdAt\":\"2023-06-15T10:30:00\","
                    + "\"createdBy\":\"librarian@library.com\"}";

    // Common error examples
    public static final String NOT_FOUND_EXAMPLE =
            "{\"status\":404,"
                    + "\"message\":\"Resource not found: Book with ID 999 not found\","
                    + "\"timestamp\":\"2023-06-15T10:30:00\","
                    + "\"path\":\"/api/v1/books/999\"}";

    public static final String VALIDATION_ERROR_EXAMPLE =
            "{\"status\":400,"
                    + "\"message\":\"Validation failed\","
                    + "\"timestamp\":\"2023-06-15T10:30:00\","
                    + "\"path\":\"/api/v1/books\","
                    + "\"errors\":["
                    + "  {\"field\":\"isbn\",\"message\":\"ISBN is required\"},"
                    + "  {\"field\":\"title\",\"message\":\"Title is required\"}"
                    + "]}";

    public static final String FORBIDDEN_EXAMPLE =
            "{\"status\":403,"
                    + "\"message\":\"Access denied: Insufficient permissions\","
                    + "\"timestamp\":\"2023-06-15T10:30:00\","
                    + "\"path\":\"/api/v1/users\"}";
}