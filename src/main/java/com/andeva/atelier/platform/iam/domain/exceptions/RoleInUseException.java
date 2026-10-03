package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;

/**
 * Thrown when an attempt is made to delete a custom security role that is currently assigned to active staff members.
 *
 * @author Joel Huamani Estefanero
 */
public class RoleInUseException extends IamDomainException {

    public RoleInUseException(RoleId roleId) {
        super("ROLE_IN_USE", "Cannot delete role " + (roleId != null ? roleId.value() : "null")
                + " because it is currently assigned to one or more active staff members");
    }

    public RoleInUseException(String message) {
        super("ROLE_IN_USE", message);
    }
}
