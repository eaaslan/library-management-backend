package tr.com.eaaslan.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.com.eaaslan.library.model.dto.user.*;
import tr.com.eaaslan.library.security.SecurityService;
import tr.com.eaaslan.library.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users in the library")
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;

    public UserController(UserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;
    }

    @Operation(
            summary = "Create user",
            description = "Creates a new user with the provided information"
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest, @RequestParam String role) {
        UserResponse createdUser = userService.createPatronUserWithStatus(userCreateRequest, role);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Create librarian",
            description = "Creates a new librarian user (Admin only)"
    )
    @PostMapping("/librarians")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<LibrarianCreateResponse> createLibrarian(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        LibrarianCreateResponse createdLibrarian = userService.createLibrarianUser(userCreateRequest);
        return new ResponseEntity<>(createdLibrarian, HttpStatus.CREATED);
    }


    @Operation(
            summary = "Get all users including deleted",
            description = "Returns all users in the library including deleted ones"
    )
    @GetMapping("/all") // Farklı bir endpoint kullan
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsersIncludingDeleted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(userService.getAllUsersIncludingDeleted(page, size, sortBy));
    }


    @Operation(
            summary = "Get all active users",
            description = "Returns all non-deleted users in the library"
    )
    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(userService.getAllActiveUsers(page, size, sortBy));
    }

    @Operation(
            summary = "Get active users by role",
            description = "Returns active users with the provided role"
    )
    @GetMapping("/role/{role}/active")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getActiveUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getActiveUsersByRole(role, page, size));
    }

    @Operation(
            summary = "Get user by id",
            description = "Returns user with the provided id"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "Soft delete user by id",
            description = "Soft deletes user with the provided id"
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<UserResponse> softDeleteUser(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(userService.deleteUser(id, principal.getName()));
    }

    @Operation(
            summary = "Hard delete user by id",
            description = "Hard deletes user with the provided id"
    )
    @DeleteMapping("/permanent/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> hardDeleteUser(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(userService.hardDeleteUser(id, principal.getName()));
    }

    @Operation(
            summary = "Update user by id",
            description = "Updates user with the provided id"
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<UserUpdateResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userCreateRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userCreateRequest));
    }


    @Operation(
            summary = "Search users by name",
            description = "Returns users with the provided name"
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUsersByRole(@PathVariable String role,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(userService.getUsersByRole(role, page, size));
    }

    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<UserUpdateResponse> updateUserStatus(@PathVariable Long id, @PathVariable String status) {
        return ResponseEntity.ok(userService.updateUserStatus(id, status));
    }


}
