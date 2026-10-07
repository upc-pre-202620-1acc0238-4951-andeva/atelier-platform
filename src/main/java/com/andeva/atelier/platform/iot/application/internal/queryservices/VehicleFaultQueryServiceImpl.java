package com.andeva.atelier.platform.iot.application.internal.queryservices;

import com.andeva.atelier.platform.iot.application.queryservices.VehicleFaultQueryService;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleFaultRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Query Service implementation for retrieving active and historical vehicle DTC faults.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class VehicleFaultQueryServiceImpl implements VehicleFaultQueryService {

    private final VehicleFaultRepository vehicleFaultRepository;

    public VehicleFaultQueryServiceImpl(VehicleFaultRepository vehicleFaultRepository) {
        this.vehicleFaultRepository = Objects.requireNonNull(vehicleFaultRepository, "VehicleFaultRepository cannot be null");
    }

    @Override
    public List<VehicleFault> getActiveFaultsByVehicle(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        return vehicleFaultRepository.findActiveByVehicleId(vehicleId);
    }

    @Override
    public java.util.Optional<VehicleFault> findById(com.andeva.atelier.platform.iot.domain.model.ids.FaultId faultId) {
        Objects.requireNonNull(faultId, "FaultId cannot be null");
        return vehicleFaultRepository.findById(faultId);
    }

    @Override
    public List<VehicleFault> getFaultHistoryByVehicle(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        return vehicleFaultRepository.findAllByVehicleId(vehicleId);
    }
}
