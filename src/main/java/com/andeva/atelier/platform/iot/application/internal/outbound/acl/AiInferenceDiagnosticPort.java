package com.andeva.atelier.platform.iot.application.internal.outbound.acl;

import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Outbound Anti-Corruption Layer port for AI-assisted diagnostic evaluation and health report generation.
 *
 * @author Joel Huamani Estefanero
 */
public interface AiInferenceDiagnosticPort {

    /**
     * Generates a comprehensive structured vehicle health report using Groq LPU inference.
     *
     * @param tenantId                  workshop tenant identifier
     * @param vehicleId                 target vehicle identifier
     * @param daysToAnalyze             timeframe in days for historical telemetry analysis
     * @param includeResolvedDtcHistory whether to include resolved DTC fault history
     * @return structured AI vehicle health report
     */
    VehicleHealthReportAiDto generateVehicleDiagnostic(
            TenantId tenantId,
            VehicleId vehicleId,
            int daysToAnalyze,
            boolean includeResolvedDtcHistory
    );
}
