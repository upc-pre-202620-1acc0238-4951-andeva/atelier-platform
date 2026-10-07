package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Official SUNAT 4-character alphanumeric fiscal series.
 * Format complies with regex: {@code ^[F|B|T][A-Z0-9]{3}$}.
 * Examples: F001 (Factura), B001 (Boleta), FC01 (Factura Credit Note), BC01 (Boleta Credit Note).
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherSerie(String value) implements Serializable {

    private static final Pattern SERIE_PATTERN = Pattern.compile("^[FBT][A-Z0-9]{3}$");

    public VoucherSerie {
        Objects.requireNonNull(value, "Voucher series cannot be null");
        value = value.trim().toUpperCase();
        if (!SERIE_PATTERN.matcher(value).matches()) {
            throw new InvoicingDomainException(
                    "ERR_INVALID_VOUCHER_SERIE",
                    "Fiscal series '" + value + "' is invalid. Must be 4 alphanumeric characters starting with F, B, or T (e.g. F001, B001, FC01)."
            );
        }
    }

    public static VoucherSerie of(String value) {
        return new VoucherSerie(value);
    }
}
