package com.andeva.atelier.platform.crm.domain.exceptions;

import java.time.Instant;

public class AppointmentPastDateException extends CrmDomainException {

    public AppointmentPastDateException(Instant scheduledAt) {
        super("APPOINTMENT_PAST_DATE", String.format("Appointment date %s cannot be scheduled in the past", scheduledAt));
    }
}
