package com.andeva.atelier.platform.iam.application.internal.queryservices;

import com.andeva.atelier.platform.iam.application.queryservices.RoleQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.queries.GetAllPermissionsQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRoleByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRolesByTenantIdQuery;
import com.andeva.atelier.platform.iam.domain.repositories.PermissionRepository;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Transactional read-only query service implementation for RBAC Roles and catalog Permissions.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class RoleQueryServiceImpl implements RoleQueryService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleQueryServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = Objects.requireNonNull(roleRepository, "RoleRepository cannot be null");
        this.permissionRepository = Objects.requireNonNull(permissionRepository, "PermissionRepository cannot be null");
    }

    @Override
    public Optional<Role> handle(GetRoleByIdQuery query) {
        Objects.requireNonNull(query, "GetRoleByIdQuery cannot be null");
        return roleRepository.findById(query.roleId());
    }

    @Override
    public List<Role> handle(GetRolesByTenantIdQuery query) {
        Objects.requireNonNull(query, "GetRolesByTenantIdQuery cannot be null");
        return roleRepository.findByTenantId(query.tenantId());
    }

    @Override
    public List<Permission> handle(GetAllPermissionsQuery query) {
        Objects.requireNonNull(query, "GetAllPermissionsQuery cannot be null");
        return permissionRepository.findAll();
    }
}
