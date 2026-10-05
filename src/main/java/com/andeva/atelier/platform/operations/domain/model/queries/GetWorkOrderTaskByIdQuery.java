package com.andeva.atelier.platform.operations.domain.model.queries;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

public record GetWorkOrderTaskByIdQuery(
        WorkOrderTaskId taskId
) {
}
