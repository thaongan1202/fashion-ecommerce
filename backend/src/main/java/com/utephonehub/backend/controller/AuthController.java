package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.auth.*;
import com.utephonehub.backend.dto.response.auth.AuthResponse;
import com.utephonehub.backend.dto.response.auth.RegistrationOtpResponse;
import com.utephonehub.backend.dto.response.user.UserResponse;
import com.utephonehub.backend.service.IAuthService;
import com.utephonehub.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API xác thực và quản lý phiên đăng nhập")
public class AuthController {

    private final IAuthService authService;
    private final SecurityUtils securityUtils;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo một tài khoản khách hàng mới")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    public ResponseEntity<ApiResponse<RegistrationOtpResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email: {}", request.getEmail());
        RegistrationOtpResponse otp = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Mã OTP đã được gửi tới email đăng ký. Mã có hiệu lực 60 giây.", otp));
    }

    @PostMapping("/register/resend-otp")
    @Operation(summary = "Cấp lại OTP đăng ký", description = "Chỉ cấp lại sau khi OTP hiện tại hết hạn 60 giây")
    public ResponseEntity<ApiResponse<RegistrationOtpResponse>> resendRegistrationOtp(
            @Valid @RequestBody ForgotPasswordRequest request) {
        RegistrationOtpResponse otp = authService.resendRegistrationOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi OTP mới. Mã có hiệu lực 60 giây.", otp));
    }

    @PostMapping("/register/admin")
    @Operation(summary = "Đăng ký tài khoản Admin", description = "Tạo một tài khoản admin mới")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    public ResponseEntity<ApiResponse<UserResponse>> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        log.info("Register admin request for email: {}", request.getEmail());
        UserResponse user = authService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng ký tài khoản Admin thành công", user));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng username hoặc email và mật khẩu")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Tên đăng nhập/email hoặc mật khẩu không chính xác")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for usernameOrEmail: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token", description = "Sử dụng refresh token để lấy access token mới")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Làm mới token thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Kết thúc phiên đăng nhập")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Không được xác thực")
    })
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request) {
        log.info("Logout request");
        Long userId = securityUtils.getUserIdIfAuthenticated(request);
        if (userId != null) {
            authService.logout(userId);
        }
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    @PostMapping("/forgot-password/request")
    @Operation(summary = "Yêu cầu đặt lại mật khẩu", description = "Gửi OTP đến email để đặt lại mật khẩu")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP đã được gửi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Người dùng không tồn tại")
    })
    public ResponseEntity<ApiResponse<?>> requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password reset request for email: {}", request.getEmail());
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success("Nếu email tồn tại, một mã OTP sẽ được gửi đi", null));
    }

    @PostMapping("/forgot-password/verify")
    @Operation(summary = "Xác thực OTP và đặt lại mật khẩu", description = "Xác thực mã OTP và cập nhật mật khẩu mới")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mật khẩu đã được đặt lại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "OTP không hợp lệ")
    })
    public ResponseEntity<ApiResponse<?>> verifyOtpAndResetPassword(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("Verify OTP and reset password for email: {}", request.getEmail());
        authService.verifyOtpAndResetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Mật khẩu đã được đặt lại thành công", null));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Xác thực email đăng ký", description = "Xác thực email bằng mã OTP được gửi sau khi đăng ký")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xác thực email thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "OTP không hợp lệ hoặc đã hết hạn")
    })
    public ResponseEntity<ApiResponse<UserResponse>> verifyRegistrationOtp(
            @Valid @RequestBody VerifyRegistrationOtpRequest request) {
        log.info("Verify registration OTP for email: {}", request.getEmail());
        UserResponse user = authService.verifyRegistrationOtp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Xác thực email thành công. Tài khoản đã được tạo.", user));
    }
}

