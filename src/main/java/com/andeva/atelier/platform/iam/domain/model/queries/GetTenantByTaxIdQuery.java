package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;

import java.util.Objects;

/**
 * Domain query to retrieve an automotive workshop Tenant by its tax identification number (RUC).
 *
 * @author Joel Huamani Estefanero
 */
public record GetTenantByTaxIdQuery(
        TaxId taxId
) {
    public GetTenantByTaxIdQuery {
        Objects.requireNonNull(taxId, "Tax ID cannot be null");
    }
}
