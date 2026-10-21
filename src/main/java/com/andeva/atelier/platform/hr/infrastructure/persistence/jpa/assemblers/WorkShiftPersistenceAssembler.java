package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GracePeriod;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.ShiftSchedule;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.WorkShiftPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WorkShiftPersistenceAssembler {

    public WorkShift toDomain(WorkShiftPersistenceEntity entity) {
        if (entity == null) return null;
        ShiftSchedule schedule = ShiftSchedule.of(entity.getStartTime(), entity.getEndTime());
        GracePeriod grace = GracePeriod.of(entity.getGracePeriodMinutes());

        WorkShift shift = new WorkShift(
                WorkShiftId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                entity.getName(),
                schedule,
                grace,
                Boolean.TRUE.equals(entity.getIsActive())
        );
        shift.assignWorkDays(parseDays(entity.getWorkDays()));
        return shift;
    }

    public WorkShiftPersistenceEntity toEntity(WorkShift domain) {
        if (domain == null) return null;
        WorkShiftPersistenceEntity entity = new WorkShiftPersistenceEntity(domain.getId().value());
        entity.setTenantId(domain.getTenantId().value());
        updateEntity(entity, domain);
        return entity;
    }

    public void updateEntity(WorkShiftPersistenceEntity entity, WorkShift domain) {
        entity.setName(domain.getName());
        entity.setStartTime(domain.getSchedule().startTime());
        entity.setEndTime(domain.getSchedule().endTime());
        entity.setGracePeriodMinutes(domain.getGracePeriod().minutes());
        entity.setIsActive(domain.isActive());
        entity.setWorkDays(formatDays(domain.getWorkDays()));
    }

    static Set<DayOfWeek> parseDays(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(day -> !day.isEmpty())
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
    }

    static String formatDays(Set<DayOfWeek> days) {
        if (days == null || days.size() == 7) {
            return null;
        }
        return days.stream().sorted().map(DayOfWeek::name).collect(Collectors.joining(","));
    }
}
