package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.RolePersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.PermissionPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.PermissionPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.RolePersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.TenantPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repository adapter implementing {@link RoleRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional
public class RoleRepositoryImpl implements RoleRepository {

    private final RolePersistenceRepository rolePersistenceRepository;
    private final TenantPersistenceRepository tenantPersistenceRepository;
    private final PermissionPersistenceRepository permissionPersistenceRepository;

    public RoleRepositoryImpl(
            RolePersistenceRepository rolePersistenceRepository,
            TenantPersistenceRepository tenantPersistenceRepository,
            PermissionPersistenceRepository permissionPersistenceRepository) {
        this.rolePersistenceRepository = rolePersistenceRepository;
        this.tenantPersistenceRepository = tenantPersistenceRepository;
        this.permissionPersistenceRepository = permissionPersistenceRepository;
    }

    @Override
    public Role save(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        TenantPersistenceEntity tenant = tenantPersistenceRepository.getReferenceById(role.tenantId().value());

        List<UUID> permUuids = role.permissions() != null
                ? role.permissions().stream().map(p -> p.id().value()).toList()
                : Collections.emptyList();
        Set<PermissionPersistenceEntity> permissions = permUuids.isEmpty()
                ? new HashSet<>()
                : new HashSet<>(permissionPersistenceRepository.findByIdIn(permUuids));

        RolePersistenceEntity entity = RolePersistenceAssembler.toEntity(role, tenant, permissions);
        RolePersistenceEntity saved = rolePersistenceRepository.save(entity);
        return RolePersistenceAssembler.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(RoleId id) {
        if (id == null) {
            return Optional.empty();
        }
        return rolePersistenceRepository.findById(id.value())
                .map(RolePersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByTenantIdAndName(TenantId tenantId, String name) {
        if (tenantId == null || name == null || name.isBlank()) {
            return Optional.empty();
        }
        return rolePersistenceRepository.findByTenant_IdAndName(tenantId.value(), name.trim())
                .map(RolePersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByTenantIdAndCode(TenantId tenantId, String code) {
        if (tenantId == null || code == null || code.isBlank()) {
            return Optional.empty();
        }
        return rolePersistenceRepository.findByTenant_IdAndCode(tenantId.value(), code.trim())
                .map(RolePersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findByTenantId(TenantId tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }
        return rolePersistenceRepository.findByTenant_Id(tenantId.value())
                .stream()
                .map(RolePersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTenantIdAndName(TenantId tenantId, String name) {
        if (tenantId == null || name == null || name.isBlank()) {
            return false;
        }
        return rolePersistenceRepository.existsByTenant_IdAndName(tenantId.value(), name.trim());
    }

    @Override
    public void delete(Role role) {
        if (role != null && role.id() != null) {
            rolePersistenceRepository.deleteById(role.id().value());
        }
    }
}
