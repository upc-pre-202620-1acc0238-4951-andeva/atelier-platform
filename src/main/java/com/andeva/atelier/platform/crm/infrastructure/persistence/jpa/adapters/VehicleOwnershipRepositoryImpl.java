package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.ids.VehicleOwnershipId;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleOwnershipRepository;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.assemblers.VehicleOwnershipPersistenceAssembler;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.VehicleOwnershipPersistenceEntity;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories.VehicleOwnershipPersistenceRepository;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories.VehiclePersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JPA adapter implementing the VehicleOwnershipRepository domain port.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public class VehicleOwnershipRepositoryImpl implements VehicleOwnershipRepository {

    private final VehicleOwnershipPersistenceRepository ownershipPersistenceRepository;
    private final VehiclePersistenceRepository vehiclePersistenceRepository;

    public VehicleOwnershipRepositoryImpl(
            VehicleOwnershipPersistenceRepository ownershipPersistenceRepository,
            VehiclePersistenceRepository vehiclePersistenceRepository
    ) {
        this.ownershipPersistenceRepository = Objects.requireNonNull(ownershipPersistenceRepository);
        this.vehiclePersistenceRepository = Objects.requireNonNull(vehiclePersistenceRepository);
    }

    @Override
    public VehicleOwnership save(VehicleOwnership ownership) {
        VehiclePersistenceEntity vehicleEntity = null;
        if (ownership.getVehicleId() != null) {
            vehicleEntity = vehiclePersistenceRepository.findById(ownership.getVehicleId().value()).orElse(null);
        }
        VehicleOwnershipPersistenceEntity entity = VehicleOwnershipPersistenceAssembler.toEntity(ownership, vehicleEntity);
        VehicleOwnershipPersistenceEntity saved = ownershipPersistenceRepository.save(entity);
        return VehicleOwnershipPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<VehicleOwnership> findById(VehicleOwnershipId id) {
        return ownershipPersistenceRepository.findById(id.value())
                .map(VehicleOwnershipPersistenceAssembler::toDomain);
    }

    @Override
    public List<VehicleOwnership> findByVehicleId(VehicleId vehicleId) {
        return ownershipPersistenceRepository.findAllByVehicleIdOrderByStartDateDesc(vehicleId.value())
                .stream()
                .map(VehicleOwnershipPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public Optional<VehicleOwnership> findActiveOwnershipByVehicleId(VehicleId vehicleId) {
        return ownershipPersistenceRepository.findActiveByVehicleId(vehicleId.value())
                .map(VehicleOwnershipPersistenceAssembler::toDomain);
    }

    @Override
    public List<VehicleOwnership> findByCustomerId(CustomerId customerId) {
        return ownershipPersistenceRepository.findAllByCustomerIdAndEndDateIsNull(customerId.value())
                .stream()
                .map(VehicleOwnershipPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<VehicleOwnership> findByUserIdAndEndDateIsNull(UserId userId) {
        return ownershipPersistenceRepository.findAllByUserIdAndEndDateIsNull(userId.value())
                .stream()
                .map(VehicleOwnershipPersistenceAssembler::toDomain)
                .toList();
    }
}
