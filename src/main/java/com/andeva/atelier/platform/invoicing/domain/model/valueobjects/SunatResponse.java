package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;

/**
 * Official validation response from SUNAT / authorized electronic service provider (PSE).
 *
 * @author Joel Huamani Estefanero
 */
public record SunatResponse(
        String responseCode,
        String description,
        String digitalSignatureHash
) implements Serializable {

    public SunatResponse {
        responseCode = responseCode != null ? responseCode.trim() : "";
        description = description != null ? description.trim() : "";
        digitalSignatureHash = digitalSignatureHash != null ? digitalSignatureHash.trim() : "";
    }

    public static SunatResponse of(String responseCode, String description, String digitalSignatureHash) {
        return new SunatResponse(responseCode, description, digitalSignatureHash);
    }

    public boolean isAccepted() {
        return "0".equals(responseCode) || "0000".equals(responseCode);
    }
}
