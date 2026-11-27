package com.andeva.atelier.platform.crm.domain.exceptions;

import java.time.Duration;
import java.time.Instant;

public class AppointmentLeadTimeException extends CrmDomainException {

    public AppointmentLeadTimeException(Instant scheduledAt, Duration minimumLeadTime) {
        super("APPOINTMENT_LEAD_TIME", String.format(
                "Appointment date %s must be at least %d hours from now", scheduledAt, minimumLeadTime.toHours()));
    }
}
