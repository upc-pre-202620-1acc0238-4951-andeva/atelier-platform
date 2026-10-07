package com.andeva.atelier.platform.invoicing.domain.model.enums;

import java.util.Objects;

/**
 * Official SUNAT Catalog 06 identity document types.
 *
 * @author Joel Huamani Estefanero
 */
public enum DocumentType {
    DNI("1", "Documento Nacional de Identidad", 8),
    CE("4", "Carnet de Extranjería", 12),
    RUC("6", "Registro Único de Contribuyentes", 11),
    PASAPORTE("7", "Pasaporte", 12);

    private final String sunatCode;
    private final String description;
    private final int maxLength;

    DocumentType(String sunatCode, String description, int maxLength) {
        this.sunatCode = Objects.requireNonNull(sunatCode, "SUNAT code cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.maxLength = maxLength;
    }

    public String getSunatCode() {
        return sunatCode;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public static DocumentType fromSunatCode(String code) {
        for (DocumentType type : values()) {
            if (type.sunatCode.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SUNAT document type code: " + code);
    }
}
