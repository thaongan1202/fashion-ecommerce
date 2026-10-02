package vn.edu.hcmute.fashion.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationOtpResponse> requestRegistrationOtp(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.accepted().body(authService.requestRegistrationOtp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<RegistrationOtpResponse> requestPasswordResetOtp(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.accepted()
                .body(authService.requestPasswordResetOtp(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password/request-otp")
    public ResponseEntity<RegistrationOtpResponse> requestPasswordChangeOtp(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.accepted()
                .body(authService.requestPasswordChangeOtp(userId, request));
    }

    @PostMapping("/change-password/verify-otp")
    public ResponseEntity<Void> verifyPasswordChangeOtp(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody VerifyPasswordChangeOtpRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        authService.verifyPasswordChangeOtp(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<RegisterResponse> verifyRegistrationOtp(
            @Valid @RequestBody VerifyRegistrationOtpRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.verifyRegistrationOtp(request));
    }
}
