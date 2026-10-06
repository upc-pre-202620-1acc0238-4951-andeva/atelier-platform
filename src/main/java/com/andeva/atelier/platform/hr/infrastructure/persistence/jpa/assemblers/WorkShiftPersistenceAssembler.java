package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GracePeriod;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.ShiftSchedule;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.WorkShiftPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

@Component
public class WorkShiftPersistenceAssembler {

    public WorkShift toDomain(WorkShiftPersistenceEntity entity) {
        if (entity == null) return null;
        ShiftSchedule schedule = ShiftSchedule.of(entity.getStartTime(), entity.getEndTime());
        GracePeriod grace = GracePeriod.of(entity.getGracePeriodMinutes());

        return new WorkShift(
                WorkShiftId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                entity.getName(),
                schedule,
                grace,
                Boolean.TRUE.equals(entity.getIsActive())
        );
    }

    public WorkShiftPersistenceEntity toEntity(WorkShift domain) {
        if (domain == null) return null;
        WorkShiftPersistenceEntity entity = new WorkShiftPersistenceEntity(domain.getId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setName(domain.getName());
        entity.setStartTime(domain.getSchedule().startTime());
        entity.setEndTime(domain.getSchedule().endTime());
        entity.setGracePeriodMinutes(domain.getGracePeriod().minutes());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public void updateEntity(WorkShiftPersistenceEntity entity, WorkShift domain) {
        entity.setName(domain.getName());
        entity.setStartTime(domain.getSchedule().startTime());
        entity.setEndTime(domain.getSchedule().endTime());
        entity.setGracePeriodMinutes(domain.getGracePeriod().minutes());
        entity.setIsActive(domain.isActive());
    }
}
