package com.andeva.atelier.platform.hr.domain.model.aggregates;

import com.andeva.atelier.platform.hr.domain.exceptions.ShiftConflictException;
import com.andeva.atelier.platform.hr.domain.model.events.WorkShiftCreatedEvent;
import com.andeva.atelier.platform.hr.domain.model.events.WorkShiftUpdatedEvent;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GracePeriod;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.ShiftSchedule;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalTime;
import java.util.Objects;

public class WorkShift extends AbstractDomainAggregateRoot<WorkShift> {

    private final WorkShiftId id;
    private final TenantId tenantId;
    private String name;
    private ShiftSchedule schedule;
    private GracePeriod gracePeriod;
    private boolean isActive;

    public WorkShift(
            WorkShiftId id,
            TenantId tenantId,
            String name,
            ShiftSchedule schedule,
            GracePeriod gracePeriod,
            boolean isActive
    ) {
        this.id = Objects.requireNonNull(id, "WorkShiftId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.name = validateName(name);
        this.schedule = Objects.requireNonNull(schedule, "ShiftSchedule cannot be null");
        this.gracePeriod = Objects.requireNonNull(gracePeriod, "GracePeriod cannot be null");
        this.isActive = isActive;
    }

    public static WorkShift create(
            TenantId tenantId,
            String name,
            LocalTime startTime,
            LocalTime endTime,
            int gracePeriodMinutes
    ) {
        WorkShiftId shiftId = WorkShiftId.generate();
        ShiftSchedule schedule = ShiftSchedule.of(startTime, endTime);
        GracePeriod grace = GracePeriod.of(gracePeriodMinutes);
        WorkShift workShift = new WorkShift(shiftId, tenantId, name, schedule, grace, true);
        workShift.registerEvent(WorkShiftCreatedEvent.now(shiftId, tenantId, name));
        return workShift;
    }

    public void updateSchedule(String name, LocalTime startTime, LocalTime endTime, int gracePeriodMinutes) {
        this.name = validateName(name);
        this.schedule = ShiftSchedule.of(startTime, endTime);
        this.gracePeriod = GracePeriod.of(gracePeriodMinutes);
        registerEvent(WorkShiftUpdatedEvent.now(this.id, this.tenantId, this.name));
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isLate(LocalTime clockInTime) {
        if (clockInTime == null) return false;
        LocalTime maxAllowed = this.schedule.startTime().plusMinutes(this.gracePeriod.minutes());
        return clockInTime.isAfter(maxAllowed);
    }

    public boolean isWithinWorkingHours(LocalTime time) {
        return this.schedule.isWithinWindow(time);
    }

    private String validateName(String shiftName) {
        if (shiftName == null || shiftName.isBlank()) {
            throw new ShiftConflictException("Shift name cannot be empty or blank");
        }
        if (shiftName.length() > 100) {
            throw new ShiftConflictException("Shift name exceeds maximum allowed length of 100 characters");
        }
        return shiftName.trim();
    }

    public WorkShiftId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public ShiftSchedule getSchedule() {
        return schedule;
    }

    public GracePeriod getGracePeriod() {
        return gracePeriod;
    }

    public boolean isActive() {
        return isActive;
    }
}
