package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Thrown when a security role cannot be located within a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public class RoleNotFoundException extends IamDomainException {

    public RoleNotFoundException(RoleId roleId) {
        super("ROLE_NOT_FOUND", "Security role not found with identifier: " + (roleId != null ? roleId.value() : "null"));
    }

    public RoleNotFoundException(TenantId tenantId, String roleName) {
        super("ROLE_NOT_FOUND", "Security role '" + roleName + "' not found in workshop tenant: " + (tenantId != null ? tenantId.value() : "null"));
    }

    public RoleNotFoundException(String message) {
        super("ROLE_NOT_FOUND", message);
    }
}
