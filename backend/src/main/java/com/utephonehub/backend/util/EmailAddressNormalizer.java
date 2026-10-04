package com.utephonehub.backend.util;

import java.util.Locale;

public final class EmailAddressNormalizer {

    private EmailAddressNormalizer() {
    }

    public static String normalize(String email) {
        if (email == null) {
            return null;
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);
        int atIndex = normalized.lastIndexOf('@');
        if (atIndex <= 0 || atIndex == normalized.length() - 1) {
            return normalized;
        }

        String localPart = normalized.substring(0, atIndex);
        String domain = normalized.substring(atIndex + 1);
        if (domain.equals("gmail.com") || domain.equals("googlemail.com")) {
            int plusIndex = localPart.indexOf('+');
            if (plusIndex >= 0) {
                localPart = localPart.substring(0, plusIndex);
            }
            localPart = localPart.replace(".", "");
            domain = "gmail.com";
        }

        return localPart + "@" + domain;
    }
}
