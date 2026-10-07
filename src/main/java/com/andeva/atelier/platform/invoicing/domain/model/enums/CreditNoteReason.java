package com.andeva.atelier.platform.invoicing.domain.model.enums;

import java.util.Objects;

/**
 * Official SUNAT Catalog 09 reason codes for credit note issuance.
 *
 * @author Joel Huamani Estefanero
 */
public enum CreditNoteReason {
    ANULACION_DE_LA_OPERACION("01", "Anulación de la operación"),
    ANULACION_POR_ERROR_EN_EL_RUC("02", "Anulación por error en el RUC"),
    CORRECCION_POR_ERROR_EN_LA_DESCRIPCION("03", "Corrección por error en la descripción"),
    DESCUENTO_GLOBAL("04", "Descuento global"),
    DEVOLUCION_TOTAL("07", "Devolución total");

    private final String sunatCode;
    private final String description;

    CreditNoteReason(String sunatCode, String description) {
        this.sunatCode = Objects.requireNonNull(sunatCode, "SUNAT code cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
    }

    public String getSunatCode() {
        return sunatCode;
    }

    public String getDescription() {
        return description;
    }

    public static CreditNoteReason fromSunatCode(String code) {
        for (CreditNoteReason reason : values()) {
            if (reason.sunatCode.equalsIgnoreCase(code)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown SUNAT credit note reason code: " + code);
    }
}
