package vn.edu.hcmute.fashion.admin;

import java.time.OffsetDateTime;
import java.util.List;

public final class AdminUserDtos {
    private AdminUserDtos() {}
    public record UserStatusRequest(String status) {}
    public record UserSummary(long id, String fullName, String email, String phone, String role,
                              String status, OffsetDateTime createdAt) {}
    public record UserPage(List<UserSummary> content, long totalElements, int page, int size, int totalPages) {}
}
