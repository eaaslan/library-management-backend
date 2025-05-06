package tr.com.eaaslan.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, name = "email", unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;

    @Column(nullable = false, name = "password")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Column(nullable = false, name = "first_name")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Column(nullable = false, name = "last_name")
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Column(nullable = false, name = "phone_number", unique = true)
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^05[0-9]{9}$", message = "Phone number must start with 05 and be 11 digits")
    private String phoneNumber;

    @Column(nullable = false, name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.PATRON;

    @Column(nullable = false, name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "deleted")
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "max_allowed_borrows")
    @Builder.Default
    private int maxAllowedBorrows = 3;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return email != null && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() + (email != null ? email.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", deleted=" + deleted +
                ", deletedAt=" + deletedAt +
                ", deletedBy='" + deletedBy + '\'' +
                ", maxAllowedBorrows=" + maxAllowedBorrows +
                '}';
    }
}
