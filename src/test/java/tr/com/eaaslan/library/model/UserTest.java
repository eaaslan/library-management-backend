package tr.com.eaaslan.library.model;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid user")
    void shouldCreateValidUser() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("05501234567")
                .role(UserRole.PATRON)
                .status(UserStatus.ACTIVE)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "User should have no validation violations");
    }

    @ParameterizedTest
    @CsvSource(value = {
            ",Email is required",
            "invalid-email,Email is not valid",
           
    }, nullValues = "null")
    @DisplayName("Should validate email correctly")
    void shouldValidateEmailCorrectly(String email, String expectedMessage) {
        User user = User.builder()
                .email(email)
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("05501234567")
                .role(UserRole.PATRON)
                .status(UserStatus.ACTIVE)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Should have validation violations");

        boolean hasExpectedMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .anyMatch(message -> message.equals(expectedMessage));

        assertTrue(hasExpectedMessage, "Should have expected validation message");
    }

    @ParameterizedTest
    @CsvSource(value = {
            ",Password is required",
            "short,Password must be at least 8 characters"
    }, nullValues = "null")
    @DisplayName("Should validate password correctly")
    void shouldValidatePasswordCorrectly(String password, String expectedMessage) {
        User user = User.builder()
                .email("test@example.com")
                .password(password)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("05501234567")
                .role(UserRole.PATRON)
                .status(UserStatus.ACTIVE)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Should have validation violations");

        boolean hasExpectedMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .anyMatch(message -> message.equals(expectedMessage));

        assertTrue(hasExpectedMessage, "Should have expected validation message");
    }

    @ParameterizedTest
    @CsvSource(value = {
            ",Phone is required",
            "12345,Phone number must start with 05 and be 11 digits",
            "15501234567,Phone number must start with 05 and be 11 digits"
    }, nullValues = "null")
    @DisplayName("Should validate phone number correctly")
    void shouldValidatePhoneNumberCorrectly(String phoneNumber, String expectedMessage) {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber(phoneNumber)
                .role(UserRole.PATRON)
                .status(UserStatus.ACTIVE)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Should have validation violations");

        boolean hasExpectedMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .anyMatch(message -> message.equals(expectedMessage));

        assertTrue(hasExpectedMessage, "Should have expected validation message");
    }
}