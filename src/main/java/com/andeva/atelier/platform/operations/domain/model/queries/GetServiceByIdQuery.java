package com.andeva.atelier.platform.operations.domain.model.queries;

import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;

public record GetServiceByIdQuery(
        ServiceId serviceId
) {
}
