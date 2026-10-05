package com.andeva.atelier.platform.operations.domain.repositories;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository {

    Service save(Service service);

    Optional<Service> findById(ServiceId id);

    List<Service> findByTenantId(TenantId tenantId);

    Optional<Service> findByTenantIdAndName(TenantId tenantId, String name);
}
