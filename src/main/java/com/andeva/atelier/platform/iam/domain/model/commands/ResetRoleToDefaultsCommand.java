package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;

import java.util.Objects;

/**
 * Domain command to reset a factory role's permissions back to platform template defaults.
 *
 * @author Joel Huamani Estefanero
 */
public record ResetRoleToDefaultsCommand(
        RoleId roleId
) {
    public ResetRoleToDefaultsCommand {
        Objects.requireNonNull(roleId, "Role identifier cannot be null");
    }
}
