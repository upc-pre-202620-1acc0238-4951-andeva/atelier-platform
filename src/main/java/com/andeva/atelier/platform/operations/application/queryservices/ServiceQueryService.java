package com.andeva.atelier.platform.operations.application.queryservices;

import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.queries.GetServiceByIdQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetServicesByTenantIdQuery;

import java.util.List;
import java.util.Optional;

public interface ServiceQueryService {

    Optional<Service> handle(GetServiceByIdQuery query);

    List<Service> handle(GetServicesByTenantIdQuery query);
}
