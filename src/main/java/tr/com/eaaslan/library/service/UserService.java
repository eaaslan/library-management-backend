package tr.com.eaaslan.library.service;

import org.springframework.data.domain.Page;
import tr.com.eaaslan.library.model.dto.user.*;


import java.util.List;

public interface UserService {

    LibrarianCreateResponse createLibrarianUser(UserCreateRequest userCreateRequest);

    UserResponse getUserById(Long id);

    UserResponse createPatronUser(UserCreateRequest userCreateRequest);

    Page<UserResponse> getAllUsersIncludingDeleted(int page, int size, String sortBy);

    Page<UserResponse> getAllActiveUsers(int page, int size, String sortBy);

    UserUpdateResponse updateUser(Long id, UserUpdateRequest userUpdateRequest);

    UserResponse deleteUser(Long id, String userName);

    UserResponse hardDeleteUser(Long id, String userName);

    UserUpdateResponse updateUserStatus(Long id, String status);

    Page<UserResponse> searchByName(String searchTerm, int page, int size);

    Page<UserResponse> getUsersByRole(String role, int page, int size);

    Page<UserResponse> getActiveUsersByRole(String role, int page, int size);

    Page<UserResponse> getActiveUsers(int page, int size);

}