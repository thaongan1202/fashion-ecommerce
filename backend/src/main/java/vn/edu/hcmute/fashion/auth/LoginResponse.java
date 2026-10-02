package vn.edu.hcmute.fashion.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long id,
        String fullName,
        String email,
        String role
) {}