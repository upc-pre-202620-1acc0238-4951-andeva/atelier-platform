package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

public record ReleaseWorkBayCommand(
        WorkOrderId workOrderId
) {
}
