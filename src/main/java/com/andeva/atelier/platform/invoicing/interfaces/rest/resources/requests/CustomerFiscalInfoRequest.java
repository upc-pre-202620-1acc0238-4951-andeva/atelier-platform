package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * Request payload containing recipient customer legal and fiscal identification data.
 *
 * @author Joel Huamani Estefanero
 */
public record CustomerFiscalInfoRequest(
        @NotBlank(message = "{error.customer.tax_id.required}")
        @Size(min = 8, max = 11, message = "{error.customer.tax_id.length}")
        String taxId,

        @NotBlank(message = "{error.customer.legal_name.required}")
        @Size(max = 200, message = "{error.customer.legal_name.length}")
        String legalName,

        @Size(max = 250, message = "{error.customer.fiscal_address.length}")
        String fiscalAddress,

        @NotBlank(message = "{error.customer.document_type.required}")
        String documentType
) implements Serializable {
}
