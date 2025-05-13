package tr.com.eaaslan.library.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.com.eaaslan.library.config.ApiResponseAnnotations.RegistrationResponses;
import tr.com.eaaslan.library.model.dto.auth.JwtAuthResponse;
import tr.com.eaaslan.library.model.dto.auth.LoginRequest;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.security.JwtUtil;
import tr.com.eaaslan.library.security.LibraryUserDetails;
import tr.com.eaaslan.library.service.UserService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication Management", description = "APIs for managing authentication in the library")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Operation(
            summary = "User login",
            description = "Authenticate user with email and password and return JWT token"
    )

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);
        LibraryUserDetails userDetails = (LibraryUserDetails) authentication.getPrincipal();

        JwtAuthResponse response = new JwtAuthResponse(
                jwt,
                "Bearer",
                userDetails.getUsername(),
                userDetails.getAuthorities().stream().findFirst().get().getAuthority(),
                jwtUtil.getExpirationTimeInSeconds(jwt)
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "User register",
            description = "Authenticate user with email and password and return JWT token"
    )
    @RegistrationResponses
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest registerRequest) {
        UserResponse createdUser = userService.createPatronUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
