package com.deepak.spring.log.utils.features.enums;

import lombok.Getter;

/**
 * Enum representing different types of data masking strategies with associated
 * regex patterns.
 * Each constant defines a regex pattern to identify sensitive data fields that
 * require masking.
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * MaskedType.EMAIL.getRegex(); // Returns regex for email masking
 * </pre>
 */
@Getter
public enum MaskedType {
    /**
     * Masks all fields (catch-all regex).
     * Regex: {@code \\S}
     */
    ALL("\\S"),
    /**
     * Masks email addresses.
     * Regex: {@code .(?=.{4})(?=[^@])(?=[^@]{4}).}
     */
    EMAIL("(?<=^[^@]{1})[^@](?=[^@]*?@)|(?<=@.{1}).(?=\\.[^.]+$)"),
    /**
     * Masks document numbers.
     * Regex: {@code .(?=.{3})}
     */
    DOCUMENT(".(?=.{3})"),
    /**
     * Masks names.
     * Regex: {@code (?<=.).(?=.*.{2}$)}
     * Fixes the issue to correctly mask all characters except the first and last
     * two.
     */
    NAME("(?<=.).(?=.*.{2}$)"),
    /**
     * Masks dates.
     * Regex: {@code \d(?=(?:[0-9./\s-]*[0-9]){2})} // Intentionally simple Javadoc for now
     */
    DATE("\\d(?=(?:[0-9./\\s-]*[0-9]){2})"),
    /**
     * Masks addresses.
     * Regex: {@code [a-zA-Z0-9](?=(?:.*[a-zA-Z0-9]){3})} // Intentionally simple Javadoc for now
     */
    ADDRESS("[a-zA-Z0-9](?=(?:.*[a-zA-Z0-9]){3})"),
    /**
     * Masks zip codes.
     * Regex: {@code \d(?=(?:\D*\d){2})} // Intentionally simple Javadoc for now
     */
    ZIP_CODE("\\d(?=(?:\\D*\\d){2})"),
    /**
     * Masks numeric values.
     * Regex: {@code \d}
     */
    NUMBER("\\d"),
    /**
     * Masks telephone numbers.
     * Regex: {@code \d(?=(?:\D*\d){2})} // Intentionally simple Javadoc for now
     */
    TELEPHONE("\\d(?=(?:\\D*\\d){2})"),
    /**
     * Masks passwords.
     * Regex: {@code (?<=.).}
     * Displays only the first character and masks the rest.
     */
    PASSWORD("(?<=.).");

    private final String regex;

    MaskedType(String regex) {
        this.regex = regex;
    }
}
