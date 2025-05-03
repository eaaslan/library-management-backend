package tr.com.eaaslan.library.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.com.eaaslan.library.model.dto.auth.JwtAuthResponse;
import tr.com.eaaslan.library.model.dto.auth.LoginRequest;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.security.JwtUtil;
import tr.com.eaaslan.library.security.LibraryUserDetails;
import tr.com.eaaslan.library.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
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

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest registerRequest) {
        return ResponseEntity.ok(userService.createUser(registerRequest));
    }
}
