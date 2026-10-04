package com.andeva.atelier.platform.crm.application.internal.queryservices;

import com.andeva.atelier.platform.crm.application.queryservices.VehicleQueryService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByPlateQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleOwnershipHistoryQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehiclesByCustomerIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehiclesByUserIdQuery;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleOwnershipRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of VehicleQueryService executing read-only projections.
 *
 * @author Adiel Sanchez Santin
 */
@Service
@Transactional(readOnly = true)
public class VehicleQueryServiceImpl implements VehicleQueryService {

    private final VehicleRepository vehicleRepository;
    private final VehicleOwnershipRepository vehicleOwnershipRepository;

    public VehicleQueryServiceImpl(
            VehicleRepository vehicleRepository,
            VehicleOwnershipRepository vehicleOwnershipRepository
    ) {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository cannot be null");
        this.vehicleOwnershipRepository = Objects.requireNonNull(vehicleOwnershipRepository, "VehicleOwnershipRepository cannot be null");
    }

    @Override
    public Optional<Vehicle> handle(GetVehicleByIdQuery query) {
        Objects.requireNonNull(query, "GetVehicleByIdQuery cannot be null");
        return vehicleRepository.findById(query.vehicleId());
    }

    @Override
    public Optional<Vehicle> handle(GetVehicleByPlateQuery query) {
        Objects.requireNonNull(query, "GetVehicleByPlateQuery cannot be null");
        return vehicleRepository.findByPlate(query.plate());
    }

    @Override
    public Optional<Vehicle> handle(com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByVinQuery query) {
        Objects.requireNonNull(query, "GetVehicleByVinQuery cannot be null");
        return vehicleRepository.findByVin(query.vin().value());
    }

    @Override
    public List<Vehicle> handle(GetVehiclesByCustomerIdQuery query) {
        Objects.requireNonNull(query, "GetVehiclesByCustomerIdQuery cannot be null");
        return vehicleRepository.findByCurrentOwnerId(query.customerId());
    }

    @Override
    public List<VehicleOwnership> handle(GetVehicleOwnershipHistoryQuery query) {
        Objects.requireNonNull(query, "GetVehicleOwnershipHistoryQuery cannot be null");
        return vehicleOwnershipRepository.findByVehicleId(query.vehicleId());
    }

    @Override
    public List<Vehicle> handle(GetVehiclesByUserIdQuery query) {
        Objects.requireNonNull(query, "GetVehiclesByUserIdQuery cannot be null");
        return vehicleRepository.findByCurrentUserId(query.userId());
    }
}
