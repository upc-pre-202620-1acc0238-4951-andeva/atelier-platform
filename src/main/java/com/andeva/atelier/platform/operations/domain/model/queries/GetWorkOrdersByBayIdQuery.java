package com.andeva.atelier.platform.operations.domain.model.queries;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;

public record GetWorkOrdersByBayIdQuery(
        WorkBayId bayId
) {
}
