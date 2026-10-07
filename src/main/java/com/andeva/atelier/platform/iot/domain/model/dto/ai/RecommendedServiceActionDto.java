package com.andeva.atelier.platform.iot.domain.model.dto.ai;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Recommended maintenance service package recommended by the AI inference engine,
 * aligned with the workshop's MRO service catalog.
 *
 * @author Joel Huamani Estefanero
 */
public record RecommendedServiceActionDto(
        String serviceCode,
        String serviceName,
        String urgency,
        BigDecimal estimatedCost,
        String rationale
) implements Serializable {

    public RecommendedServiceActionDto {
        serviceCode = serviceCode != null ? serviceCode : "SRV-GEN";
        serviceName = serviceName != null ? serviceName : "Preventive Inspection";
        urgency = urgency != null ? urgency : "MEDIUM";
        estimatedCost = estimatedCost != null ? estimatedCost : BigDecimal.ZERO;
        rationale = rationale != null ? rationale : "";
    }
}
