package vn.edu.hcmute.fashion.auth;

public record RegisterResponse(
        Long id,
        String fullName,
        String email,
        String role
) {}