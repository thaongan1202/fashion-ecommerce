package vn.edu.hcmute.fashion.auth;

public record RegistrationOtpResponse(String email, int expiresInSeconds, String message) {}
