package tr.com.eaaslan.library.service;

import org.springframework.data.domain.Page;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.model.dto.user.UserUpdateRequest;


import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreateRequest userCreateRequest);

    UserResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    Page<UserResponse> getAllUsers(int page, int size, String sortBy);

    UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest);

    UserResponse deleteUser(Long id);

    Page<UserResponse> searchUsersByName(String firstName, String lastName, int page, int size);

    Page<UserResponse> getUsersByRole(String role, int page, int size);

    Page<UserResponse> getActiveUsers(int page, int size);

 
}