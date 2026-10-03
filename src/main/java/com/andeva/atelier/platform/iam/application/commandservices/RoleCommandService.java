package com.andeva.atelier.platform.iam.application.commandservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateCustomRoleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.DeleteCustomRoleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ProvisionTenantRolesCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetRoleToDefaultsCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateRolePermissionsCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

import java.util.List;

/**
 * Public command service interface orchestrating RBAC Role management operations.
 *
 * @author Joel Huamani Estefanero
 */
public interface RoleCommandService {

    Result<Role, ApplicationError> handle(CreateCustomRoleCommand command);

    Result<Role, ApplicationError> handle(UpdateRolePermissionsCommand command);

    Result<Role, ApplicationError> handle(ResetRoleToDefaultsCommand command);

    Result<Void, ApplicationError> handle(DeleteCustomRoleCommand command);

    Result<List<Role>, ApplicationError> handle(ProvisionTenantRolesCommand command);
}
