package com.andeva.atelier.platform.crm.domain.repositories;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence port for the universal Vehicle aggregate.
 * Ownership history is persisted through the aggregate.
 */
public interface VehicleRepository {

    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(VehicleId id);

    Optional<Vehicle> findByPlate(LicensePlate plate);

    Optional<Vehicle> findByVin(String vin);

    boolean existsByPlate(LicensePlate plate);

    boolean existsByVin(String vin);

    List<Vehicle> findByCurrentOwnerId(CustomerId customerId);

    List<Vehicle> findByCurrentUserId(UserId userId);
}
