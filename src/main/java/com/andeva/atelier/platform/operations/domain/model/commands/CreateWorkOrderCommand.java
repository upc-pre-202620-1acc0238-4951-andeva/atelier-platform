package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;

public record CreateWorkOrderCommand(
        TenantId tenantId,
        BranchId branchId,
        AppointmentId appointmentId,
        VehicleId vehicleId,
        CustomerId customerId,
        Mileage mileageIn,
        DiagnosticSummary diagnosticSummary
) {
}
