package com.andeva.atelier.platform.iot.domain.repositories;

import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound domain repository port for managing PredictiveAlert aggregate roots.
 *
 * @author Joel Huamani Estefanero
 */
public interface PredictiveAlertRepository {

    PredictiveAlert save(PredictiveAlert alert);

    Optional<PredictiveAlert> findById(AlertId id);

    List<PredictiveAlert> findAllByVehicleId(VehicleId vehicleId);

    List<PredictiveAlert> findAllByTenantIdAndStatus(TenantId tenantId, AlertStatus status);

    List<PredictiveAlert> findAllByTenantId(TenantId tenantId);
}
