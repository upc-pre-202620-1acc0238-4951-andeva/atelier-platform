package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.commands.ScheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.ScheduleAppointmentResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.UUID;

/**
 * Assembler creating ScheduleAppointmentCommand from REST resource.
 *
 * @author Adiel Sanchez Santin
 */
public final class ScheduleAppointmentCommandFromResourceAssembler {

    private ScheduleAppointmentCommandFromResourceAssembler() {
    }

    public static ScheduleAppointmentCommand toCommandFromResource(
            UUID tenantId,
            ScheduleAppointmentResource resource
    ) {
        return new ScheduleAppointmentCommand(
                TenantId.of(tenantId),
                BranchId.of(resource.branchId()),
                CustomerId.of(resource.customerId()),
                VehicleId.of(resource.vehicleId()),
                resource.scheduledAt(),
                resource.estimatedDurationMinutes() > 0 ? resource.estimatedDurationMinutes() : 30,
                resource.reason() != null ? resource.reason().trim() : null
        );
    }
}
