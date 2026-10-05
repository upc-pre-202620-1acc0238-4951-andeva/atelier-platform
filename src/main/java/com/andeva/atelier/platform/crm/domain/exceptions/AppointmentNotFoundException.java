package com.andeva.atelier.platform.crm.domain.exceptions;

import java.util.UUID;

public class AppointmentNotFoundException extends CrmDomainException {

    public AppointmentNotFoundException(UUID appointmentId) {
        super("APPOINTMENT_NOT_FOUND", String.format("Appointment with identifier %s was not found", appointmentId));
    }
}
