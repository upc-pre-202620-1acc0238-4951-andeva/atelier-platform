package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.iot.domain.repositories.Obd2DeviceRepository;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers.Obd2DevicePersistenceAssembler;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories.Obd2DevicePersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Secondary adapter implementing {@link Obd2DeviceRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public class Obd2DeviceRepositoryAdapter implements Obd2DeviceRepository {

    private final Obd2DevicePersistenceRepository persistenceRepository;
    private final Obd2DevicePersistenceAssembler assembler;

    public Obd2DeviceRepositoryAdapter(
            Obd2DevicePersistenceRepository persistenceRepository,
            Obd2DevicePersistenceAssembler assembler) {
        this.persistenceRepository = persistenceRepository;
        this.assembler = assembler;
    }

    @Override
    public Obd2Device save(Obd2Device device) {
        Objects.requireNonNull(device, "device cannot be null");
        var entity = assembler.toEntity(device);
        return assembler.toDomain(persistenceRepository.save(entity));
    }

    @Override
    public Optional<Obd2Device> findById(DeviceId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<Obd2Device> findByIdentifier(DeviceIdentifier identifier) {
        Objects.requireNonNull(identifier, "identifier cannot be null");
        return persistenceRepository.findByDeviceIdentifier(identifier.value()).map(assembler::toDomain);
    }

    @Override
    public List<Obd2Device> findAllByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return persistenceRepository.findAllByTenantId(tenantId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public boolean existsByIdentifier(DeviceIdentifier identifier) {
        Objects.requireNonNull(identifier, "identifier cannot be null");
        return persistenceRepository.existsByDeviceIdentifier(identifier.value());
    }

    @Override
    public boolean existsById(DeviceId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.existsById(id.value());
    }
}
