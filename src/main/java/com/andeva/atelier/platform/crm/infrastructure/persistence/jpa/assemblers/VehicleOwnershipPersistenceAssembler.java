package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.ids.VehicleOwnershipId;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.VehicleOwnershipPersistenceEntity;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

public final class VehicleOwnershipPersistenceAssembler {

    private VehicleOwnershipPersistenceAssembler() {
    }

    public static VehicleOwnership toDomain(VehicleOwnershipPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        VehicleId vehicleId = entity.getVehicle() != null
                ? VehicleId.of(entity.getVehicle().getId())
                : null;
        CustomerId customerId = entity.getCustomerId() != null
                ? CustomerId.of(entity.getCustomerId())
                : null;
        UserId userId = entity.getUserId() != null
                ? UserId.of(entity.getUserId())
                : null;

        return new VehicleOwnership(
                VehicleOwnershipId.of(entity.getId()),
                vehicleId,
                customerId,
                userId,
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    public static VehicleOwnershipPersistenceEntity toEntity(VehicleOwnership domain, VehiclePersistenceEntity vehicleEntity) {
        if (domain == null) {
            return null;
        }

        return new VehicleOwnershipPersistenceEntity(
                domain.getId().value(),
                vehicleEntity,
                domain.getCustomerId() != null ? domain.getCustomerId().value() : null,
                domain.getUserId() != null ? domain.getUserId().value() : null,
                domain.getStartDate(),
                domain.getEndDate()
        );
    }
}
