package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;

import java.util.Objects;

/**
 * Domain command to delete a custom security role from a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record DeleteCustomRoleCommand(
        RoleId roleId
) {
    public DeleteCustomRoleCommand {
        Objects.requireNonNull(roleId, "Role identifier cannot be null");
    }
}
