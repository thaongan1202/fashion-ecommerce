package vn.edu.hcmute.fashion.user;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String role
) {}