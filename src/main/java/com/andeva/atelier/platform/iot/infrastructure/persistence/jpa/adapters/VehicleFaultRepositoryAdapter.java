package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleFaultRepository;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers.VehicleFaultPersistenceAssembler;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories.VehicleFaultPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Secondary adapter implementing {@link VehicleFaultRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public class VehicleFaultRepositoryAdapter implements VehicleFaultRepository {

    private final VehicleFaultPersistenceRepository persistenceRepository;
    private final VehicleFaultPersistenceAssembler assembler;

    public VehicleFaultRepositoryAdapter(
            VehicleFaultPersistenceRepository persistenceRepository,
            VehicleFaultPersistenceAssembler assembler) {
        this.persistenceRepository = persistenceRepository;
        this.assembler = assembler;
    }

    @Override
    public VehicleFault save(VehicleFault fault) {
        Objects.requireNonNull(fault, "fault cannot be null");
        return assembler.toDomain(persistenceRepository.save(assembler.toEntity(fault)));
    }

    @Override
    public Optional<VehicleFault> findById(FaultId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public List<VehicleFault> findActiveByVehicleId(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        return persistenceRepository.findAllByVehicleIdAndIsResolvedFalse(vehicleId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public List<VehicleFault> findAllByVehicleId(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        return persistenceRepository.findAllByVehicleIdOrderByDetectedAtDesc(vehicleId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public List<VehicleFault> findAllByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return persistenceRepository.findAllByTenantId(tenantId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }
}
