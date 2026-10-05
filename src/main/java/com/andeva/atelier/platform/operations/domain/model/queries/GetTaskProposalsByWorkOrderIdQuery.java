package com.andeva.atelier.platform.operations.domain.model.queries;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

public record GetTaskProposalsByWorkOrderIdQuery(
        WorkOrderId workOrderId
) {
}
