package com.andeva.atelier.platform.operations.application.internal.queryservices;

import com.andeva.atelier.platform.operations.application.queryservices.ServiceQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.queries.GetServiceByIdQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetServicesByTenantIdQuery;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class ServiceQueryServiceImpl implements ServiceQueryService {

    private final ServiceRepository serviceRepository;

    public ServiceQueryServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = Objects.requireNonNull(serviceRepository);
    }

    @Override
    public Optional<Service> handle(GetServiceByIdQuery query) {
        if (query == null || query.serviceId() == null) return Optional.empty();
        return serviceRepository.findById(query.serviceId());
    }

    @Override
    public List<Service> handle(GetServicesByTenantIdQuery query) {
        if (query == null || query.tenantId() == null) return List.of();
        return serviceRepository.findByTenantId(query.tenantId());
    }
}
