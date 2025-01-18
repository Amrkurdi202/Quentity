package com.quentity.misc;

/**
 * Utility class containing predefined regular expression patterns for common validation tasks.
 */
public class Patterns {

    /**
     * Matches alphabetical characters and spaces from any language (Unicode support).
     * Suitable for validating names or any text containing only letters and spaces.
     */
    public static final String ALPHABETICAL = "^[\\p{L}\\s]+$";

    /**
     * Matches alphabetical characters, spaces, and dashes from any language (Unicode support).
     * Useful for names or text with hyphenated words.
     */
    public static final String ALPHABETICAL_WITH_DASH = "^[\\p{L}\\s\\-]+$";

    /**
     * Matches alphanumeric characters and spaces from any language (Unicode support).
     * Suitable for general input where letters, numbers, and spaces are allowed.
     */
    public static final String ALPHANUMERIC = "^[\\p{L}\\p{N}\\s]+$";

    /**
     * Matches alphanumeric characters, spaces, and dashes from any language (Unicode support).
     * Useful for validating identifiers or text containing both numbers and letters with optional dashes.
     */
    public static final String ALPHANUMERIC_WITH_DASH = "^[\\p{L}\\p{N}\\s\\-]+$";

    /**
     * Matches alphanumeric characters, spaces, dashes and slashes from any language (Unicode support).
     * Useful for validating identifiers or text containing both numbers and letters with optional dashes.
     */
    public static final String ALPHANUMERIC_WITH_DASH_SLASH = "^[\\p{L}\\p{N}\\s\\-\\/]+$";

    /**
     * Matches positive or negative whole numbers.
     * Can be used to validate integer inputs.
     */
    public static final String INTEGER = "^[+-]?\\d+$";

    /**
     * Matches positive or negative decimal numbers.
     * Suitable for inputs where fractional values are allowed.
     */
    public static final String FLOAT = "^[+-]?\\d+(\\.\\d+)?$";

    /**
     * Matches currency values with up to two decimal places.
     * Commonly used for validating monetary amounts.
     */
    public static final String CURRENCY = "^[+-]?\\d+(\\.\\d{1,2})?$";

    /**
     * Matches names containing letters, spaces, dashes, and periods.
     * Supports international naming conventions.
     */
    public static final String NAME = "^[\\p{L}\\s\\-\\.]+$";

    /**
     * Matches phone numbers in international format.
     * Allows optional '+' and symbols like dashes, spaces, and parentheses.
     */
    public static final String PHONE_NUMBER = "^\\+?[0-9\\-\\s()]+$";

    /**
     * Matches standard email addresses.
     * Ensures proper email formatting with an '@' symbol and a valid domain.
     */
    public static final String EMAIL = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    /**
     * Matches HTTP or HTTPS URLs with optional path segments.
     * Useful for validating website links.
     */
    public static final String URL = "^(https?://)?[\\w.-]+(\\.[a-zA-Z]{2,})+(/[\\w.-]*)*$";

    /**
     * Matches hexadecimal color codes with optional '#' prefix.
     * Supports 3-digit or 6-digit hexadecimal values.
     */
    public static final String HEX_COLOR = "^#?([a-fA-F0-9]{3}|[a-fA-F0-9]{6})$";

    /**
     * Matches general hexadecimal numbers.
     * Useful for validating hex-based identifiers or codes.
     */
    public static final String HEX = "^[a-fA-F0-9]+$";

    /**
     * Matches dates in ISO 8601 format (YYYY-MM-DD).
     * Commonly used for validating date inputs.
     */
    public static final String DATE_ISO_8601 = "^\\d{4}-\\d{2}-\\d{2}$";

    /**
     * Matches time in 24-hour format (HH:mm:ss).
     * Suitable for time inputs without AM/PM.
     */
    public static final String TIME_24H = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$";

    /**
     * Matches strong passwords with at least one uppercase letter, one lowercase letter, one digit, one special character, and a minimum length of 8.
     * Suitable for enforcing secure password policies.
     */
    public static final String PASSWORD_STRONG = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

    /**
     * Matches alphanumeric postal codes with optional dashes and spaces.
     * Supports a variety of international postal code formats.
     */
    public static final String POSTAL_CODE = "^[\\w\\d\\-\\s]+$";

    /**
     * Matches street addresses containing letters, numbers, dashes, commas, and periods.
     * Useful for validating formatted address inputs.
     */
    public static final String STREET_ADDRESS = "^[\\p{L}\\d\\s\\-\\,\\.]+$";

    /**
     * Matches common file extensions, including image, document, and text file types.
     * Useful for validating uploaded file types.
     */
    public static final String FILE_EXTENSION = "^\\.(jpg|jpeg|png|gif|pdf|doc|docx|xls|xlsx|csv|txt)$";

    /**
     * Matches user input excluding angle brackets.
     * Prevents HTML injection by disallowing '<' and '>'.
     */
    public static final String CUSTOM_TEXT = "^[^<>]+$";

    /**
     * Matches user comments containing letters, numbers, spaces, and basic punctuation.
     * Useful for text areas or feedback forms.
     */
    public static final String CUSTOM_COMMENT = "^[\\p{L}\\p{N}\\s\\.,?!'\"\\-]+$";

    public static final String NUMERIC = "^\\p{N}+$";
}
