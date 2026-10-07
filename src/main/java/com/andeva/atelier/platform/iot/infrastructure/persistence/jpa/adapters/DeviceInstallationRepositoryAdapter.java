package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.iot.domain.repositories.DeviceInstallationRepository;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers.DeviceInstallationPersistenceAssembler;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories.DeviceInstallationPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Secondary adapter implementing {@link DeviceInstallationRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public class DeviceInstallationRepositoryAdapter implements DeviceInstallationRepository {

    private final DeviceInstallationPersistenceRepository persistenceRepository;
    private final DeviceInstallationPersistenceAssembler assembler;

    public DeviceInstallationRepositoryAdapter(
            DeviceInstallationPersistenceRepository persistenceRepository,
            DeviceInstallationPersistenceAssembler assembler) {
        this.persistenceRepository = persistenceRepository;
        this.assembler = assembler;
    }

    @Override
    public DeviceInstallation save(DeviceInstallation installation) {
        Objects.requireNonNull(installation, "installation cannot be null");
        return assembler.toDomain(persistenceRepository.save(assembler.toEntity(installation)));
    }

    @Override
    public Optional<DeviceInstallation> findById(InstallationId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<DeviceInstallation> findActiveByVehicleId(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        return persistenceRepository.findByVehicleIdAndUninstalledAtIsNull(vehicleId.value())
                .map(assembler::toDomain);
    }

    @Override
    public Optional<DeviceInstallation> findActiveByDeviceId(DeviceId deviceId) {
        Objects.requireNonNull(deviceId, "deviceId cannot be null");
        return persistenceRepository.findByDeviceIdAndUninstalledAtIsNull(deviceId.value())
                .map(assembler::toDomain);
    }

    @Override
    public List<DeviceInstallation> findAllHistoryByVehicleId(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        return persistenceRepository.findAllByVehicleIdOrderByInstalledAtDesc(vehicleId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public List<DeviceInstallation> findAllActiveByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return persistenceRepository.findAllByTenantIdAndUninstalledAtIsNull(tenantId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }
}
