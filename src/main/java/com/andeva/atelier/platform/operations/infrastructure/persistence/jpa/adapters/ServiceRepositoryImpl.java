package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers.ServicePersistenceAssembler;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.ServicePersistenceEntity;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.repositories.ServicePersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ServiceRepositoryImpl implements ServiceRepository {

    private final ServicePersistenceRepository servicePersistenceRepository;

    public ServiceRepositoryImpl(ServicePersistenceRepository servicePersistenceRepository) {
        this.servicePersistenceRepository = Objects.requireNonNull(servicePersistenceRepository);
    }

    @Override
    public Service save(Service service) {
        ServicePersistenceEntity entity = servicePersistenceRepository.findById(service.getId().value())
                .map(existing -> {
                    ServicePersistenceAssembler.updateEntity(existing, service);
                    return existing;
                })
                .orElseGet(() -> ServicePersistenceAssembler.toEntity(service));

        ServicePersistenceEntity saved = servicePersistenceRepository.save(entity);
        return ServicePersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Service> findById(ServiceId id) {
        return servicePersistenceRepository.findById(id.value())
                .map(ServicePersistenceAssembler::toDomain);
    }

    @Override
    public List<Service> findByTenantId(TenantId tenantId) {
        return servicePersistenceRepository.findAllByTenantId(tenantId.value()).stream()
                .map(ServicePersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public Optional<Service> findByTenantIdAndName(TenantId tenantId, String name) {
        return servicePersistenceRepository.findByTenantIdAndName(tenantId.value(), name)
                .map(ServicePersistenceAssembler::toDomain);
    }
}
