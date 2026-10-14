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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Idempotent startup synchronization of the RBAC permission catalog and the factory roles of existing tenants.
 * <ol>
 *     <li>Inserts every {@link PermissionCatalog} permission missing from the {@code permissions} table.</li>
 *     <li>Grants each newly inserted permission to the system roles whose template includes it, so tenants
 *     registered before the permission existed receive it without overriding their sovereign edits.</li>
 *     <li>Provisions factory roles missing in a tenant and assigns the owner role to the account that
 *     registered the workshop when no member holds it (tenants registered before owners were provisioned).</li>
 * </ol>
 * Running it again on a synchronized database performs no writes.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class PermissionCatalogSeedingService {

    private static final Logger log = LoggerFactory.getLogger(PermissionCatalogSeedingService.class);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final TenantMembershipRepository membershipRepository;

    /**
     * Outcome of a synchronization run.
     */
    public record SeedingResult(int permissionsCreated, int rolesUpdated, int rolesProvisioned, int ownersAssigned) {
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
        Map<String, Permission> catalog = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::name, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        List<Permission> created = new ArrayList<>();
        for (PermissionCatalog.Entry entry : PermissionCatalog.entries()) {
            if (!catalog.containsKey(entry.name())) {
                Permission saved = permissionRepository.save(
                        Permission.create(entry.name(), entry.description(), entry.category()));
                catalog.put(saved.name(), saved);
                created.add(saved);
            }
        }

        int rolesUpdated = 0;
        Map<TenantId, Map<RoleTemplate, Role>> systemRolesByTenant = new LinkedHashMap<>();
        for (Role role : roleRepository.findAllSystemRoles()) {
            Optional<RoleTemplate> template = RoleTemplate.fromCode(role.code());
            if (template.isEmpty()) {
                continue;
            }
            Role current = role;
            Set<Permission> grants = Role.templateSubset(template.get(), created);
            if (!grants.isEmpty()) {
                grants.forEach(current::grantPermission);
                current = roleRepository.save(current);
                rolesUpdated++;
            }
            systemRolesByTenant.computeIfAbsent(role.tenantId(), id -> new LinkedHashMap<>())
                    .put(template.get(), current);
        }

        int rolesProvisioned = 0;
        int ownersAssigned = 0;
        for (Map.Entry<TenantId, Map<RoleTemplate, Role>> tenantRoles : systemRolesByTenant.entrySet()) {
            TenantId tenantId = tenantRoles.getKey();
            Map<RoleTemplate, Role> roles = tenantRoles.getValue();
            for (RoleTemplate template : RoleTemplate.values()) {
                if (!roles.containsKey(template)) {
                    roles.put(template, roleRepository.save(Role.fromTemplate(tenantId, template, catalog.values())));
                    rolesProvisioned++;
                }
            }
            if (assignOwnerIfMissing(tenantId, roles.get(RoleTemplate.ROLE_WORKSHOP_OWNER))) {
                ownersAssigned++;
            }
        }

        SeedingResult result = new SeedingResult(created.size(), rolesUpdated, rolesProvisioned, ownersAssigned);
        if (created.size() + rolesUpdated + rolesProvisioned + ownersAssigned > 0) {
            log.info("RBAC catalog synchronized: {}", result);
        }
        return result;
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
