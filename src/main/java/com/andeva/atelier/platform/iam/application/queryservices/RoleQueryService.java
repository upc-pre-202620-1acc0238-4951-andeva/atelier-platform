package com.andeva.atelier.platform.iam.application.queryservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.queries.GetAllPermissionsQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRoleByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRolesByTenantIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Public query service interface for reading RBAC Roles and catalog Permissions.
 *
 * @author Joel Huamani Estefanero
 */
public interface RoleQueryService {

    Optional<Role> handle(GetRoleByIdQuery query);

    List<Role> handle(GetRolesByTenantIdQuery query);

    List<Permission> handle(GetAllPermissionsQuery query);
}
