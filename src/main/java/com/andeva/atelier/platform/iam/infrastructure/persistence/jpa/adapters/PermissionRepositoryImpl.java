package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.repositories.PermissionRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.PermissionPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.PermissionPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.PermissionPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository adapter implementing {@link PermissionRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionPersistenceRepository persistenceRepository;

    public PermissionRepositoryImpl(PermissionPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Permission save(Permission permission) {
        Objects.requireNonNull(permission, "Permission cannot be null");
        PermissionPersistenceEntity entity = PermissionPersistenceAssembler.toEntity(permission);
        PermissionPersistenceEntity saved = persistenceRepository.save(entity);
        return PermissionPersistenceAssembler.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> findAll() {
        return persistenceRepository.findAll()
                .stream()
                .map(PermissionPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> findByIdIn(Collection<PermissionId> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<UUID> uuids = ids.stream().map(PermissionId::value).toList();
        return persistenceRepository.findByIdIn(uuids)
                .stream()
                .map(PermissionPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return persistenceRepository.findByName(name.trim())
                .map(PermissionPersistenceAssembler::toDomain);
    }
}
