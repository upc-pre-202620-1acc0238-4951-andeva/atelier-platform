package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.VehicleFaultPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Assembler for bidirectional translation between pure domain {@link VehicleFault}
 * aggregates and JPA {@link VehicleFaultPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class VehicleFaultPersistenceAssembler {

    public VehicleFaultPersistenceEntity toEntity(VehicleFault domain) {
        if (domain == null) {
            return null;
        }
        var entity = new VehicleFaultPersistenceEntity();
        entity.setId(domain.getId().value());
        entity.setVehicleId(domain.getVehicleId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setDtcCode(domain.getDtcCode().value());
        entity.setSeverity(domain.getSeverity());
        entity.setDescription(domain.getDescription());
        entity.setDetectedAt(domain.getDetectedAt());
        entity.setResolved(domain.isResolved());
        entity.setResolvedAt(domain.getResolvedAt().orElse(null));
        return entity;
    }

    public VehicleFault toDomain(VehicleFaultPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new VehicleFault(
                new FaultId(entity.getId()),
                new VehicleId(entity.getVehicleId()),
                new TenantId(entity.getTenantId()),
                new DtcCode(entity.getDtcCode()),
                entity.getSeverity(),
                entity.getDescription(),
                entity.getDetectedAt(),
                entity.isResolved(),
                Optional.ofNullable(entity.getResolvedAt())
        );
    }
}
