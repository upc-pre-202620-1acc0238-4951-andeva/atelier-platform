package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskProduct;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskProductId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskPersistenceEntity;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskProductPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

public final class WorkOrderTaskProductPersistenceAssembler {

    private WorkOrderTaskProductPersistenceAssembler() {
    }

    public static WorkOrderTaskProduct toDomain(WorkOrderTaskProductPersistenceEntity entity) {
        if (entity == null) return null;

        WorkOrderTaskId taskId = entity.getTask() != null ? WorkOrderTaskId.of(entity.getTask().getId()) : null;

        return new WorkOrderTaskProduct(
                WorkOrderTaskProductId.of(entity.getId()),
                taskId,
                entity.getProductId(),
                Quantity.of(entity.getQuantity()),
                Money.soles(entity.getUnitPrice()),
                Money.soles(entity.getTotalAmount())
        );
    }

    public static WorkOrderTaskProductPersistenceEntity toEntity(WorkOrderTaskProduct domain, WorkOrderTaskPersistenceEntity parent) {
        if (domain == null) return null;

        WorkOrderTaskProductPersistenceEntity entity = new WorkOrderTaskProductPersistenceEntity();
        entity.setId(domain.getId().value());
        entity.setTask(parent);
        entity.setProductId(domain.getProductId());
        entity.setQuantity(domain.getQuantity().value());
        entity.setUnitPrice(domain.getUnitPrice().amount());
        entity.setTotalAmount(domain.getTotalAmount().amount());
        return entity;
    }
}
