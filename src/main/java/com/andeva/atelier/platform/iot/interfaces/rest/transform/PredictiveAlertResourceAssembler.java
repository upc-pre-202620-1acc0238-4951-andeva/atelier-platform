package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.PredictiveAlertResponse;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId;
import org.springframework.stereotype.Component;

/**
 * Transforms {@link PredictiveAlert} aggregate roots into REST {@link PredictiveAlertResponse} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class PredictiveAlertResourceAssembler {

    public PredictiveAlertResponse toResponse(PredictiveAlert alert) {
        if (alert == null) {
            return null;
        }

        return new PredictiveAlertResponse(
                alert.getId().value(),
                alert.getVehicleId().value(),
                alert.getRecommendedServiceId().map(ServiceId::value).orElse(null),
                alert.getAlertType().name(),
                alert.getConfidenceScore().value(),
                alert.getMessage(),
                alert.getStatus().name(),
                alert.getCreatedAt()
        );
    }
}
