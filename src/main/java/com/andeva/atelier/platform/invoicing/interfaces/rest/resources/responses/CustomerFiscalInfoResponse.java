package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;

/**
 * Presentation resource detailing customer fiscal identification data.
 *
 * @author Joel Huamani Estefanero
 */
public record CustomerFiscalInfoResponse(
        String taxId,
        String legalName,
        String fiscalAddress,
        String documentType
) implements Serializable {
}
