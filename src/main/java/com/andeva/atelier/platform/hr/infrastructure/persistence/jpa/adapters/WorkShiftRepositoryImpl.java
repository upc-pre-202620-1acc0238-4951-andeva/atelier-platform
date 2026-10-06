package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.repositories.WorkShiftRepository;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.assemblers.WorkShiftPersistenceAssembler;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.WorkShiftPersistenceEntity;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.repositories.WorkShiftPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class WorkShiftRepositoryImpl implements WorkShiftRepository {

    private final WorkShiftPersistenceRepository persistenceRepository;
    private final WorkShiftPersistenceAssembler assembler;
    private final ApplicationEventPublisher eventPublisher;

    public WorkShiftRepositoryImpl(
            WorkShiftPersistenceRepository persistenceRepository,
            WorkShiftPersistenceAssembler assembler,
            ApplicationEventPublisher eventPublisher
    ) {
        this.persistenceRepository = Objects.requireNonNull(persistenceRepository, "persistenceRepository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "assembler cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    @Override
    public Optional<WorkShift> findById(WorkShiftId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<WorkShift> findByTenantIdAndName(TenantId tenantId, String name) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        return persistenceRepository.findByTenantIdAndName(tenantId.value(), name).map(assembler::toDomain);
    }

    @Override
    public List<WorkShift> findAllByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return persistenceRepository.findAllByTenantId(tenantId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public boolean existsByTenantIdAndName(TenantId tenantId, String name) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        return persistenceRepository.existsByTenantIdAndName(tenantId.value(), name);
    }

    @Override
    public WorkShift save(WorkShift workShift) {
        Objects.requireNonNull(workShift, "workShift cannot be null");
        WorkShiftPersistenceEntity entity = persistenceRepository.findById(workShift.getId().value())
                .map(existing -> {
                    assembler.updateEntity(existing, workShift);
                    return existing;
                })
                .orElseGet(() -> assembler.toEntity(workShift));

        WorkShiftPersistenceEntity saved = persistenceRepository.save(entity);
        WorkShift domain = assembler.toDomain(saved);

        workShift.domainEvents().forEach(eventPublisher::publishEvent);
        workShift.clearDomainEvents();

        return domain;
    }
}
