package com.utephonehub.backend.dto.request.auth;

import com.utephonehub.backend.enums.EGender;
import com.utephonehub.backend.util.VietnameseMobilePhoneValidator;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String username;

    private String fullName;

    private String email;

    @Pattern(
            regexp = "^$|" + VietnameseMobilePhoneValidator.PATTERN,
            message = "Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 0 và có đầu số di động hợp lệ tại Việt Nam"
    )
    private String phoneNumber;

    private EGender gender;

    private LocalDate dateOfBirth;

    private String password;

    private String confirmPassword;
}

