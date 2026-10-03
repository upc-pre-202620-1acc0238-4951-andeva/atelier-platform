package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Thrown when an automotive workshop Tenant cannot be located by its identifier or tax ID.
 *
 * @author Joel Huamani Estefanero
 */
public class TenantNotFoundException extends IamDomainException {

    public TenantNotFoundException(TenantId tenantId) {
        super("TENANT_NOT_FOUND", "Workshop Tenant not found with identifier: " + (tenantId != null ? tenantId.value() : "null"));
    }

    public TenantNotFoundException(TaxId taxId) {
        super("TENANT_NOT_FOUND", "Workshop Tenant not found with Tax ID (RUC): " + (taxId != null ? taxId.value() : "null"));
    }

    public TenantNotFoundException(String message) {
        super("TENANT_NOT_FOUND", message);
    }
}
