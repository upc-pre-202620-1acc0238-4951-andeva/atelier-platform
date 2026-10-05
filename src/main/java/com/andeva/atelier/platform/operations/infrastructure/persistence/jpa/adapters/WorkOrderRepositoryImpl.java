package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers.WorkOrderPersistenceAssembler;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.repositories.WorkOrderPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class WorkOrderRepositoryImpl implements WorkOrderRepository {

    private final WorkOrderPersistenceRepository workOrderPersistenceRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public WorkOrderRepositoryImpl(
            WorkOrderPersistenceRepository workOrderPersistenceRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.workOrderPersistenceRepository = Objects.requireNonNull(workOrderPersistenceRepository);
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
    }

    @Override
    public WorkOrder save(WorkOrder workOrder) {
        WorkOrderPersistenceEntity entity = workOrderPersistenceRepository.findById(workOrder.getId().value())
                .map(existing -> {
                    WorkOrderPersistenceAssembler.updateEntity(existing, workOrder);
                    return existing;
                })
                .orElseGet(() -> WorkOrderPersistenceAssembler.toEntity(workOrder));

        WorkOrderPersistenceEntity saved = workOrderPersistenceRepository.saveAndFlush(entity);

        workOrder.domainEvents().forEach(applicationEventPublisher::publishEvent);
        workOrder.clearDomainEvents();

        return WorkOrderPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<WorkOrder> findById(WorkOrderId id) {
        return workOrderPersistenceRepository.findById(id.value())
                .map(WorkOrderPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<WorkOrder> findByOrderNumber(WorkOrderNumber orderNumber) {
        return workOrderPersistenceRepository.findAll().stream()
                .filter(w -> w.getInternalNumber().equals(orderNumber.sequence()))
                .findFirst()
                .map(WorkOrderPersistenceAssembler::toDomain);
    }

    @Override
    public List<WorkOrder> findByTenantId(TenantId tenantId) {
        return workOrderPersistenceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId.value()).stream()
                .map(WorkOrderPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<WorkOrder> findByBranchId(TenantId tenantId, BranchId branchId) {
        return workOrderPersistenceRepository.findByTenantIdAndBranchId(tenantId.value(), branchId.value()).stream()
                .map(WorkOrderPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<WorkOrder> findByVehicleId(VehicleId vehicleId) {
        return workOrderPersistenceRepository.findAll().stream()
                .filter(w -> w.getVehicleId().equals(vehicleId.value()))
                .map(WorkOrderPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<WorkOrder> findByCustomerId(CustomerId customerId) {
        return workOrderPersistenceRepository.findByCustomerId(customerId.value()).stream()
                .map(WorkOrderPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public Optional<WorkOrder> findByCurrentBayId(WorkBayId bayId) {
        return workOrderPersistenceRepository.findByCurrentBayId(bayId.value())
                .map(WorkOrderPersistenceAssembler::toDomain);
    }

    @Override
    public Integer findNextInternalSequence(TenantId tenantId) {
        return workOrderPersistenceRepository.findNextInternalNumber(tenantId.value());
    }
}
