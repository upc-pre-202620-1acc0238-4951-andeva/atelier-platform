package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.PredictiveAlertPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link PredictiveAlertPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface PredictiveAlertPersistenceRepository extends JpaRepository<PredictiveAlertPersistenceEntity, UUID> {

    List<PredictiveAlertPersistenceEntity> findAllByVehicleIdOrderByCreatedAtDesc(UUID vehicleId);

    List<PredictiveAlertPersistenceEntity> findAllByTenantIdAndStatus(UUID tenantId, AlertStatus status);

    List<PredictiveAlertPersistenceEntity> findAllByTenantId(UUID tenantId);
}
