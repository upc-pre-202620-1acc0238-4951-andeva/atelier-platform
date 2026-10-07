package com.andeva.atelier.platform.iot.domain.repositories;

import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound domain repository port for managing Obd2Device aggregate roots.
 *
 * @author Joel Huamani Estefanero
 */
public interface Obd2DeviceRepository {

    Obd2Device save(Obd2Device device);

    Optional<Obd2Device> findById(DeviceId id);

    Optional<Obd2Device> findByIdentifier(DeviceIdentifier identifier);

    List<Obd2Device> findAllByTenantId(TenantId tenantId);

    boolean existsByIdentifier(DeviceIdentifier identifier);

    boolean existsById(DeviceId id);
}
