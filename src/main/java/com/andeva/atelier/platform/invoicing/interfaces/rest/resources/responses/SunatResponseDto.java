package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;

/**
 * Presentation resource detailing the official SUNAT acceptance or rejection response.
 *
 * @author Joel Huamani Estefanero
 */
public record SunatResponseDto(
        String responseCode,
        String description,
        String digitalSignatureHash
) implements Serializable {
}
