package com.andeva.atelier.platform.crm.application.queryservices;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByPlateQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleOwnershipHistoryQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehiclesByCustomerIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehiclesByUserIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Public application port for vehicle and ownership read queries.
 *
 * @author Adiel Sanchez Santin
 */
public interface VehicleQueryService {

    Optional<Vehicle> handle(GetVehicleByIdQuery query);

    Optional<Vehicle> handle(GetVehicleByPlateQuery query);

    Optional<Vehicle> handle(com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByVinQuery query);

    List<Vehicle> handle(GetVehiclesByCustomerIdQuery query);

    List<VehicleOwnership> handle(GetVehicleOwnershipHistoryQuery query);

    List<Vehicle> handle(GetVehiclesByUserIdQuery query);
}
