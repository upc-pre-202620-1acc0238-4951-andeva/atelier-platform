package com.andeva.atelier.platform.crm.domain.model.aggregates;

import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentAlreadyArrivedException;
import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentInvalidStateTransitionException;
import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentPastDateException;
import com.andeva.atelier.platform.crm.domain.model.enums.AppointmentStatus;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentArrivedEvent;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentCanceledEvent;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentConfirmedEvent;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentRescheduledEvent;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentScheduledEvent;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root governing workshop appointment scheduling, confirmation, and reception arrival.
 *
 * @author Adiel Sanchez Santin
 */
public class Appointment extends AbstractDomainAggregateRoot<Appointment> {

    private final AppointmentId id;
    private final TenantId tenantId;
    private final BranchId branchId;
    private final CustomerId customerId;
    private final VehicleId vehicleId;
    private Instant scheduledAt;
    private int estimatedDurationMinutes;
    private String reason;
    private AppointmentStatus status;
    private String cancellationReason;

    public Appointment(
            AppointmentId id,
            TenantId tenantId,
            BranchId branchId,
            CustomerId customerId,
            VehicleId vehicleId,
            Instant scheduledAt,
            int estimatedDurationMinutes,
            String reason,
            AppointmentStatus status,
            String cancellationReason
    ) {
        this.id = Objects.requireNonNull(id, "AppointmentId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "BranchId cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "CustomerId cannot be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        this.scheduledAt = Objects.requireNonNull(scheduledAt, "ScheduledAt cannot be null");
        this.status = Objects.requireNonNull(status, "AppointmentStatus cannot be null");
        this.estimatedDurationMinutes = estimatedDurationMinutes > 0 ? estimatedDurationMinutes : 30;
        this.reason = reason;
        this.cancellationReason = cancellationReason;
    }

    public static Appointment schedule(
            AppointmentId id,
            TenantId tenantId,
            BranchId branchId,
            CustomerId customerId,
            VehicleId vehicleId,
            Instant scheduledAt,
            int estimatedDurationMinutes,
            String reason
    ) {
        if (scheduledAt.isBefore(Instant.now())) {
            throw new AppointmentPastDateException(scheduledAt);
        }

        Appointment appointment = new Appointment(
                id,
                tenantId,
                branchId,
                customerId,
                vehicleId,
                scheduledAt,
                estimatedDurationMinutes,
                reason,
                AppointmentStatus.PENDING,
                null
        );

        appointment.registerEvent(AppointmentScheduledEvent.of(id, tenantId, branchId, customerId, vehicleId, scheduledAt));
        return appointment;
    }

    public void confirm() {
        if (this.status != AppointmentStatus.PENDING) {
            throw new AppointmentInvalidStateTransitionException(this.status.name(), AppointmentStatus.CONFIRMED.name());
        }
        this.status = AppointmentStatus.CONFIRMED;
        registerEvent(AppointmentConfirmedEvent.of(this.id, this.customerId, this.scheduledAt));
    }

    public void markArrived() {
        if (this.status == AppointmentStatus.ARRIVED) {
            throw new AppointmentAlreadyArrivedException(this.id.value());
        }
        if (this.status == AppointmentStatus.CANCELED) {
            throw new AppointmentInvalidStateTransitionException(this.status.name(), AppointmentStatus.ARRIVED.name());
        }
        this.status = AppointmentStatus.ARRIVED;
        registerEvent(AppointmentArrivedEvent.of(this.id, this.tenantId, this.customerId, this.vehicleId));
    }

    public void cancel(String reason) {
        if (this.status == AppointmentStatus.ARRIVED) {
            throw new AppointmentAlreadyArrivedException(this.id.value());
        }
        if (this.status == AppointmentStatus.CANCELED) {
            return;
        }
        Objects.requireNonNull(reason, "Cancellation reason cannot be null");
        this.status = AppointmentStatus.CANCELED;
        this.cancellationReason = reason.trim();
        registerEvent(AppointmentCanceledEvent.of(this.id, this.cancellationReason));
    }

    public void reschedule(Instant newScheduledAt) {
        Objects.requireNonNull(newScheduledAt, "New scheduled time cannot be null");
        if (this.status == AppointmentStatus.ARRIVED) {
            throw new AppointmentAlreadyArrivedException(this.id.value());
        }
        if (this.status == AppointmentStatus.CANCELED) {
            throw new AppointmentInvalidStateTransitionException(this.status.name(), "RESCHEDULED");
        }
        if (newScheduledAt.isBefore(Instant.now())) {
            throw new AppointmentPastDateException(newScheduledAt);
        }
        this.scheduledAt = newScheduledAt;
        registerEvent(AppointmentRescheduledEvent.of(this.id, newScheduledAt));
    }

    public AppointmentId id() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public BranchId branchId() {
        return branchId;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public VehicleId vehicleId() {
        return vehicleId;
    }

    public Instant scheduledAt() {
        return scheduledAt;
    }

    public int estimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public String reason() {
        return reason;
    }

    public AppointmentStatus status() {
        return status;
    }

    public String cancellationReason() {
        return cancellationReason;
    }
}
