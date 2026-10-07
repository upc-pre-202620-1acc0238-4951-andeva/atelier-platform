package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.DeviceInstallationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link DeviceInstallationPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface DeviceInstallationPersistenceRepository extends JpaRepository<DeviceInstallationPersistenceEntity, UUID> {

    Optional<DeviceInstallationPersistenceEntity> findByVehicleIdAndUninstalledAtIsNull(UUID vehicleId);

    Optional<DeviceInstallationPersistenceEntity> findByDeviceIdAndUninstalledAtIsNull(UUID deviceId);

    List<DeviceInstallationPersistenceEntity> findAllByVehicleIdOrderByInstalledAtDesc(UUID vehicleId);

    List<DeviceInstallationPersistenceEntity> findAllByTenantIdAndUninstalledAtIsNull(UUID tenantId);
}
