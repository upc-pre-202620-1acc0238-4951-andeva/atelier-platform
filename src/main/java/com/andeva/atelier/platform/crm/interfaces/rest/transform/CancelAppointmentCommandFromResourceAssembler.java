package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.commands.CancelAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CancelAppointmentResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.UUID;

/**
 * Assembler creating CancelAppointmentCommand from REST resource.
 *
 * @author Adiel Sanchez Santin
 */
public final class CancelAppointmentCommandFromResourceAssembler {

    private CancelAppointmentCommandFromResourceAssembler() {
    }

    public static CancelAppointmentCommand toCommandFromResource(
            UUID tenantId,
            UUID appointmentId,
            CancelAppointmentResource resource
    ) {
        return new CancelAppointmentCommand(
                TenantId.of(tenantId),
                AppointmentId.of(appointmentId),
                resource.cancellationReason().trim()
        );
    }
}
