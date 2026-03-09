package dev.cominotti.java.evo.validation;

/**
 * Shared validation rules and resource bundle keys for the CpfOrCnpj sealed interface.
 *
 * <p>Message constants are Jakarta Validation interpolation keys resolved from
 * {@code ValidationMessages.properties}. The mod-11 remainder formula is shared
 * between CPF and CNPJ check digit algorithms.</p>
 */
public final class TaxIdRules {

    private TaxIdRules() {}

    public static final String TAX_ID_BLANK_MESSAGE = "{evo.taxId.blank}";
    public static final String TAX_ID_LENGTH_MESSAGE = "{evo.taxId.length}";

    public static int mod11Remainder(int sum) {
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
