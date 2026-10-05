package com.andeva.atelier.platform.crm.domain.repositories;

import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.ids.VehicleOwnershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence port for vehicle ownership lifecycle records.
 *
 * @author Adiel Sanchez Santin
 */
public interface VehicleOwnershipRepository {

    VehicleOwnership save(VehicleOwnership ownership);

    Optional<VehicleOwnership> findById(VehicleOwnershipId id);

    List<VehicleOwnership> findByVehicleId(VehicleId vehicleId);

    Optional<VehicleOwnership> findActiveOwnershipByVehicleId(VehicleId vehicleId);

    List<VehicleOwnership> findByCustomerId(CustomerId customerId);

    List<VehicleOwnership> findByUserIdAndEndDateIsNull(UserId userId);
}
