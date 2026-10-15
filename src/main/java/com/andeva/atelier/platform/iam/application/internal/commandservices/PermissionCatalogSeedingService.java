package com.andeva.atelier.platform.iam.application.internal.commandservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PermissionCatalog;
import com.andeva.atelier.platform.iam.domain.repositories.PermissionRepository;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Idempotent startup synchronization of the RBAC permission catalog and the factory roles of existing tenants.
 * <ol>
 *     <li>Inserts every {@link PermissionCatalog} permission missing from the {@code permissions} table and deletes
 *     permissions that are no longer part of the catalog (revoking them from every role first).</li>
 *     <li>Rebinds system roles provisioned under legacy template codes or default names to their current
 *     template, keeping role ids and therefore memberships.</li>
 *     <li>Grants each newly inserted permission to the system roles whose template includes it, so tenants
 *     registered before the permission existed receive it without overriding their sovereign edits.</li>
 *     <li>Provisions factory roles missing in a tenant and assigns the owner role to the account that
 *     registered the workshop when no member holds it.</li>
 * </ol>
 * Running it again on a synchronized database performs no writes.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class PermissionCatalogSeedingService {

    private static final Logger log = LoggerFactory.getLogger(PermissionCatalogSeedingService.class);

    /**
     * Template codes and default names used before the eight canonical roles of atelier-roles.md.
     */
    private static final Map<String, RoleTemplate> LEGACY_CODES = Map.of(
            "ROLE_WORKSHOP_ADMIN", RoleTemplate.ROLE_WORKSHOP_ADMINISTRATOR);
    private static final Map<String, RoleTemplate> LEGACY_NAMES = Map.of(
            "Workshop Owner", RoleTemplate.ROLE_WORKSHOP_OWNER,
            "Workshop Administrator", RoleTemplate.ROLE_WORKSHOP_ADMINISTRATOR,
            "Service Advisor", RoleTemplate.ROLE_SERVICE_ADVISOR,
            "Mechanic", RoleTemplate.ROLE_MECHANIC,
            "Inventory Manager", RoleTemplate.ROLE_INVENTORY_MANAGER);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final TenantMembershipRepository membershipRepository;

    /**
     * Outcome of a synchronization run.
     */
    public record SeedingResult(
            int permissionsCreated,
            int permissionsRemoved,
            int rolesUpdated,
            int rolesProvisioned,
            int ownersAssigned
    ) {
        public boolean hasChanges() {
            return permissionsCreated + permissionsRemoved + rolesUpdated + rolesProvisioned + ownersAssigned > 0;
        }
    }

    public PermissionCatalogSeedingService(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            TenantMembershipRepository membershipRepository
    ) {
        this.permissionRepository = Objects.requireNonNull(permissionRepository, "PermissionRepository cannot be null");
        this.roleRepository = Objects.requireNonNull(roleRepository, "RoleRepository cannot be null");
        this.membershipRepository = Objects.requireNonNull(membershipRepository, "TenantMembershipRepository cannot be null");
    }

    public SeedingResult synchronize() {
        Map<String, Permission> stored = new LinkedHashMap<>();
        permissionRepository.findAll().forEach(permission -> stored.putIfAbsent(permission.name(), permission));

        List<Permission> obsolete = stored.values().stream()
                .filter(permission -> !PermissionCatalog.contains(permission.name()))
                .toList();
        List<Permission> created = new ArrayList<>();
        List<Permission> catalog = new ArrayList<>();
        for (PermissionCatalog.Entry entry : PermissionCatalog.entries()) {
            Permission permission = stored.get(entry.name());
            if (permission == null) {
                permission = permissionRepository.save(
                        Permission.create(entry.name(), entry.description(), entry.category()));
                created.add(permission);
            }
            catalog.add(permission);
        }

        int rolesUpdated = 0;
        Map<TenantId, Map<RoleTemplate, Role>> systemRolesByTenant = new LinkedHashMap<>();
        for (Role role : roleRepository.findAll()) {
            boolean changed = revokeObsolete(role, obsolete);
            Optional<RoleTemplate> template = Optional.empty();
            if (role.isSystemRole()) {
                template = resolveTemplate(role);
                if (template.isPresent() && hasLegacyIdentity(role, template.get())) {
                    role.adoptTemplateIdentity(template.get());
                    changed = true;
                }
                if (template.isPresent()) {
                    Set<Permission> grants = Role.templateSubset(template.get(), created);
                    grants.forEach(role::grantPermission);
                    changed |= !grants.isEmpty();
                }
            }
            Role current = changed ? roleRepository.save(role) : role;
            if (changed) {
                rolesUpdated++;
            }
            template.ifPresent(t -> systemRolesByTenant
                    .computeIfAbsent(current.tenantId(), id -> new LinkedHashMap<>())
                    .putIfAbsent(t, current));
        }
        obsolete.forEach(permissionRepository::delete);

        int rolesProvisioned = 0;
        int ownersAssigned = 0;
        for (Map.Entry<TenantId, Map<RoleTemplate, Role>> tenantRoles : systemRolesByTenant.entrySet()) {
            TenantId tenantId = tenantRoles.getKey();
            Map<RoleTemplate, Role> roles = tenantRoles.getValue();
            for (RoleTemplate template : RoleTemplate.values()) {
                if (!roles.containsKey(template)) {
                    roles.put(template, roleRepository.save(Role.fromTemplate(tenantId, template, catalog)));
                    rolesProvisioned++;
                }
            }
            if (assignOwnerIfMissing(tenantId, roles.get(RoleTemplate.ROLE_WORKSHOP_OWNER))) {
                ownersAssigned++;
            }
        }

        SeedingResult result = new SeedingResult(
                created.size(), obsolete.size(), rolesUpdated, rolesProvisioned, ownersAssigned);
        if (result.hasChanges()) {
            log.info("RBAC catalog synchronized: {}", result);
        }
        return result;
    }

    private static Optional<RoleTemplate> resolveTemplate(Role role) {
        Optional<RoleTemplate> template = RoleTemplate.fromCode(role.code());
        if (template.isPresent() || role.code() == null) {
            return template;
        }
        return Optional.ofNullable(LEGACY_CODES.get(role.code()));
    }

    private static boolean hasLegacyIdentity(Role role, RoleTemplate template) {
        return !template.code().equals(role.code()) || LEGACY_NAMES.get(role.name()) == template;
    }

    private static boolean revokeObsolete(Role role, List<Permission> obsolete) {
        Set<String> obsoleteNames = obsolete.stream().map(Permission::name).collect(Collectors.toSet());
        List<Permission> toRevoke = role.permissions().stream()
                .filter(permission -> obsoleteNames.contains(permission.name()))
                .toList();
        toRevoke.forEach(permission -> role.revokePermission(permission.id()));
        return !toRevoke.isEmpty();
    }

    private boolean assignOwnerIfMissing(TenantId tenantId, Role ownerRole) {
        String ownerCode = RoleTemplate.ROLE_WORKSHOP_OWNER.code();
        boolean hasOwner = membershipRepository.findByTenantId(tenantId).stream()
                .anyMatch(m -> m.assignedRoles().stream().anyMatch(r -> ownerCode.equals(r.code())));
        if (hasOwner) {
            return false;
        }
        Optional<TenantMembership> registrant = membershipRepository.findEarliestByTenantId(tenantId);
        if (registrant.isEmpty()) {
            return false;
        }
        TenantMembership membership = registrant.get();
        membership.assignRole(ownerRole);
        membershipRepository.save(membership);
        return true;
    }
}
