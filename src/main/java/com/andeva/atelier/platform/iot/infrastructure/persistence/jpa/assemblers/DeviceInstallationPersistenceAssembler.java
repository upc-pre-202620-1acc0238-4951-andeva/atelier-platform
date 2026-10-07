package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.DeviceInstallationPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Assembler for bidirectional translation between pure domain {@link DeviceInstallation}
 * aggregates and JPA {@link DeviceInstallationPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class DeviceInstallationPersistenceAssembler {

    public DeviceInstallationPersistenceEntity toEntity(DeviceInstallation domain) {
        if (domain == null) {
            return null;
        }
        var entity = new DeviceInstallationPersistenceEntity();
        entity.setId(domain.getId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setDeviceId(domain.getDeviceId().value());
        entity.setVehicleId(domain.getVehicleId().value());
        entity.setInstalledAt(domain.getInstalledAt());
        entity.setUninstalledAt(domain.getUninstalledAt().orElse(null));
        entity.setInitialOdometerKm(domain.getInitialOdometerKm());
        entity.setFinalOdometerKm(domain.getFinalOdometerKm().orElse(null));
        return entity;
    }

    public DeviceInstallation toDomain(DeviceInstallationPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DeviceInstallation(
                new InstallationId(entity.getId()),
                new DeviceId(entity.getDeviceId()),
                new VehicleId(entity.getVehicleId()),
                new TenantId(entity.getTenantId()),
                entity.getInstalledAt(),
                Optional.ofNullable(entity.getUninstalledAt()),
                entity.getInitialOdometerKm(),
                Optional.ofNullable(entity.getFinalOdometerKm())
        );
    }
}
