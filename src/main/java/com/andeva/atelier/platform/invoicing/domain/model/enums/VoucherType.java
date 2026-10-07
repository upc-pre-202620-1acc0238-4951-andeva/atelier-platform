package com.andeva.atelier.platform.invoicing.domain.model.enums;

import java.util.Objects;

/**
 * Official SUNAT Catalog 01 electronic voucher types.
 *
 * @author Joel Huamani Estefanero
 */
public enum VoucherType {
    FACTURA("01", "Factura Electrónica"),
    BOLETA("03", "Boleta de Venta Electrónica"),
    NOTA_CREDITO("07", "Nota de Crédito Electrónica");

    private final String sunatCode;
    private final String description;

    VoucherType(String sunatCode, String description) {
        this.sunatCode = Objects.requireNonNull(sunatCode, "SUNAT code cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
    }

    public String getSunatCode() {
        return sunatCode;
    }

    public String getDescription() {
        return description;
    }

    public static VoucherType fromSunatCode(String code) {
        for (VoucherType type : values()) {
            if (type.sunatCode.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SUNAT voucher code: " + code);
    }
}
