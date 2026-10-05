package com.andeva.atelier.platform.crm.domain.repositories;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence port for the Appointment aggregate.
 */
public interface AppointmentRepository {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(AppointmentId id);

    Optional<Appointment> findByIdAndTenantId(AppointmentId id, TenantId tenantId);

    List<Appointment> findByTenantIdAndBranchIdAndDate(TenantId tenantId, BranchId branchId, Instant startOfDay, Instant endOfDay);

    List<Appointment> findByCustomerId(CustomerId customerId);

    List<Appointment> findByVehicleId(VehicleId vehicleId);

    /**
     * Returns PENDING or CONFIRMED appointments of a branch whose start falls inside the given window.
     * Used to evaluate slot overlap in the application layer.
     */
    List<Appointment> findActiveByBranchInWindow(TenantId tenantId, BranchId branchId, Instant windowStart, Instant windowEnd);

    boolean existsActiveAppointmentsByCustomerId(CustomerId customerId);
}
