package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.Obd2DevicePersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Assembler for bidirectional translation between pure domain {@link Obd2Device}
 * aggregates and JPA {@link Obd2DevicePersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class Obd2DevicePersistenceAssembler {

    public Obd2DevicePersistenceEntity toEntity(Obd2Device domain) {
        if (domain == null) {
            return null;
        }
        var entity = new Obd2DevicePersistenceEntity();
        entity.setId(domain.getId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setDeviceIdentifier(domain.getDeviceIdentifier().value());
        entity.setConnectionType(domain.getConnectionType());
        entity.setStatus(domain.getStatus());
        entity.setHardwareModel(domain.getHardwareModel());
        entity.setFirmwareVersion(domain.getFirmwareVersion());
        return entity;
    }

    public Obd2Device toDomain(Obd2DevicePersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Obd2Device(
                new DeviceId(entity.getId()),
                new TenantId(entity.getTenantId()),
                new DeviceIdentifier(entity.getDeviceIdentifier()),
                entity.getConnectionType(),
                entity.getStatus(),
                entity.getHardwareModel(),
                entity.getFirmwareVersion()
        );
    }
}
