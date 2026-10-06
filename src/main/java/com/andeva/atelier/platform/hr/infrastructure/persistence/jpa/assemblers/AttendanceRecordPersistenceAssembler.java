package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.AttendanceJustification;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.HaversineDistance;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.AttendanceRecordPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AttendanceRecordPersistenceAssembler {

    public AttendanceRecord toDomain(AttendanceRecordPersistenceEntity entity) {
        if (entity == null) return null;

        GeoCoordinates coords = GeoCoordinates.of(
                entity.getLatitude().doubleValue(),
                entity.getLongitude().doubleValue()
        );
        HaversineDistance distance = HaversineDistance.of(entity.getDistanceToBranchMeters());

        AttendanceJustification justification = null;
        if (entity.getJustificationReason() != null && entity.getJustifiedBy() != null && entity.getJustifiedAt() != null) {
            justification = AttendanceJustification.of(
                    entity.getJustificationReason(),
                    TenantMembershipId.of(entity.getJustifiedBy()),
                    entity.getJustifiedAt()
            );
        }

        return new AttendanceRecord(
                AttendanceRecordId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                BranchId.of(entity.getBranchId()),
                TenantMembershipId.of(entity.getMembershipId()),
                WorkShiftId.of(entity.getShiftId()),
                entity.getClockIn(),
                entity.getClockOut(),
                entity.getStatus(),
                coords,
                distance,
                justification
        );
    }

    public AttendanceRecordPersistenceEntity toEntity(AttendanceRecord domain) {
        if (domain == null) return null;
        AttendanceRecordPersistenceEntity entity = new AttendanceRecordPersistenceEntity(domain.getId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setBranchId(domain.getBranchId().value());
        entity.setMembershipId(domain.getMembershipId().value());
        entity.setShiftId(domain.getShiftId().value());
        entity.setClockIn(domain.getClockIn());
        entity.setClockOut(domain.getClockOut());
        entity.setStatus(domain.getStatus());
        entity.setLatitude(BigDecimal.valueOf(domain.getCoordinates().latitude()));
        entity.setLongitude(BigDecimal.valueOf(domain.getCoordinates().longitude()));
        entity.setDistanceToBranchMeters((int) Math.round(domain.getDistanceToBranch().meters()));

        domain.getJustification().ifPresent(j -> {
            entity.setJustificationReason(j.reason());
            entity.setJustifiedBy(j.justifiedBy().value());
            entity.setJustifiedAt(j.justifiedAt());
        });

        return entity;
    }

    public void updateEntity(AttendanceRecordPersistenceEntity entity, AttendanceRecord domain) {
        entity.setClockOut(domain.getClockOut());
        entity.setStatus(domain.getStatus());
        domain.getJustification().ifPresent(j -> {
            entity.setJustificationReason(j.reason());
            entity.setJustifiedBy(j.justifiedBy().value());
            entity.setJustifiedAt(j.justifiedAt());
        });
    }
}
