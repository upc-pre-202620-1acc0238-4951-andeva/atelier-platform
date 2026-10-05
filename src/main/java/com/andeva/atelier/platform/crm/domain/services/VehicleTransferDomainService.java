package com.andeva.atelier.platform.crm.domain.services;

import com.andeva.atelier.platform.crm.domain.exceptions.CustomerInactiveException;
import com.andeva.atelier.platform.crm.domain.exceptions.VehicleActiveOwnershipNotFoundException;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain service orchestrating the transfer of legal custody and vehicle ownership between clients.
 *
 * @author Adiel Sanchez Santin
 */
public class VehicleTransferDomainService {

    public Result<VehicleOwnership, DomainException> transferVehicle(
            Vehicle vehicle,
            Customer targetCustomer,
            LocalDate transferDate
    ) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        Objects.requireNonNull(targetCustomer, "Target customer cannot be null");
        Objects.requireNonNull(transferDate, "Transfer date cannot be null");

        if (targetCustomer.status() != CustomerStatus.ACTIVE) {
            return Result.failure(new CustomerInactiveException(targetCustomer.id().value()));
        }

        if (vehicle.getActiveOwnership().isEmpty()) {
            return Result.failure(new VehicleActiveOwnershipNotFoundException(vehicle.id().value()));
        }

        VehicleOwnership newOwnership = vehicle.transferOwnership(targetCustomer.id(), transferDate);
        return Result.success(newOwnership);
    }
}
