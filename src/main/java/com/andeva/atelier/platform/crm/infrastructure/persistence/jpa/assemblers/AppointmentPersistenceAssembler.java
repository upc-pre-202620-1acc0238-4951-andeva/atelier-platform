package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.AppointmentPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

public final class AppointmentPersistenceAssembler {

    private AppointmentPersistenceAssembler() {
    }

    public static Appointment toDomain(AppointmentPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Appointment(
                AppointmentId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                BranchId.of(entity.getBranchId()),
                CustomerId.of(entity.getCustomerId()),
                VehicleId.of(entity.getVehicleId()),
                entity.getScheduledAt(),
                entity.getEstimatedDurationMinutes(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCancellationReason()
        );
    }

    public static AppointmentPersistenceEntity toEntity(Appointment domain) {
        if (domain == null) {
            return null;
        }

        return new AppointmentPersistenceEntity(
                domain.id().value(),
                domain.tenantId().value(),
                domain.branchId().value(),
                domain.customerId().value(),
                domain.vehicleId().value(),
                domain.scheduledAt(),
                domain.estimatedDurationMinutes(),
                domain.reason(),
                domain.status(),
                domain.cancellationReason()
        );
    }
}
