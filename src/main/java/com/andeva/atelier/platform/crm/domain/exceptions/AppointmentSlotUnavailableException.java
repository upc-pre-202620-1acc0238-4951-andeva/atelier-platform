package com.andeva.atelier.platform.crm.domain.exceptions;

import java.time.Instant;
import java.util.UUID;

public class AppointmentSlotUnavailableException extends CrmDomainException {

    public AppointmentSlotUnavailableException(UUID branchId, Instant scheduledAt) {
        super("APPOINTMENT_SLOT_UNAVAILABLE", String.format("Reception capacity at branch %s is full for time slot %s", branchId, scheduledAt));
    }
}
