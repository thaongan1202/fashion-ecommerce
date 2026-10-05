package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.auth.*;
import com.utephonehub.backend.dto.response.auth.AuthResponse;
import com.utephonehub.backend.dto.response.auth.RegistrationOtpResponse;
import com.utephonehub.backend.dto.response.user.UserResponse;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.entity.Cart;
import com.utephonehub.backend.enums.UserRole;
import com.utephonehub.backend.enums.UserStatus;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.exception.ConflictException;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.exception.UnauthorizedException;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.repository.CartRepository;
import com.utephonehub.backend.service.IAuthService;
import com.utephonehub.backend.service.IEmailService;
import com.utephonehub.backend.mapper.UserMapper;
import com.utephonehub.backend.util.JwtTokenProvider;
import com.utephonehub.backend.util.PasswordEncoder;
import com.utephonehub.backend.util.OtpGenerator;
import com.utephonehub.backend.util.EmailAddressNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpGenerator otpGenerator;
    private final RedisTemplate<String, String> redisTemplate;
    private final IEmailService emailService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    private static final String OTP_PREFIX = "otp:";
    private static final String REGISTER_OTP_PREFIX = "verify_email:";
    private static final String REGISTER_ATTEMPTS_PREFIX = "verify_email_attempts:";
    private static final String REGISTER_LOCK_PREFIX = "verify_email_lock:";
    private static final String REGISTER_PENDING_PREFIX = "verify_email_pending:";
    private static final long OTP_EXPIRATION_MINUTES = 5;
    private static final long REGISTER_OTP_SECONDS = 60;
    private static final int REGISTER_MAX_ATTEMPTS = 5;
    private static final long REGISTER_PENDING_MINUTES = 15;

    @Override
    public RegistrationOtpResponse register(RegisterRequest request) {
        log.info("Starting registration OTP for email: {}", request.getEmail());
        String email = validateRegistration(request);

        Long lockTtl = redisTemplate.getExpire(REGISTER_LOCK_PREFIX + email, TimeUnit.SECONDS);
        if (lockTtl != null && lockTtl > 0) {
            throw new BadRequestException("Bạn đã nhập sai 5 lần. Vui lòng đợi hết thời hạn "
                    + lockTtl + " giây trước khi yêu cầu OTP mới.");
        }

        Long otpTtl = redisTemplate.getExpire(REGISTER_OTP_PREFIX + email, TimeUnit.SECONDS);
        if (otpTtl != null && otpTtl > 0 && Boolean.TRUE.equals(redisTemplate.hasKey(REGISTER_OTP_PREFIX + email))) {
            return otpResponse(email, otpTtl.intValue(), remainingAttempts(email));
        }

        storePendingRegistration(email, request);
        issueRegistrationOtp(email, request.getFullName());
        return otpResponse(email, (int) REGISTER_OTP_SECONDS, REGISTER_MAX_ATTEMPTS);
    }

    @Override
    public RegistrationOtpResponse resendRegistrationOtp(ForgotPasswordRequest request) {
        String email = EmailAddressNormalizer.normalize(request.getEmail());
        Long lockTtl = redisTemplate.getExpire(REGISTER_LOCK_PREFIX + email, TimeUnit.SECONDS);
        if (lockTtl != null && lockTtl > 0) {
            throw new BadRequestException("Bạn đã nhập sai 5 lần. Vui lòng đợi hết thời hạn "
                    + lockTtl + " giây trước khi yêu cầu OTP mới.");
        }
        Long otpTtl = redisTemplate.getExpire(REGISTER_OTP_PREFIX + email, TimeUnit.SECONDS);
        if (otpTtl != null && otpTtl > 0 && Boolean.TRUE.equals(redisTemplate.hasKey(REGISTER_OTP_PREFIX + email))) {
            throw new BadRequestException("OTP hiện tại vẫn còn hiệu lực. Vui lòng đợi hết thời hạn "
                    + otpTtl + " giây trước khi yêu cầu mã mới.");
        }

        String pending = redisTemplate.opsForValue().get(REGISTER_PENDING_PREFIX + email);
        if (pending == null) {
            throw new BadRequestException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
        }
        try {
            RegisterRequest stored = objectMapper.readValue(pending, RegisterRequest.class);
            redisTemplate.expire(REGISTER_PENDING_PREFIX + email, REGISTER_PENDING_MINUTES, TimeUnit.MINUTES);
            issueRegistrationOtp(email, stored.getFullName());
            return otpResponse(email, (int) REGISTER_OTP_SECONDS, REGISTER_MAX_ATTEMPTS);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Cannot resend registration OTP for {}", email, ex);
            throw new BadRequestException("Không thể cấp lại OTP. Vui lòng đăng ký lại.");
        }
    }

    @Override
    @Transactional
    public UserResponse registerAdmin(RegisterRequest request) {
        log.info("Registering new admin with email: {}", request.getEmail());
        String email = EmailAddressNormalizer.normalize(request.getEmail());

        // Validate password match
        if (request.getPassword() != null && request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu và xác nhận mật khẩu không khớp");
        }

        // Check if email already exists
        if (email != null && userRepository.findByCanonicalEmail(email).isPresent()) {
            throw new ConflictException("Email này đã được sử dụng");
        }

        // Check if username already exists
        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Tên đăng nhập này đã được sử dụng");
        }

        // Create new admin user
        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        log.info("Admin registered successfully with id: {}", user.getId());

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt with usernameOrEmail: {}", request.getUsernameOrEmail());

        // Find user by email or username
        String normalizedEmail = EmailAddressNormalizer.normalize(request.getUsernameOrEmail());
        User user = userRepository.findByCanonicalEmail(normalizedEmail)
                .or(() -> userRepository.findByUsername(request.getUsernameOrEmail()))
                .orElseThrow(() -> new UnauthorizedException("Tên đăng nhập/email hoặc mật khẩu không chính xác"));

        // Check if account is locked
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new UnauthorizedException("Tài khoản của bạn đã bị khóa");
        }

        // Only allow ACTIVE users to login
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Tài khoản của bạn không ở trạng thái hoạt động");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Tên đăng nhập/email hoặc mật khẩu không chính xác");
        }

        AuthResponse response = buildAuthResponse(user);
        log.info("User logged in successfully with id: {}", user.getId());
        return response;
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Refresh token không hợp lệ");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());
        String email = jwtTokenProvider.getEmailFromToken(request.getRefreshToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        if (user.getDeletedAt() != null || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Tài khoản không còn hoạt động");
        }

        // Verify refresh token in Redis
        String refreshTokenKey = "refresh_token:" + userId;
        String storedToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (!request.getRefreshToken().equals(storedToken)) {
            throw new UnauthorizedException("Refresh token không hợp lệ");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    /**
     * Generate access/refresh tokens for a user, store refresh token in Redis, and
     * build AuthResponse.
     */
    public AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        String refreshTokenKey = "refresh_token:" + user.getId();
        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 7, TimeUnit.DAYS);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        log.info("User logout with id: {}", userId);
        String refreshTokenKey = "refresh_token:" + userId;
        redisTemplate.delete(refreshTokenKey);
    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        log.info("Password reset request for email: {}", request.getEmail());
        String email = EmailAddressNormalizer.normalize(request.getEmail());

        User user = userRepository.findByCanonicalEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // Generate OTP
        String otp = otpGenerator.generateOtp();

        // Store OTP in Redis with expiration
        String otpKey = OTP_PREFIX + email;
        redisTemplate.opsForValue().set(otpKey, otp, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        // Send OTP via email
        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
            throw new BadRequestException("Không thể gửi email OTP");
        }

        log.info("OTP sent to email: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void verifyOtpAndResetPassword(VerifyOtpRequest request) {
        log.info("Verifying OTP and resetting password for email: {}", request.getEmail());
        String email = EmailAddressNormalizer.normalize(request.getEmail());

        // Validate input
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu và xác nhận mật khẩu không khớp");
        }

        if (!passwordEncoder.isValidPassword(request.getNewPassword())) {
            throw new BadRequestException("Mật khẩu phải ít nhất 8 ký tự, chứa chữ hoa, chữ thường và số");
        }

        // Verify OTP
        String otpKey = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new UnauthorizedException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        // Find user
        User user = userRepository.findByCanonicalEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete OTP from Redis
        redisTemplate.delete(otpKey);

        log.info("Password reset successfully for user id: {}", user.getId());

        // Send password reset confirmation email (async, không block flow)
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}",
                    user.getEmail(), e.getMessage());
            // Không throw exception
        }
    }

    @Override
    @Transactional
    public UserResponse verifyRegistrationOtp(VerifyRegistrationOtpRequest request) {
        log.info("Verifying registration OTP for email: {}", request.getEmail());
        String email = EmailAddressNormalizer.normalize(request.getEmail());
        String lockKey = REGISTER_LOCK_PREFIX + email;
        Long lockTtl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        if (lockTtl != null && lockTtl > 0) {
            throw new UnauthorizedException("Bạn đã nhập sai tối đa 5 lần. Vui lòng đợi hết thời hạn "
                    + lockTtl + " giây để yêu cầu OTP mới.");
        }

        String otpKey = REGISTER_OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        if (storedOtp == null) {
            throw new UnauthorizedException("Mã OTP đã hết hạn. Vui lòng yêu cầu cấp lại OTP mới.");
        }
        if (!storedOtp.equals(request.getOtp())) {
            registerFailedAttempt(email);
        }

        String pending = redisTemplate.opsForValue().get(REGISTER_PENDING_PREFIX + email);
        if (pending == null) {
            throw new BadRequestException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
        }

        RegisterRequest storedRequest;
        try {
            storedRequest = objectMapper.readValue(pending, RegisterRequest.class);
        } catch (Exception ex) {
            throw new BadRequestException("Không thể đọc thông tin đăng ký. Vui lòng đăng ký lại.");
        }

        validateRegistration(storedRequest);
        User user = User.builder()
                .username(storedRequest.getUsername())
                .fullName(storedRequest.getFullName())
                .email(email)
                .phoneNumber(storedRequest.getPhoneNumber())
                .gender(storedRequest.getGender())
                .dateOfBirth(storedRequest.getDateOfBirth())
                .passwordHash(passwordEncoder.encode(storedRequest.getPassword()))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .walletBalance(java.math.BigDecimal.ZERO)
                .build();
        user = userRepository.save(user);
        cartRepository.save(Cart.builder().user(user).build());

        redisTemplate.delete(otpKey);
        redisTemplate.delete(REGISTER_ATTEMPTS_PREFIX + email);
        redisTemplate.delete(lockKey);
        redisTemplate.delete(REGISTER_PENDING_PREFIX + email);

        try {
            emailService.sendRegistrationEmail(user.getEmail(), user.getFullName());
        } catch (Exception ex) {
            log.error("Failed to send welcome email to {}: {}", user.getEmail(), ex.getMessage());
        }

        log.info("Registration OTP verified and user created: {}", user.getId());
        return userMapper.toResponse(user);
    }

    private String validateRegistration(RegisterRequest request) {
        if (request.getPassword() != null && request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu và xác nhận mật khẩu không khớp");
        }
        String email = EmailAddressNormalizer.normalize(request.getEmail());
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email không được để trống");
        }
        if (userRepository.findByCanonicalEmail(email).isPresent()) {
            throw new ConflictException("Email này đã được sử dụng");
        }
        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Tên đăng nhập này đã được sử dụng");
        }
        request.setEmail(email);
        return email;
    }

    private void storePendingRegistration(String email, RegisterRequest request) {
        try {
            redisTemplate.opsForValue().set(
                    REGISTER_PENDING_PREFIX + email,
                    objectMapper.writeValueAsString(request),
                    REGISTER_PENDING_MINUTES,
                    TimeUnit.MINUTES);
        } catch (Exception ex) {
            throw new BadRequestException("Không thể lưu thông tin đăng ký. Vui lòng thử lại.");
        }
    }

    private void issueRegistrationOtp(String email, String fullName) {
        String otp = otpGenerator.generateOtp();
        redisTemplate.opsForValue().set(REGISTER_OTP_PREFIX + email, otp, REGISTER_OTP_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(REGISTER_ATTEMPTS_PREFIX + email, "0", REGISTER_OTP_SECONDS, TimeUnit.SECONDS);
        redisTemplate.delete(REGISTER_LOCK_PREFIX + email);
        try {
            emailService.sendRegistrationOtpEmail(email, fullName, otp);
        } catch (Exception ex) {
            redisTemplate.delete(REGISTER_OTP_PREFIX + email);
            redisTemplate.delete(REGISTER_ATTEMPTS_PREFIX + email);
            log.error("Failed to send registration OTP to {}: {}", email, ex.getMessage());
            throw new BadRequestException("Không thể gửi email OTP. Vui lòng kiểm tra lại địa chỉ email.");
        }
    }

    private void registerFailedAttempt(String email) {
        String attemptsKey = REGISTER_ATTEMPTS_PREFIX + email;
        String raw = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = 0;
        if (raw != null) {
            try {
                attempts = Integer.parseInt(raw);
            } catch (NumberFormatException ignored) {
                attempts = 0;
            }
        }
        attempts++;
        if (attempts >= REGISTER_MAX_ATTEMPTS) {
            Long remain = redisTemplate.getExpire(REGISTER_OTP_PREFIX + email, TimeUnit.SECONDS);
            if (remain == null || remain < 1) {
                remain = REGISTER_OTP_SECONDS;
            }
            redisTemplate.opsForValue().set(REGISTER_LOCK_PREFIX + email, "1", remain, TimeUnit.SECONDS);
            redisTemplate.delete(REGISTER_OTP_PREFIX + email);
            redisTemplate.delete(attemptsKey);
            throw new UnauthorizedException("Bạn đã nhập sai 5 lần. Vui lòng đợi hết thời hạn "
                    + remain + " giây trước khi yêu cầu OTP mới.");
        }
        Long remain = redisTemplate.getExpire(REGISTER_OTP_PREFIX + email, TimeUnit.SECONDS);
        if (remain == null || remain < 1) {
            remain = REGISTER_OTP_SECONDS;
        }
        redisTemplate.opsForValue().set(attemptsKey, String.valueOf(attempts), remain, TimeUnit.SECONDS);
        throw new UnauthorizedException("Mã OTP không đúng. Bạn còn " + (REGISTER_MAX_ATTEMPTS - attempts) + " lần thử.");
    }

    private int remainingAttempts(String email) {
        String raw = redisTemplate.opsForValue().get(REGISTER_ATTEMPTS_PREFIX + email);
        int used = 0;
        if (raw != null) {
            try {
                used = Integer.parseInt(raw);
            } catch (NumberFormatException ignored) {
                used = 0;
            }
        }
        return Math.max(0, REGISTER_MAX_ATTEMPTS - used);
    }

    private RegistrationOtpResponse otpResponse(String email, int expiresInSeconds, int remainingAttempts) {
        return RegistrationOtpResponse.builder()
                .email(email)
                .expiresInSeconds(expiresInSeconds)
                .maxAttempts(REGISTER_MAX_ATTEMPTS)
                .remainingAttempts(remainingAttempts)
                .build();
    }
}
