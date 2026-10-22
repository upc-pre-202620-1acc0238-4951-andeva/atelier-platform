package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.inventory.domain.model.entities.WorkOrderPartAllocation;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.repositories.WorkOrderPartAllocationRepository;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.WorkOrderPartAllocationPersistenceEntity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories.WorkOrderPartAllocationPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class WorkOrderPartAllocationRepositoryImpl implements WorkOrderPartAllocationRepository {

    private final WorkOrderPartAllocationPersistenceRepository persistenceRepository;

    public WorkOrderPartAllocationRepositoryImpl(WorkOrderPartAllocationPersistenceRepository persistenceRepository) {
        this.persistenceRepository = Objects.requireNonNull(persistenceRepository, "persistenceRepository cannot be null");
    }

    @Override
    public WorkOrderPartAllocation save(WorkOrderPartAllocation allocation) {
        WorkOrderPartAllocationPersistenceEntity entity = persistenceRepository.findById(allocation.getId())
                .orElseGet(() -> newEntity(allocation));
        entity.setReturnedQuantity(allocation.getReturnedQuantity());
        entity.setLastReturnedAt(allocation.getLastReturnedAt());
        entity.setLastReturnedBy(allocation.getLastReturnedBy());
        entity.setLastReturnReason(allocation.getLastReturnReason());
        return toDomain(persistenceRepository.save(entity));
    }

    @Override
    public List<WorkOrderPartAllocation> findOpenByWorkOrderTaskAndItem(UUID workOrderId, UUID taskId, InventoryItemId itemId) {
        var open = taskId != null
                ? persistenceRepository.findOpenOfTask(workOrderId, taskId, itemId.value())
                : persistenceRepository.findOpenOfOrder(workOrderId, itemId.value());
        return open.stream().map(this::toDomain).toList();
    }

    @Override
    public List<WorkOrderPartAllocation> findByWorkOrderId(UUID workOrderId) {
        return persistenceRepository.findByWorkOrderIdOrderByAllocatedAtAsc(workOrderId).stream().map(this::toDomain).toList();
    }

    private WorkOrderPartAllocationPersistenceEntity newEntity(WorkOrderPartAllocation allocation) {
        WorkOrderPartAllocationPersistenceEntity entity = new WorkOrderPartAllocationPersistenceEntity(allocation.getId());
        entity.setTenantId(allocation.getTenantId().value());
        entity.setItemId(allocation.getItemId().value());
        entity.setBatchId(allocation.getBatchId());
        entity.setWorkOrderId(allocation.getWorkOrderId());
        entity.setTaskId(allocation.getTaskId());
        entity.setQuantity(allocation.getQuantity());
        entity.setUnitCost(allocation.getUnitCost().amount());
        entity.setCurrency(allocation.getUnitCost().currency().name());
        entity.setAllocatedAt(allocation.getAllocatedAt());
        return entity;
    }

    private WorkOrderPartAllocation toDomain(WorkOrderPartAllocationPersistenceEntity entity) {
        return new WorkOrderPartAllocation(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                InventoryItemId.of(entity.getItemId()),
                entity.getBatchId(),
                entity.getWorkOrderId(),
                entity.getTaskId(),
                entity.getQuantity(),
                Money.of(entity.getUnitCost(), Currency.valueOf(entity.getCurrency())),
                entity.getAllocatedAt(),
                entity.getReturnedQuantity(),
                entity.getLastReturnedAt(),
                entity.getLastReturnedBy(),
                entity.getLastReturnReason()
        );
    }
}
