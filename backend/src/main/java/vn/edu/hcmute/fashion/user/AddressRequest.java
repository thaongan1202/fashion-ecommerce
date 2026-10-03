package vn.edu.hcmute.fashion.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank
        @Size(max = 150)
        String recipientName,

        @NotBlank
        @Pattern(regexp = "^0[0-9]{9}$")
        String phone,

        @NotBlank
        @Size(max = 500)
        String addressLine,

        boolean defaultAddress
) {}