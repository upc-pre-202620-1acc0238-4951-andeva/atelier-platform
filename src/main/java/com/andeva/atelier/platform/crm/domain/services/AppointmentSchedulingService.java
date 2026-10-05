package com.andeva.atelier.platform.crm.domain.services;

import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentPastDateException;
import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentSlotUnavailableException;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain service validating reception operational capacity and time window invariants.
 *
 * @author Adiel Sanchez Santin
 */
public class AppointmentSchedulingService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentSchedulingService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "AppointmentRepository cannot be null");
    }

    public Result<Void, DomainException> validateSlotAvailability(
            TenantId tenantId,
            BranchId branchId,
            Instant scheduledAt,
            int estimatedDurationMinutes,
            int maxConcurrentSlots
    ) {
        Instant now = Instant.now();
        if (scheduledAt.isBefore(now.plus(Duration.ofHours(2)))) {
            return Result.failure(new AppointmentPastDateException(scheduledAt));
        }

        Instant slotEnd = scheduledAt.plus(Duration.ofMinutes(estimatedDurationMinutes));
        List<Appointment> overlapping = appointmentRepository.findActiveByBranchInWindow(
                tenantId,
                branchId,
                scheduledAt.minus(Duration.ofMinutes(estimatedDurationMinutes)),
                slotEnd
        );

        if (overlapping.size() >= maxConcurrentSlots) {
            return Result.failure(new AppointmentSlotUnavailableException(branchId.value(), scheduledAt));
        }

        return Result.success(null);
    }
}
