package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.CreateWorkOrderResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.UUID;

public final class CreateWorkOrderCommandFromResourceAssembler {
    private CreateWorkOrderCommandFromResourceAssembler() {}

    public static CreateWorkOrderCommand toCommandFromResource(CreateWorkOrderResource resource, UUID tenantId) {
        return new CreateWorkOrderCommand(
                tenantId != null ? new TenantId(tenantId) : null,
                resource.branchId() != null ? new BranchId(resource.branchId()) : null,
                resource.appointmentId() != null ? new AppointmentId(resource.appointmentId()) : null,
                resource.vehicleId() != null ? new VehicleId(resource.vehicleId()) : null,
                resource.customerId() != null ? new CustomerId(resource.customerId()) : null,
                resource.mileageIn() != null ? Mileage.of(resource.mileageIn()) : null,
                resource.diagnosticSummary() != null ? DiagnosticSummary.of(resource.diagnosticSummary()) : null
        );
    }
}
