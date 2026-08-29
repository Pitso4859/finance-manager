package com.financemanager.util;

import com.financemanager.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

public final class Validation {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private Validation() {}

    public static String requiredText(String value, String field, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) throw new ValidationException(field + " is required.");
        if (normalized.length() > maxLength) throw new ValidationException(field + " is too long.");
        return normalized;
    }

    public static String email(String value) {
        String normalized = requiredText(value, "Email", 160).toLowerCase();
        if (!EMAIL.matcher(normalized).matches()) throw new ValidationException("Enter a valid email address.");
        return normalized;
    }

    public static BigDecimal positiveAmount(String value, String field) {
        try {
            BigDecimal amount = new BigDecimal(requiredText(value, field, 32));
            if (amount.signum() <= 0) throw new ValidationException(field + " must be greater than zero.");
            if (amount.scale() > 2) amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
            return amount;
        } catch (NumberFormatException ex) {
            throw new ValidationException(field + " must be a valid number.");
        }
    }

    public static LocalDate date(String value, String field) {
        try {
            return LocalDate.parse(requiredText(value, field, 10));
        } catch (java.time.format.DateTimeParseException ex) {
            throw new ValidationException(field + " must use YYYY-MM-DD format.");
        }
    }

    public static int month(int value) {
        if (value < 1 || value > 12) throw new ValidationException("Month must be between 1 and 12.");
        return value;
    }

    public static int year(int value) {
        if (value < 2000 || value > 2200) throw new ValidationException("Year must be between 2000 and 2200.");
        return value;
    }
}
