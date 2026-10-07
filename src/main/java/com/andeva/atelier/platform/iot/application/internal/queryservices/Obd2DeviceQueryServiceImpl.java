package com.andeva.atelier.platform.iot.application.internal.queryservices;

import com.andeva.atelier.platform.iot.application.queryservices.Obd2DeviceQueryService;
import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.queries.GetDeviceByIdQuery;
import com.andeva.atelier.platform.iot.domain.repositories.Obd2DeviceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Query Service implementation for OBD-II telematics device inventory lookups.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class Obd2DeviceQueryServiceImpl implements Obd2DeviceQueryService {

    private final Obd2DeviceRepository obd2DeviceRepository;

    public Obd2DeviceQueryServiceImpl(Obd2DeviceRepository obd2DeviceRepository) {
        this.obd2DeviceRepository = Objects.requireNonNull(obd2DeviceRepository, "Obd2DeviceRepository cannot be null");
    }

    @Override
    public Optional<Obd2Device> handle(GetDeviceByIdQuery query) {
        Objects.requireNonNull(query, "GetDeviceByIdQuery cannot be null");
        return obd2DeviceRepository.findById(query.deviceId());
    }

    @Override
    public List<Obd2Device> getDevicesByTenant(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        return obd2DeviceRepository.findAllByTenantId(tenantId);
    }
}
