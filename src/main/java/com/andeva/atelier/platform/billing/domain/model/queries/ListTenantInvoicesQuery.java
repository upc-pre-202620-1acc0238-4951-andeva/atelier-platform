package com.andeva.atelier.platform.billing.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Query to retrieve the complete SaaS invoice receipts history for a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record ListTenantInvoicesQuery(
        TenantId tenantId
) {

    public ListTenantInvoicesQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
    }
}
