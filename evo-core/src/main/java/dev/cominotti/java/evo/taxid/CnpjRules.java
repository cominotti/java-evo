package dev.cominotti.java.evo.taxid;

/**
 * Validation rules, resource bundle keys, and algorithmic methods for CNPJ (Brazilian company tax ID).
 *
 * <p>Message constants are Jakarta Validation interpolation keys ({@code "{evo.cnpj.blank}"})
 * resolved from {@code ValidationMessages.properties}. They are compile-time constants,
 * valid for use in annotation {@code message} attributes.</p>
 */
public final class CnpjRules {

    private CnpjRules() {}

    public static final String REGEX = "\\d{14}";
    public static final int DIGIT_COUNT = 14;

    public static final String BLANK_MESSAGE = "{evo.cnpj.blank}";
    public static final String FORMAT_MESSAGE = "{evo.cnpj.format}";
    public static final String ALL_SAME_DIGIT_MESSAGE = "{evo.cnpj.allSameDigit}";
    public static final String CHECK_DIGIT_MESSAGE = "{evo.cnpj.checkDigit}";
    public static final String NULL_MESSAGE = "{evo.cnpj.null}";

    private static final int[] WEIGHTS_12 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] WEIGHTS_13 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public static boolean hasValidCheckDigits(String digits) {
        int d1 = checkDigit(digits, WEIGHTS_12);
        int d2 = checkDigit(digits, WEIGHTS_13);
        return digits.charAt(12) - '0' == d1
                && digits.charAt(13) - '0' == d2;
    }

    private static int checkDigit(String digits, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += (digits.charAt(i) - '0') * weights[i];
        }
        return TaxIdRules.mod11Remainder(sum);
    }
}
