package tr.com.eaaslan.library.controller;

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
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        UserResponse createdUser = userService.createUser(userCreateRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(userService.getAllUsers(page, size, sortBy));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<UserResponse>> getActiveUsers(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getActiveUsers(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userCreateRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userCreateRequest));
    }

    @GetMapping("/search/name/{firstName}/{lastName}")
    public ResponseEntity<Page<UserResponse>> searchUsersByName(@PathVariable String firstName, @PathVariable String lastName,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.searchUsersByName(firstName, lastName, page, size));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<Page<UserResponse>> getUsersByRole(@PathVariable String role,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getUsersByRole(role, page, size));
    }


}
