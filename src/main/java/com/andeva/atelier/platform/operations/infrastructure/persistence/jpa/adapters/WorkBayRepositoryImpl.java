package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.repositories.WorkBayRepository;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers.WorkBayPersistenceAssembler;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkBayPersistenceEntity;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.repositories.WorkBayPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class WorkBayRepositoryImpl implements WorkBayRepository {

    private final WorkBayPersistenceRepository workBayPersistenceRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public WorkBayRepositoryImpl(
            WorkBayPersistenceRepository workBayPersistenceRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.workBayPersistenceRepository = Objects.requireNonNull(workBayPersistenceRepository);
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
    }

    @Override
    public WorkBay save(WorkBay workBay) {
        WorkBayPersistenceEntity entity = workBayPersistenceRepository.findById(workBay.getId().value())
                .map(existing -> {
                    WorkBayPersistenceAssembler.updateEntity(existing, workBay);
                    return existing;
                })
                .orElseGet(() -> WorkBayPersistenceAssembler.toEntity(workBay));

        WorkBayPersistenceEntity saved = workBayPersistenceRepository.save(entity);

        workBay.domainEvents().forEach(applicationEventPublisher::publishEvent);
        workBay.clearDomainEvents();

        return WorkBayPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<WorkBay> findById(WorkBayId id) {
        return workBayPersistenceRepository.findById(id.value())
                .map(WorkBayPersistenceAssembler::toDomain);
    }

    @Override
    public List<WorkBay> findByTenantIdAndBranchId(TenantId tenantId, BranchId branchId) {
        return workBayPersistenceRepository.findAllByTenantIdAndBranchId(tenantId.value(), branchId.value()).stream()
                .map(WorkBayPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<WorkBay> findAvailableBays(TenantId tenantId, BranchId branchId, BayType bayType) {
        return workBayPersistenceRepository.findAvailableByBranchIdAndType(tenantId.value(), branchId.value(), bayType).stream()
                .map(WorkBayPersistenceAssembler::toDomain)
                .toList();
    }
}
