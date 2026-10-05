package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.commands.RescheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.RescheduleAppointmentResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.UUID;

/**
 * Assembler creating RescheduleAppointmentCommand from REST resource.
 *
 * @author Adiel Sanchez Santin
 */
public final class RescheduleAppointmentCommandFromResourceAssembler {

    private RescheduleAppointmentCommandFromResourceAssembler() {
    }

    public static RescheduleAppointmentCommand toCommandFromResource(
            UUID tenantId,
            UUID appointmentId,
            RescheduleAppointmentResource resource
    ) {
        return new RescheduleAppointmentCommand(
                TenantId.of(tenantId),
                AppointmentId.of(appointmentId),
                resource.newScheduledAt()
        );
    }
}
