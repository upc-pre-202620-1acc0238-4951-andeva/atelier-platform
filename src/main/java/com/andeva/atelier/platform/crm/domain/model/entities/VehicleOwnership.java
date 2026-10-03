package com.andeva.atelier.platform.crm.domain.model.entities;

import com.andeva.atelier.platform.crm.domain.model.ids.VehicleOwnershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing a period of legal custody or vehicle ownership.
 *
 * @author Adiel Sanchez Santin
 */
@Getter
public class VehicleOwnership {

    private final VehicleOwnershipId id;
    private final VehicleId vehicleId;
    private CustomerId customerId;
    private final UserId userId;
    private final LocalDate startDate;
    private LocalDate endDate;

    public VehicleOwnership(
            VehicleOwnershipId id,
            VehicleId vehicleId,
            CustomerId customerId,
            UserId userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.id = Objects.requireNonNull(id, "VehicleOwnershipId cannot be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        this.startDate = Objects.requireNonNull(startDate, "StartDate cannot be null");

        if (customerId == null && userId == null) {
            throw new IllegalArgumentException("VehicleOwnership must have either a customerId or a userId");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("EndDate cannot be prior to StartDate");
        }

        this.customerId = customerId;
        this.userId = userId;
        this.endDate = endDate;
    }

    public static VehicleOwnership create(
            VehicleOwnershipId id,
            VehicleId vehicleId,
            CustomerId customerId,
            UserId userId,
            LocalDate startDate
    ) {
        return new VehicleOwnership(id, vehicleId, customerId, userId, startDate, null);
    }

    public boolean isCurrent() {
        return this.endDate == null;
    }

    public void terminate(LocalDate terminationDate) {
        Objects.requireNonNull(terminationDate, "Termination date cannot be null");
        if (terminationDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException("Termination date cannot be prior to start date");
        }
        this.endDate = terminationDate;
    }

    public void linkCustomer(CustomerId newCustomerId) {
        Objects.requireNonNull(newCustomerId, "New customer ID cannot be null");
        if (this.customerId != null) {
            throw new IllegalStateException("Vehicle ownership is already linked to a customer");
        }
        this.customerId = newCustomerId;
    }

    public boolean isOwnedByCustomer(CustomerId customerId) {
        return this.customerId != null && this.customerId.equals(customerId);
    }

    public boolean isOwnedByUser(UserId userId) {
        return this.userId != null && this.userId.equals(userId);
    }
}
