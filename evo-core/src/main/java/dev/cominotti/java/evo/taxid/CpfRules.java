package dev.cominotti.java.evo.taxid;

/**
 * Validation rules, resource bundle keys, and algorithmic methods for CPF (Brazilian individual tax ID).
 *
 * <p>Message constants are Jakarta Validation interpolation keys ({@code "{evo.cpf.blank}"})
 * resolved from {@code ValidationMessages.properties}. They are compile-time constants,
 * valid for use in annotation {@code message} attributes.</p>
 */
public final class CpfRules {

    private CpfRules() {}

    public static final String REGEX = "\\d{11}";
    public static final int DIGIT_COUNT = 11;

    public static final String BLANK_MESSAGE = "{evo.cpf.blank}";
    public static final String FORMAT_MESSAGE = "{evo.cpf.format}";
    public static final String ALL_SAME_DIGIT_MESSAGE = "{evo.cpf.allSameDigit}";
    public static final String CHECK_DIGIT_MESSAGE = "{evo.cpf.checkDigit}";
    public static final String NULL_MESSAGE = "{evo.cpf.null}";

    public static boolean hasValidCheckDigits(String digits) {
        int d1 = checkDigit(digits, 9, 10);
        int d2 = checkDigit(digits, 10, 11);
        return digits.charAt(9) - '0' == d1
                && digits.charAt(10) - '0' == d2;
    }

    private static int checkDigit(String digits, int count, int startWeight) {
        int sum = 0;
        for (int i = 0; i < count; i++) {
            sum += (digits.charAt(i) - '0') * (startWeight - i);
        }
        return TaxIdRules.mod11Remainder(sum);
    }
}
