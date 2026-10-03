package vn.edu.hcmute.fashion.auth;

public interface RegistrationOtpMailer {
    void sendOtp(String recipient, String otp);
}
