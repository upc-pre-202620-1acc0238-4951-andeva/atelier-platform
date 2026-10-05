package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.AppointmentResource;

/**
 * Assembler transforming Appointment domain aggregates into AppointmentResource REST responses.
 *
 * @author Adiel Sanchez Santin
 */
public final class AppointmentResourceFromAggregateAssembler {

    private AppointmentResourceFromAggregateAssembler() {
    }

    public static AppointmentResource toResourceFromEntity(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        return new AppointmentResource(
                appointment.id().value(),
                appointment.tenantId().value(),
                appointment.branchId().value(),
                appointment.customerId().value(),
                appointment.vehicleId().value(),
                appointment.scheduledAt(),
                appointment.estimatedDurationMinutes(),
                appointment.reason(),
                appointment.status().name(),
                appointment.cancellationReason()
        );
    }
}
