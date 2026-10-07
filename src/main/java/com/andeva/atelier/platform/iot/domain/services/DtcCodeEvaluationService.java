package com.andeva.atelier.platform.iot.domain.services;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * Domain service evaluating Diagnostic Trouble Codes (DTCs) against automotive industry standards
 * (SAE J2012 / ISO 15031-6) to infer severity, risk profiles, and recommended corrective alerts.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class DtcCodeEvaluationService {

    /**
     * Determines default fault severity from DTC standardized nomenclature and domain heuristics.
     *
     * @param code the standardized DTC code
     * @return evaluated FaultSeverity (CRITICAL, MEDIUM, LOW)
     */
    public FaultSeverity evaluateSeverity(DtcCode code) {
        Objects.requireNonNull(code, "DtcCode cannot be null");
        String codeVal = code.value();

        // 1. Engine Cylinder Misfires (P0300 - P0309) -> CRITICAL
        // Unburned fuel directly damages the catalytic monolith and creates thermal runaway
        if (codeVal.startsWith("P030")) {
            return FaultSeverity.CRITICAL;
        }

        // 2. Severe Overheating / Coolant Temp Sensors (P0117, P0118, P0217) -> CRITICAL
        if (codeVal.equals("P0217") || codeVal.equals("P0117") || codeVal.equals("P0118")) {
            return FaultSeverity.CRITICAL;
        }

        // 3. Oil Pressure / Level Emergencies (P0520, P0521, P0524) -> CRITICAL
        if (codeVal.equals("P0524") || codeVal.equals("P0520") || codeVal.equals("P0521")) {
            return FaultSeverity.CRITICAL;
        }

        // 4. Catalyst System & Emissions (P0420, P0430, P0171, P0172, P0130 - P0167) -> MEDIUM
        if (codeVal.startsWith("P042") || codeVal.startsWith("P043") || codeVal.startsWith("P017") || codeVal.startsWith("P013")) {
            return FaultSeverity.MEDIUM;
        }

        // 5. Powertrain Transmission Slippage (P0700 - P0799) -> MEDIUM
        if (codeVal.startsWith("P07")) {
            return FaultSeverity.MEDIUM;
        }

        // 6. Body, Chassis, Network (Bxxxx, Cxxxx, Uxxxx) or non-critical P codes -> LOW
        return FaultSeverity.LOW;
    }

    /**
     * Maps an electronic DTC code to a corresponding predictive alert type, if applicable.
     */
    public Optional<AlertType> resolveRecommendedAlertType(DtcCode code) {
        Objects.requireNonNull(code, "DtcCode cannot be null");
        String codeVal = code.value();

        if (codeVal.startsWith("P030")) {
            return Optional.of(AlertType.CYLINDER_MISFIRE_HAZARD);
        }
        if (codeVal.equals("P0420") || codeVal.equals("P0430")) {
            return Optional.of(AlertType.CATALYTIC_SYSTEM_DEGRADATION);
        }
        if (codeVal.equals("P0217") || codeVal.equals("P0117") || codeVal.equals("P0118")) {
            return Optional.of(AlertType.ENGINE_OVERHEATING_RISK);
        }
        if (codeVal.equals("P0560") || codeVal.equals("P0562")) {
            return Optional.of(AlertType.BATTERY_FAILURE_RISK);
        }

        return Optional.empty();
    }
}
