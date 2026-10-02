package vn.edu.hcmute.fashion.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import vn.edu.hcmute.fashion.user.Address;
import vn.edu.hcmute.fashion.user.AddressRepository;
import vn.edu.hcmute.fashion.user.User;
import vn.edu.hcmute.fashion.user.UserRepository;

class AuthRegistrationTest {

    private static final Instant NOW = Instant.parse("2026-10-01T05:00:00Z");

    private UserRepository userRepository;
    private AddressRepository addressRepository;
    private PendingRegistrationRepository pendingRepository;
    private RegistrationOtpMailer otpMailer;
    private PasswordEncoder passwordEncoder;
    private MutableClock clock;
    private AuthService authService;
    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(UserRepository.class);
        addressRepository = org.mockito.Mockito.mock(AddressRepository.class);
        pendingRepository = org.mockito.Mockito.mock(PendingRegistrationRepository.class);
        otpMailer = org.mockito.Mockito.mock(RegistrationOtpMailer.class);
        passwordEncoder = new BCryptPasswordEncoder();
        clock = new MutableClock(NOW);
        authService = new AuthService(
                userRepository,
                addressRepository,
                pendingRepository,
                passwordEncoder,
                otpMailer,
                clock
        );
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void registrationRequiresVietnamPhoneGmailAndStrongPassword() {
        RegisterRequest invalid = new RegisterRequest(
                "Test User", "1234567890", "Home address", "test@example.com", "weak"
        );

        assertThat(validator.validate(invalid)).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void requestOtpStoresPendingDataAndDoesNotCreateAccount() {
        when(userRepository.existsByEmailIgnoreCase("test@gmail.com")).thenReturn(false);
        when(pendingRepository.findByEmailIgnoreCase("test@gmail.com")).thenReturn(Optional.empty());
        when(pendingRepository.save(any(PendingRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AtomicReference<String> sentOtp = new AtomicReference<>();
        doAnswer(invocation -> {
            sentOtp.set(invocation.getArgument(1));
            return null;
        }).when(otpMailer).sendOtp(anyString(), anyString());

        RegistrationOtpResponse response = authService.requestRegistrationOtp(validRequest(" TEST@gmail.com "));

        assertThat(response.expiresInSeconds()).isEqualTo(60);
        assertThat(sentOtp.get()).matches("[0-9]{6}");
        verify(userRepository, never()).save(any(User.class));
        verify(addressRepository, never()).save(any(Address.class));
        org.mockito.ArgumentCaptor<PendingRegistration> pendingCaptor =
                org.mockito.ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRepository).save(pendingCaptor.capture());
        assertThat(passwordEncoder.matches("Strong#Pass1", pendingCaptor.getValue().getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches(sentOtp.get(), pendingCaptor.getValue().getOtpHash())).isTrue();
    }

    @Test
    void expiredOtpCanBeReplacedImmediately() {
        PendingRegistration pending = pending("test@gmail.com", NOW.minusSeconds(1), "OldCode1!");
        when(userRepository.existsByEmailIgnoreCase("test@gmail.com")).thenReturn(false);
        when(pendingRepository.findByEmailIgnoreCase("test@gmail.com")).thenReturn(Optional.of(pending));
        when(pendingRepository.save(any(PendingRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationOtpResponse response = authService.requestRegistrationOtp(validRequest("test@gmail.com"));

        assertThat(response.expiresInSeconds()).isEqualTo(60);
        assertThat(pending.getOtpExpiresAt()).isEqualTo(NOW.plusSeconds(60));
        verify(otpMailer).sendOtp(eq("test@gmail.com"), matches("[0-9]{6}"));
    }

    @Test
    void activeOtpCannotBeResentBeforeItsSixtySecondExpiry() {
        PendingRegistration pending = pending("test@gmail.com", NOW.plusSeconds(30), "123456");
        when(userRepository.existsByEmailIgnoreCase("test@gmail.com")).thenReturn(false);
        when(pendingRepository.findByEmailIgnoreCase("test@gmail.com")).thenReturn(Optional.of(pending));

        Throwable thrown = catchThrowable(() -> authService.requestRegistrationOtp(validRequest("test@gmail.com")));

        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) thrown).getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        org.mockito.Mockito.verifyNoInteractions(otpMailer);
    }

    @Test
    void validOtpCreatesCustomerAndDefaultAddressThenConsumesPendingRegistration() {
        PendingRegistration pending = pending("test@gmail.com", NOW.plusSeconds(60), "123456");
        when(pendingRepository.findByEmailIgnoreCase("test@gmail.com")).thenReturn(Optional.of(pending));
        when(userRepository.existsByEmailIgnoreCase("test@gmail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 73L);
            return saved;
        });
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = authService.verifyRegistrationOtp(
                new VerifyRegistrationOtpRequest("test@gmail.com", "123456")
        );

        assertThat(response.id()).isEqualTo(73L);
        assertThat(response.role()).isEqualTo("CUSTOMER");
        org.mockito.ArgumentCaptor<Address> addressCaptor = org.mockito.ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(addressCaptor.capture());
        assertThat(addressCaptor.getValue().getUserId()).isEqualTo(73L);
        assertThat(addressCaptor.getValue().isDefaultAddress()).isTrue();
        verify(pendingRepository).delete(pending);
    }

    @Test
    void expiredOtpCannotCreateAccount() {
        PendingRegistration pending = pending("test@gmail.com", NOW, "123456");
        when(pendingRepository.findByEmailIgnoreCase("test@gmail.com")).thenReturn(Optional.of(pending));

        Throwable thrown = catchThrowable(() -> authService.verifyRegistrationOtp(
                new VerifyRegistrationOtpRequest("test@gmail.com", "123456")
        ));

        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) thrown).getStatusCode()).isEqualTo(HttpStatus.GONE);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void wrongOtpIsRejectedAndAttemptCountIsSaved() {
        PendingRegistration pending = pending("test@gmail.com", NOW.plusSeconds(60), "123456");
        when(pendingRepository.findByEmailIgnoreCase("test@gmail.com")).thenReturn(Optional.of(pending));
        when(pendingRepository.save(any(PendingRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Throwable thrown = catchThrowable(() -> authService.verifyRegistrationOtp(
                new VerifyRegistrationOtpRequest("test@gmail.com", "654321")
        ));

        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        assertThat(pending.getFailedAttempts()).isEqualTo(1);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void fifthWrongOtpInvalidatesPendingRegistration() {
        PendingRegistration pending = pending("test@gmail.com", NOW.plusSeconds(60), "123456");
        pending.setFailedAttempts(4);
        when(pendingRepository.findByEmailIgnoreCase("test@gmail.com")).thenReturn(Optional.of(pending));

        Throwable thrown = catchThrowable(() -> authService.verifyRegistrationOtp(
                new VerifyRegistrationOtpRequest("test@gmail.com", "654321")
        ));

        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        verify(pendingRepository).delete(pending);
        verify(userRepository, never()).save(any(User.class));
    }

    private RegisterRequest validRequest(String email) {
        return new RegisterRequest("Test User", "0912345678", "10 Example Street", email, "Strong#Pass1");
    }

    private PendingRegistration pending(String email, Instant expiresAt, String otp) {
        PendingRegistration pending = new PendingRegistration();
        ReflectionTestUtils.setField(pending, "id", 1L);
        pending.setFullName("Test User");
        pending.setPhone("0912345678");
        pending.setAddressLine("10 Example Street");
        pending.setEmail(email);
        pending.setPasswordHash(passwordEncoder.encode("Strong#Pass1"));
        pending.setOtpHash(passwordEncoder.encode(otp));
        pending.setOtpExpiresAt(expiresAt);
        pending.setFailedAttempts(0);
        return pending;
    }

    private static class MutableClock extends Clock {
        private final Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
