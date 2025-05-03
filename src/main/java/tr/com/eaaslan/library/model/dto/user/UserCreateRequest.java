package tr.com.eaaslan.library.model.dto.user;

public record UserCreateRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber,
        String role,
        String status

) {
}
