package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;

public record UpdateWorkOrderCommand(
        WorkOrderId workOrderId,
        DiagnosticSummary diagnosticSummary,
        Mileage mileageIn
) {}
