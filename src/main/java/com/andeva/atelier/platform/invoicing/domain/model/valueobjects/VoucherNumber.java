package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;

import java.io.Serializable;

/**
 * Sequential positive integer correlative of an electronic voucher.
 * Formatted as an 8-digit zero-padded number per SUNAT standard (e.g. 00000001).
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherNumber(int value) implements Serializable, Comparable<VoucherNumber> {

    public static final int MIN_VALUE = 1;
    public static final int MAX_VALUE = 99_999_999;

    public VoucherNumber {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new InvoicingDomainException(
                    "ERR_INVALID_VOUCHER_NUMBER",
                    "Voucher number must be between " + MIN_VALUE + " and " + MAX_VALUE + ", but was: " + value
            );
        }
    }

    public static VoucherNumber of(int value) {
        return new VoucherNumber(value);
    }

    public static VoucherNumber of(String value) {
        try {
            return new VoucherNumber(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            throw new InvoicingDomainException("ERR_INVALID_VOUCHER_NUMBER", "Cannot parse voucher number: " + value);
        }
    }

    public String format() {
        return String.format("%08d", value);
    }

    @Override
    public int compareTo(VoucherNumber other) {
        return Integer.compare(this.value, other.value);
    }
}
