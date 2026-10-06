package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.auth.*;
import com.utephonehub.backend.dto.response.auth.AuthResponse;
import com.utephonehub.backend.dto.response.auth.RegistrationOtpResponse;
import com.utephonehub.backend.dto.response.user.UserResponse;

/**
 * Interface for Authentication Service operations
 */
public interface IAuthService {
    
    /**
     * Bắt đầu đăng ký: gửi OTP xác thực email, chưa tạo tài khoản.
     */
    RegistrationOtpResponse register(RegisterRequest request);

    /**
     * Cấp lại OTP đăng ký sau khi mã hiện tại hết hạn.
     */
    RegistrationOtpResponse resendRegistrationOtp(ForgotPasswordRequest request);
    
    /**
     * Register new admin account
     * @param request Registration request
     * @return UserResponse
     */
    UserResponse registerAdmin(RegisterRequest request);
    
    /**
     * Login user
     * @param request Login request
     * @return AuthResponse with tokens
     */
    AuthResponse login(LoginRequest request);
    
    /**
     * Refresh access token
     * @param request Refresh token request
     * @return AuthResponse with new access token
     */
    AuthResponse refreshToken(RefreshTokenRequest request);
    
    /**
     * Logout user
     * @param userId User ID
     */
    void logout(Long userId);
    
    /**
     * Request password reset
     * @param request Forgot password request
     */
    void requestPasswordReset(ForgotPasswordRequest request);
    
    /**
     * Verify OTP and reset password
     * @param request Verify OTP request
     */
    void verifyOtpAndResetPassword(VerifyOtpRequest request);

    /**
     * Xác thực OTP và tạo tài khoản khách hàng.
     */
    UserResponse verifyRegistrationOtp(VerifyRegistrationOtpRequest request);
}
