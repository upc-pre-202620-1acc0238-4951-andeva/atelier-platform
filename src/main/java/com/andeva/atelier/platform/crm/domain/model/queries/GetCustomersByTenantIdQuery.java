package com.andeva.atelier.platform.crm.domain.model.queries;

import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Lists the customer portfolio of a workshop with optional filters.
 * Null filter values mean no filtering on that attribute.
 */
public record GetCustomersByTenantIdQuery(
        TenantId tenantId,
        CustomerType type,
        String search,
        CustomerStatus status
) {
}
