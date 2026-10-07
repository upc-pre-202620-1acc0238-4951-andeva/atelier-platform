package com.andeva.atelier.platform.iot.domain;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.iot.domain.services.DtcCodeEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for DtcCodeEvaluationService.
 *
 * @author Joel Huamani Estefanero
 */
class DtcCodeEvaluationServiceTest {

    private DtcCodeEvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new DtcCodeEvaluationService();
    }

    @ParameterizedTest
    @CsvSource({
            "P0300, CRITICAL",
            "P0301, CRITICAL",
            "P0304, CRITICAL",
            "P0217, CRITICAL",
            "P0117, CRITICAL",
            "P0524, CRITICAL",
            "P0521, CRITICAL",
            "P0420, MEDIUM",
            "P0430, MEDIUM",
            "P0171, MEDIUM",
            "P0130, MEDIUM",
            "P0700, MEDIUM",
            "B0001, LOW",
            "C0001, LOW",
            "U0100, LOW",
            "P0999, LOW"
    })
    @DisplayName("evaluateSeverity should correctly categorize DTC code by severity")
    void testEvaluateSeverity(String dtcStr, FaultSeverity expectedSeverity) {
        DtcCode code = DtcCode.of(dtcStr);
        FaultSeverity actualSeverity = evaluationService.evaluateSeverity(code);
        assertThat(actualSeverity).isEqualTo(expectedSeverity);
    }

    @Test
    @DisplayName("resolveRecommendedAlertType should map known DTC codes to preventive AlertType")
    void testResolveRecommendedAlertType() {
        assertThat(evaluationService.resolveRecommendedAlertType(DtcCode.of("P0300")))
                .contains(AlertType.CYLINDER_MISFIRE_HAZARD);

        assertThat(evaluationService.resolveRecommendedAlertType(DtcCode.of("P0420")))
                .contains(AlertType.CATALYTIC_SYSTEM_DEGRADATION);

        assertThat(evaluationService.resolveRecommendedAlertType(DtcCode.of("P0217")))
                .contains(AlertType.ENGINE_OVERHEATING_RISK);

        assertThat(evaluationService.resolveRecommendedAlertType(DtcCode.of("P0560")))
                .contains(AlertType.BATTERY_FAILURE_RISK);

        assertThat(evaluationService.resolveRecommendedAlertType(DtcCode.of("B0001")))
                .isEmpty();
    }
}
