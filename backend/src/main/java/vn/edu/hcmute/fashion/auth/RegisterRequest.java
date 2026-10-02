package vn.edu.hcmute.fashion.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(max = 150)
        String fullName,

        @NotBlank
        @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0")
        String phone,

        @NotBlank
        @Size(max = 500)
        String addressLine,

        @NotBlank
        @Email
        @Size(max = 254)
        @Pattern(
                regexp = "(?i)^[^@\\s]+@gmail\\.com$",
                message = "Email phải có dạng @gmail.com"
        )
        String email,

        @NotBlank
        @Size(min = 8, max = 32)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).*$",
                message = "Mật khẩu cần có chữ hoa, chữ thường, số và ký tự đặc biệt"
        )
        String password
) {}
