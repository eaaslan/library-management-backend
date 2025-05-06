package tr.com.eaaslan.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.model.dto.user.UserUpdateRequest;
import tr.com.eaaslan.library.service.UserService;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users in the library")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Create user",
            description = "Creates a new user with the provided information"
    )
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        UserResponse createdUser = userService.createUser(userCreateRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all users",
            description = "Returns all users in the library"
    )
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(userService.getAllUsers(page, size, sortBy));
    }

    @Operation(
            summary = "Get active users",
            description = "Returns all active users in the library"
    )
    @GetMapping("/active")
    public ResponseEntity<Page<UserResponse>> getActiveUsers(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getActiveUsers(page, size));
    }

    @Operation(
            summary = "Get user by id",
            description = "Returns user with the provided id"
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "Delete user by id",
            description = "Deletes user with the provided id"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @Operation(
            summary = "Update user by id",
            description = "Updates user with the provided id"
    )
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userCreateRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userCreateRequest));
    }

    @Operation(
            summary = "Search users by name",
            description = "Returns users with the provided name"
    )
    @GetMapping("/search/name/{firstName}/{lastName}")
    public ResponseEntity<Page<UserResponse>> searchUsersByName(@PathVariable String firstName, @PathVariable String lastName,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.searchUsersByName(firstName, lastName, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.searchByName(name, page, size));
    }

    @Operation(
            summary = "Get users by role",
            description = "Returns users with the provided role"
    )
    @GetMapping("/role/{role}")
    public ResponseEntity<Page<UserResponse>> getUsersByRole(@PathVariable String role,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getUsersByRole(role, page, size));
    }


}
