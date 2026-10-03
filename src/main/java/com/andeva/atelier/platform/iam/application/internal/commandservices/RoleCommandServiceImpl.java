package com.andeva.atelier.platform.iam.application.internal.commandservices;

import com.andeva.atelier.platform.iam.application.commandservices.RoleCommandService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateCustomRoleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.DeleteCustomRoleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ProvisionTenantRolesCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetRoleToDefaultsCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateRolePermissionsCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.repositories.PermissionRepository;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Transactional orchestrator implementing RBAC role creation, permission updates, and template resets.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final TenantMembershipRepository membershipRepository;

    public RoleCommandServiceImpl(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            TenantMembershipRepository membershipRepository
    ) {
        this.roleRepository = Objects.requireNonNull(roleRepository, "RoleRepository cannot be null");
        this.permissionRepository = Objects.requireNonNull(permissionRepository, "PermissionRepository cannot be null");
        this.membershipRepository = Objects.requireNonNull(membershipRepository, "TenantMembershipRepository cannot be null");
    }

    @Override
    public Result<Role, ApplicationError> handle(CreateCustomRoleCommand command) {
        Objects.requireNonNull(command, "CreateCustomRoleCommand cannot be null");

        if (roleRepository.existsByTenantIdAndName(command.tenantId(), command.name())) {
            return Result.failure(ApplicationError.conflict(
                    "A security role with name '" + command.name() + "' already exists in this workshop tenant"));
        }

        Set<Permission> resolvedPermissions = resolvePermissions(command.permissionIds());
        Role role = Role.createCustom(command.tenantId(), command.name(), command.description(), resolvedPermissions);
        Role savedRole = roleRepository.save(role);

        return Result.success(savedRole);
    }

    @Override
    public Result<Role, ApplicationError> handle(UpdateRolePermissionsCommand command) {
        Objects.requireNonNull(command, "UpdateRolePermissionsCommand cannot be null");

        Optional<Role> optionalRole = roleRepository.findById(command.roleId());
        if (optionalRole.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Role", command.roleId().value().toString()));
        }

        Role role = optionalRole.get();
        Set<Permission> resolvedPermissions = resolvePermissions(command.permissionIds());
        role.updatePermissions(resolvedPermissions);
        Role savedRole = roleRepository.save(role);

        return Result.success(savedRole);
    }

    @Override
    public Result<Role, ApplicationError> handle(ResetRoleToDefaultsCommand command) {
        Objects.requireNonNull(command, "ResetRoleToDefaultsCommand cannot be null");

        Optional<Role> optionalRole = roleRepository.findById(command.roleId());
        if (optionalRole.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Role", command.roleId().value().toString()));
        }

        Role role = optionalRole.get();
        if (!role.isSystemRole()) {
            return Result.failure(ApplicationError.badRequest(
                    "Only protected system roles can be reset to template defaults"));
        }

        RoleTemplate template = Arrays.stream(RoleTemplate.values())
                .filter(t -> t.code().equalsIgnoreCase(role.code()))
                .findFirst()
                .orElse(null);

        if (template == null) {
            return Result.failure(ApplicationError.notFound("RoleTemplate", role.code()));
        }

        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
        role.resetToTemplate(template, allPermissions);
        Role savedRole = roleRepository.save(role);

        return Result.success(savedRole);
    }

    @Override
    public Result<Void, ApplicationError> handle(DeleteCustomRoleCommand command) {
        Objects.requireNonNull(command, "DeleteCustomRoleCommand cannot be null");

        Optional<Role> optionalRole = roleRepository.findById(command.roleId());
        if (optionalRole.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Role", command.roleId().value().toString()));
        }

        Role role = optionalRole.get();
        if (role.isSystemRole()) {
            return Result.failure(ApplicationError.conflict("Protected system factory role cannot be deleted"));
        }

        List<TenantMembership> memberships = membershipRepository.findByTenantId(role.tenantId());
        boolean isAssigned = memberships.stream()
                .anyMatch(m -> m.assignedRoles().stream().anyMatch(r -> r.id().equals(role.id())));

        if (isAssigned) {
            return Result.failure(ApplicationError.conflict(
                    "Cannot delete security role because it is currently assigned to active staff members"));
        }

        roleRepository.delete(role);
        return Result.success(null);
    }

    @Override
    public Result<List<Role>, ApplicationError> handle(ProvisionTenantRolesCommand command) {
        Objects.requireNonNull(command, "ProvisionTenantRolesCommand cannot be null");

        List<Role> provisioned = new ArrayList<>();
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());

        for (RoleTemplate template : RoleTemplate.values()) {
            Role role = Role.fromTemplate(command.tenantId(), template, allPermissions);
            provisioned.add(roleRepository.save(role));
        }

        return Result.success(provisioned);
    }

    private Set<Permission> resolvePermissions(Set<PermissionId> permissionIds) {
        Set<Permission> permissions = new HashSet<>();
        List<Permission> allCatalog = permissionRepository.findAll();
        for (Permission perm : allCatalog) {
            if (permissionIds.contains(perm.id())) {
                permissions.add(perm);
            }
        }
        return permissions;
    }
}
