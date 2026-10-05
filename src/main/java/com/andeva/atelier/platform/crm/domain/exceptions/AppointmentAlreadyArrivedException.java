package com.andeva.atelier.platform.crm.domain.exceptions;

import java.util.UUID;

public class AppointmentAlreadyArrivedException extends CrmDomainException {

    public AppointmentAlreadyArrivedException(UUID appointmentId) {
        super("APPOINTMENT_ALREADY_ARRIVED", String.format("Appointment %s has already arrived at the workshop and cannot be canceled or rescheduled", appointmentId));
    }
}
