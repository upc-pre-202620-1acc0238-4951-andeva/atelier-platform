package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.WorkShiftPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkShiftPersistenceRepository extends JpaRepository<WorkShiftPersistenceEntity, UUID> {
    Optional<WorkShiftPersistenceEntity> findByTenantIdAndName(UUID tenantId, String name);
    List<WorkShiftPersistenceEntity> findAllByTenantId(UUID tenantId);
    boolean existsByTenantIdAndName(UUID tenantId, String name);
}
