package vn.edu.hcmute.fashion.user;

public record AddressResponse(
        Long id,
        String recipientName,
        String phone,
        String addressLine,
        boolean defaultAddress
) {}