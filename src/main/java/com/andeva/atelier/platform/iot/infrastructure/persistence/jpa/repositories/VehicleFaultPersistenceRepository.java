package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.VehicleFaultPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link VehicleFaultPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface VehicleFaultPersistenceRepository extends JpaRepository<VehicleFaultPersistenceEntity, UUID> {

    List<VehicleFaultPersistenceEntity> findAllByVehicleIdAndIsResolvedFalse(UUID vehicleId);

    List<VehicleFaultPersistenceEntity> findAllByVehicleIdOrderByDetectedAtDesc(UUID vehicleId);

    List<VehicleFaultPersistenceEntity> findAllByTenantId(UUID tenantId);
}
