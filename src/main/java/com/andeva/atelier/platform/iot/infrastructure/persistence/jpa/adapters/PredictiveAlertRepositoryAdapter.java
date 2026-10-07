package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.domain.repositories.PredictiveAlertRepository;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers.PredictiveAlertPersistenceAssembler;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories.PredictiveAlertPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Secondary adapter implementing {@link PredictiveAlertRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public class PredictiveAlertRepositoryAdapter implements PredictiveAlertRepository {

    private final PredictiveAlertPersistenceRepository persistenceRepository;
    private final PredictiveAlertPersistenceAssembler assembler;

    public PredictiveAlertRepositoryAdapter(
            PredictiveAlertPersistenceRepository persistenceRepository,
            PredictiveAlertPersistenceAssembler assembler) {
        this.persistenceRepository = persistenceRepository;
        this.assembler = assembler;
    }

    @Override
    public PredictiveAlert save(PredictiveAlert alert) {
        Objects.requireNonNull(alert, "alert cannot be null");
        return assembler.toDomain(persistenceRepository.save(assembler.toEntity(alert)));
    }

    @Override
    public Optional<PredictiveAlert> findById(AlertId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public List<PredictiveAlert> findAllByVehicleId(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        return persistenceRepository.findAllByVehicleIdOrderByCreatedAtDesc(vehicleId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public List<PredictiveAlert> findAllByTenantIdAndStatus(TenantId tenantId, AlertStatus status) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        return persistenceRepository.findAllByTenantIdAndStatus(tenantId.value(), status).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public List<PredictiveAlert> findAllByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return persistenceRepository.findAllByTenantId(tenantId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }
}
