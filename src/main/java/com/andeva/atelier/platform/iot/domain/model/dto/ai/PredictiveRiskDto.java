package com.andeva.atelier.platform.iot.domain.model.dto.ai;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Predictive mechanical risk model quantifying imminent component failure probability,
 * temporal horizon, and severe operational consequences.
 *
 * @author Joel Huamani Estefanero
 */
public record PredictiveRiskDto(
        String category,
        BigDecimal probability,
        int timeHorizonDays,
        String consequences
) implements Serializable {

    public PredictiveRiskDto {
        category = category != null ? category : "General";
        probability = probability != null ? probability : BigDecimal.ZERO;
        consequences = consequences != null ? consequences : "";
    }
}
