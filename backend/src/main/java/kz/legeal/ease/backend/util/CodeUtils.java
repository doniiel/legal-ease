package kz.legeal.ease.backend.util;

import java.security.SecureRandom;
import java.util.UUID;

public class CodeUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DIGITS = "0123456789";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private CodeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String generateVerificationCode() {
        return generateNumericCode(6);
    }

    public static String generateNumericCode(int length) {
        validateLength(length);

        final var sb = new StringBuilder(length);
        for (var i = 0; i < length; i++) {
            sb.append(DIGITS.charAt(SECURE_RANDOM.nextInt(DIGITS.length())));
        }
        return sb.toString();
    }

    public static String generateAlphanumericCode(int length) {
        validateLength(length);

        final var sb = new StringBuilder(length);
        for (var i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private static void validateLength(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Code length must be greater than 0");
        }
    }
}
