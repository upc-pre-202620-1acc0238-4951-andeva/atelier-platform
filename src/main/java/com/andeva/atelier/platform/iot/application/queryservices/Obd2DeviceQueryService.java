package com.andeva.atelier.platform.iot.application.queryservices;

import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.queries.GetDeviceByIdQuery;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Query Service for inspecting OBD-II hardware scanners across workshop tenants.
 *
 * @author Joel Huamani Estefanero
 */
public interface Obd2DeviceQueryService {

    /**
     * Finds a single OBD-II device by its strongly-typed identifier.
     *
     * @param query query with deviceId
     * @return optional Obd2Device aggregate
     */
    Optional<Obd2Device> handle(GetDeviceByIdQuery query);

    /**
     * Lists all OBD-II devices registered to a specific workshop tenant.
     *
     * @param tenantId workshop tenant identifier
     * @return list of Obd2Device aggregates
     */
    List<Obd2Device> getDevicesByTenant(TenantId tenantId);
}
