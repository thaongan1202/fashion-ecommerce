package com.utephonehub.backend.util;

import java.util.regex.Pattern;

public final class VietnameseMobilePhoneValidator {

    public static final String PATTERN = "^0(?:32|33|34|35|36|37|38|39|52|55|56|58|59|70|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|96|97|98|99)\\d{7}$";

    private static final Pattern PHONE_PATTERN = Pattern.compile(PATTERN);

    private VietnameseMobilePhoneValidator() {
    }

    public static boolean isValid(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }
}
