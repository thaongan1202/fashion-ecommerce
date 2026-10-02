package vn.edu.hcmute.fashion.auth;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.edu.hcmute.fashion.user.Address;
import vn.edu.hcmute.fashion.user.AddressRepository;
import vn.edu.hcmute.fashion.user.User;
import vn.edu.hcmute.fashion.user.UserRepository;

@Service
public class AuthService {

    private static final int OTP_VALIDITY_SECONDS = 60;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PendingRegistrationRepository pendingRepository;
    private final PendingPasswordResetRepository pendingPasswordResetRepository;
    private final PendingPasswordChangeRepository pendingPasswordChangeRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationOtpMailer otpMailer;
    private final Clock clock;
    private final JwtTokenService jwtTokenService;


    public AuthService(
            UserRepository userRepository,
            AddressRepository addressRepository,
            PendingRegistrationRepository pendingRepository,
            PendingPasswordResetRepository pendingPasswordResetRepository,
            PendingPasswordChangeRepository pendingPasswordChangeRepository,
            PasswordEncoder passwordEncoder,
            RegistrationOtpMailer otpMailer,
            Clock clock,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.pendingRepository = pendingRepository;
        this.pendingPasswordResetRepository = pendingPasswordResetRepository;
        this.pendingPasswordChangeRepository = pendingPasswordChangeRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpMailer = otpMailer;
        this.clock = clock;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Email hoặc mật khẩu không chính xác"
                ));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Email hoặc mật khẩu không chính xác"
            );
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tài khoản đã bị khóa"
            );
        }

        return new LoginResponse(
                jwtTokenService.createToken(user),
                "Bearer",
                jwtTokenService.getTokenLifetimeSeconds(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Transactional
    public RegistrationOtpResponse requestPasswordResetOtp(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Gmail này chưa có tài khoản"
                ));

        Instant now = clock.instant();
        PendingPasswordReset pending = pendingPasswordResetRepository.findById(user.getId())
                .orElseGet(PendingPasswordReset::new);

        if (pending.getUserId() != null && pending.getOtpExpiresAt().isAfter(now)) {
            long secondsLeft = Math.max(
                    1,
                    Duration.between(now, pending.getOtpExpiresAt()).toSeconds()
            );
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "OTP hiện tại còn hiệu lực; hãy thử lại sau " + secondsLeft + " giây"
            );
        }

        String otp = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
        pending.setUserId(user.getId());
        pending.setOtpHash(passwordEncoder.encode(otp));
        pending.setOtpExpiresAt(now.plusSeconds(OTP_VALIDITY_SECONDS));
        pending.setFailedAttempts(0);
        pendingPasswordResetRepository.save(pending);

        otpMailer.sendOtp(email, otp);

        return new RegistrationOtpResponse(
                email,
                OTP_VALIDITY_SECONDS,
                "OTP đặt lại mật khẩu đã được gửi"
        );
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Gmail này chưa có tài khoản"
                ));

        PendingPasswordReset pending = pendingPasswordResetRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Chưa có yêu cầu đặt lại mật khẩu"
                ));

        Instant now = clock.instant();
        if (!pending.getOtpExpiresAt().isAfter(now)) {
            pendingPasswordResetRepository.delete(pending);
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "OTP đã hết hạn; hãy yêu cầu mã mới"
            );
        }

        if (!passwordEncoder.matches(request.otp(), pending.getOtpHash())) {
            int attempts = pending.getFailedAttempts() + 1;

            if (attempts >= MAX_OTP_ATTEMPTS) {
                pendingPasswordResetRepository.delete(pending);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "OTP sai quá số lần cho phép; hãy yêu cầu mã mới"
                );
            }

            pending.setFailedAttempts(attempts);
            pendingPasswordResetRepository.save(pending);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP không chính xác"
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        pendingPasswordResetRepository.delete(pending);
    }

    @Transactional
    public RegistrationOtpResponse requestPasswordChangeOtp(
            Long userId,
            ChangePasswordRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản"
                ));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tài khoản đã bị khóa"
            );
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mật khẩu hiện tại không chính xác"
            );
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mật khẩu mới phải khác mật khẩu hiện tại"
            );
        }

        Instant now = clock.instant();
        PendingPasswordChange pending = pendingPasswordChangeRepository.findById(userId)
                .orElseGet(PendingPasswordChange::new);

        if (pending.getUserId() != null && pending.getOtpExpiresAt().isAfter(now)) {
            long secondsLeft = Math.max(
                    1,
                    Duration.between(now, pending.getOtpExpiresAt()).toSeconds()
            );
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "OTP hiện tại còn hiệu lực; hãy thử lại sau " + secondsLeft + " giây"
            );
        }

        String otp = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
        pending.setUserId(userId);
        pending.setNewPasswordHash(passwordEncoder.encode(request.newPassword()));
        pending.setOtpHash(passwordEncoder.encode(otp));
        pending.setOtpExpiresAt(now.plusSeconds(OTP_VALIDITY_SECONDS));
        pending.setFailedAttempts(0);
        pendingPasswordChangeRepository.save(pending);

        otpMailer.sendOtp(user.getEmail(), otp);

        return new RegistrationOtpResponse(
                user.getEmail(),
                OTP_VALIDITY_SECONDS,
                "OTP xác nhận đổi mật khẩu đã được gửi"
        );
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void verifyPasswordChangeOtp(
            Long userId,
            VerifyPasswordChangeOtpRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản"
                ));

        PendingPasswordChange pending = pendingPasswordChangeRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Chưa có yêu cầu đổi mật khẩu"
                ));

        if (!pending.getOtpExpiresAt().isAfter(clock.instant())) {
            pendingPasswordChangeRepository.delete(pending);
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "OTP đã hết hạn; hãy yêu cầu mã mới"
            );
        }

        if (!passwordEncoder.matches(request.otp(), pending.getOtpHash())) {
            int attempts = pending.getFailedAttempts() + 1;

            if (attempts >= MAX_OTP_ATTEMPTS) {
                pendingPasswordChangeRepository.delete(pending);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "OTP sai quá số lần cho phép; hãy yêu cầu gửi mã mới"
                );
            }

            pending.setFailedAttempts(attempts);
            pendingPasswordChangeRepository.save(pending);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP không chính xác"
            );
        }

        user.setPasswordHash(pending.getNewPasswordHash());
        userRepository.save(user);
        pendingPasswordChangeRepository.delete(pending);
    }

    @Transactional
    public RegistrationOtpResponse requestRegistrationOtp(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }

        Instant now = clock.instant();
        PendingRegistration pending = pendingRepository.findByEmailIgnoreCase(email)
                .orElseGet(PendingRegistration::new);
        if (pending.getId() != null && pending.getOtpExpiresAt().isAfter(now)) {
            long secondsLeft = Math.max(1, Duration.between(now, pending.getOtpExpiresAt()).toSeconds());
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "OTP hiện tại còn hiệu lực; hãy thử lại sau " + secondsLeft + " giây"
            );
        }

        String otp = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
        pending.setFullName(request.fullName().trim());
        pending.setPhone(request.phone());
        pending.setAddressLine(request.addressLine().trim());
        pending.setEmail(email);
        pending.setPasswordHash(passwordEncoder.encode(request.password()));
        pending.setOtpHash(passwordEncoder.encode(otp));
        pending.setOtpExpiresAt(now.plusSeconds(OTP_VALIDITY_SECONDS));
        pending.setFailedAttempts(0);
        pendingRepository.save(pending);

        otpMailer.sendOtp(email, otp);
        return new RegistrationOtpResponse(email, OTP_VALIDITY_SECONDS, "OTP đã được gửi đến Gmail của bạn");
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public RegisterResponse verifyRegistrationOtp(VerifyRegistrationOtpRequest request) {
        String email = normalizeEmail(request.email());
        PendingRegistration pending = pendingRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không có yêu cầu đăng ký đang chờ"));

        Instant now = clock.instant();
        if (!pending.getOtpExpiresAt().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.GONE, "OTP đã hết hạn; hãy yêu cầu gửi mã mới");
        }

        if (!passwordEncoder.matches(request.otp(), pending.getOtpHash())) {
            int attempts = pending.getFailedAttempts() + 1;
            if (attempts >= MAX_OTP_ATTEMPTS) {
                pendingRepository.delete(pending);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP sai quá số lần cho phép; hãy đăng ký lại");
            }
            pending.setFailedAttempts(attempts);
            pendingRepository.save(pending);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP không chính xác");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            pendingRepository.delete(pending);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }

        User user = new User();
        user.setFullName(pending.getFullName());
        user.setEmail(email);
        user.setPhone(pending.getPhone());
        user.setPasswordHash(pending.getPasswordHash());
        User savedUser = userRepository.save(user);

        addressRepository.save(new Address(
                savedUser.getId(),
                pending.getFullName(),
                pending.getPhone(),
                pending.getAddressLine(),
                true
        ));
        pendingRepository.delete(pending);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
