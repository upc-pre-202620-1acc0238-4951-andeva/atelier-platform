package com.andeva.atelier.platform.crm.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Query for retrieving a customer by document type and number.
 *
 * @author Adiel Sanchez Santin
 */
public record GetCustomerByDocumentQuery(
        TenantId tenantId,
        String documentType,
        String documentNumber
) {
    public GetCustomerByDocumentQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(documentType, "DocumentType cannot be null");
        Objects.requireNonNull(documentNumber, "DocumentNumber cannot be null");
    }
}
