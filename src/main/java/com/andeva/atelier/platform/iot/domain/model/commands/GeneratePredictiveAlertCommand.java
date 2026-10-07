package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Domain command to generate an analytical or AI-driven predictive maintenance alert.
 *
 * @author Joel Huamani Estefanero
 */
public record GeneratePredictiveAlertCommand(
        VehicleId vehicleId,
        TenantId tenantId,
        Optional<ServiceId> serviceId,
        AlertType type,
        ConfidenceScore score,
        String message
) implements Serializable {

    public GeneratePredictiveAlertCommand {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        serviceId = serviceId != null ? serviceId : Optional.empty();
        Objects.requireNonNull(type, "AlertType cannot be null");
        Objects.requireNonNull(score, "ConfidenceScore cannot be null");
        message = message != null ? message.trim() : "";
    }
}
