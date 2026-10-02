package vn.edu.hcmute.fashion.auth;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        LoginUser user
) {
    public record LoginUser(
            Long id,
            String fullName,
            String email,
            String role
    ) {}
}