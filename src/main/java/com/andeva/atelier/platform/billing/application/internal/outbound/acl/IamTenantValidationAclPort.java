package com.andeva.atelier.platform.billing.application.internal.outbound.acl;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Outbound Anti-Corruption Layer (ACL) port for querying tenant identity and registration
 * attributes hosted in the IAM & Tenancy Bounded Context.
 *
 * @author Joel Huamani Estefanero
 */
public interface IamTenantValidationAclPort {

    /**
     * Checks if a tenant exists in the platform.
     *
     * @param tenantId workshop tenant identifier
     * @return true if registered in IAM
     */
    boolean tenantExists(TenantId tenantId);

    /**
     * Retrieves the official legal business name of the workshop.
     *
     * @param tenantId workshop tenant identifier
     * @return legal corporate name
     */
    String getTenantLegalName(TenantId tenantId);

    /**
     * Retrieves the administrative contact email for billing communication.
     *
     * @param tenantId workshop tenant identifier
     * @return contact email string
     */
    String getTenantContactEmail(TenantId tenantId);
}
