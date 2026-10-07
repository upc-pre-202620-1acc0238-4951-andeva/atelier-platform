package com.andeva.atelier.platform.iot.application.queryservices;

import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;

/**
 * Query Service for reading workshop preventative opportunities and vehicle predictive alerts.
 *
 * @author Joel Huamani Estefanero
 */
public interface PredictiveAlertQueryService {

    /**
     * Retrieves predictive alerts for a workshop tenant filtered by status.
     *
     * @param tenantId workshop tenant identifier
     * @param status   target alert status filter (e.g. DISPATCHED, ACKNOWLEDGED) or null for all
     * @return list of predictive alerts
     */
    List<PredictiveAlert> getActiveAlertsByTenant(TenantId tenantId, AlertStatus status);

    /**
     * Retrieves all predictive alerts recorded for a specific vehicle.
     *
     * @param vehicleId target vehicle identifier
     * @return list of predictive alerts
     */
    /**
     * Finds a single predictive alert by its strongly-typed identifier.
     *
     * @param alertId unique alert identifier
     * @return optional PredictiveAlert aggregate
     */
    java.util.Optional<PredictiveAlert> findById(com.andeva.atelier.platform.iot.domain.model.ids.AlertId alertId);

    List<PredictiveAlert> getAlertsByVehicle(VehicleId vehicleId);
}
