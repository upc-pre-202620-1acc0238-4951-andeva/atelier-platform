package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;

/**
 * Thrown when an attempt is made to physically or logically delete a protected system factory role.
 *
 * @author Joel Huamani Estefanero
 */
public class SystemRoleImmutableException extends IamDomainException {

    public SystemRoleImmutableException(RoleId roleId) {
        super("SYSTEM_ROLE_IMMUTABLE", "Protected system factory role " + (roleId != null ? roleId.value() : "null")
                + " cannot be deleted. Permissions may be customized or reset, but the role itself is immutable.");
    }

    public SystemRoleImmutableException(String message) {
        super("SYSTEM_ROLE_IMMUTABLE", message);
    }
}
