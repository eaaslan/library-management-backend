package tr.com.eaaslan.library.model.dto.user;

public record UserUpdateRequest(
        String firstName,
        String lastName,
        String phoneNumber

) {

}