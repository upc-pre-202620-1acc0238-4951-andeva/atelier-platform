package com.andeva.atelier.platform.iot.application.internal.queryservices;

import com.andeva.atelier.platform.iot.application.queryservices.PredictiveAlertQueryService;
import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.iot.domain.repositories.PredictiveAlertRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Query Service implementation for consulting workshop predictive alerts and preventative opportunities.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class PredictiveAlertQueryServiceImpl implements PredictiveAlertQueryService {

    private final PredictiveAlertRepository predictiveAlertRepository;

    public PredictiveAlertQueryServiceImpl(PredictiveAlertRepository predictiveAlertRepository) {
        this.predictiveAlertRepository = Objects.requireNonNull(predictiveAlertRepository, "PredictiveAlertRepository cannot be null");
    }

    @Override
    public List<PredictiveAlert> getActiveAlertsByTenant(TenantId tenantId, AlertStatus status) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        if (status != null) {
            return predictiveAlertRepository.findAllByTenantIdAndStatus(tenantId, status);
        }
        return predictiveAlertRepository.findAllByTenantId(tenantId);
    }

    @Override
    public java.util.Optional<PredictiveAlert> findById(com.andeva.atelier.platform.iot.domain.model.ids.AlertId alertId) {
        Objects.requireNonNull(alertId, "AlertId cannot be null");
        return predictiveAlertRepository.findById(alertId);
    }

    @Override
    public List<PredictiveAlert> getAlertsByVehicle(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        return predictiveAlertRepository.findAllByVehicleId(vehicleId);
    }
}
