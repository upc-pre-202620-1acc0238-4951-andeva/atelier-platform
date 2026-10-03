package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.assemblers.VehiclePersistenceAssembler;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories.VehiclePersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JPA adapter implementing the VehicleRepository domain port.
 *
 * @author Adiel Sanchez Santin
 */
@Repository
public class VehicleRepositoryImpl implements VehicleRepository {

    private final VehiclePersistenceRepository vehiclePersistenceRepository;

    public VehicleRepositoryImpl(VehiclePersistenceRepository vehiclePersistenceRepository) {
        this.vehiclePersistenceRepository = Objects.requireNonNull(vehiclePersistenceRepository);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehiclePersistenceEntity entity = VehiclePersistenceAssembler.toEntity(vehicle);
        VehiclePersistenceEntity saved = vehiclePersistenceRepository.save(entity);
        return VehiclePersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Vehicle> findById(VehicleId id) {
        return vehiclePersistenceRepository.findById(id.value())
                .map(VehiclePersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Vehicle> findByPlate(LicensePlate plate) {
        return vehiclePersistenceRepository.findByPlate(plate.value())
                .map(VehiclePersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Vehicle> findByVin(String vin) {
        return vehiclePersistenceRepository.findByVin(vin)
                .map(VehiclePersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByPlate(LicensePlate plate) {
        return vehiclePersistenceRepository.existsByPlate(plate.value());
    }

    @Override
    public boolean existsByVin(String vin) {
        return vehiclePersistenceRepository.existsByVin(vin);
    }

    @Override
    public List<Vehicle> findByCurrentOwnerId(CustomerId customerId) {
        return vehiclePersistenceRepository.findAllActiveByCustomerId(customerId.value())
                .stream()
                .map(VehiclePersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<Vehicle> findByCurrentUserId(UserId userId) {
        return vehiclePersistenceRepository.findAllActiveByUserId(userId.value())
                .stream()
                .map(VehiclePersistenceAssembler::toDomain)
                .toList();
    }
}
