package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;

/**
 * Thrown when attempting to register a Tenant whose Tax ID (RUC) is already registered in the platform.
 *
 * @author Joel Huamani Estefanero
 */
public class TenantAlreadyExistsException extends IamDomainException {

    public TenantAlreadyExistsException(TaxId taxId) {
        super("TENANT_ALREADY_EXISTS", "A workshop Tenant with Tax ID (RUC) " + (taxId != null ? taxId.value() : "null") + " already exists");
    }

    public TenantAlreadyExistsException(String message) {
        super("TENANT_ALREADY_EXISTS", message);
    }
}
