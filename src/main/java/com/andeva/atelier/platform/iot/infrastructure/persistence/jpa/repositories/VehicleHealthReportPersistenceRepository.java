package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.VehicleHealthReportPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for issued AI vehicle health reports.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface VehicleHealthReportPersistenceRepository extends JpaRepository<VehicleHealthReportPersistenceEntity, UUID> {

    Optional<VehicleHealthReportPersistenceEntity> findFirstByTenantIdAndVehicleIdOrderByGeneratedAtDesc(UUID tenantId, UUID vehicleId);

    List<VehicleHealthReportPersistenceEntity> findAllByTenantIdAndVehicleIdInOrderByGeneratedAtDesc(UUID tenantId, Collection<UUID> vehicleIds);

    long countByTenantIdAndGeneratedAtGreaterThanEqual(UUID tenantId, Instant since);
}
