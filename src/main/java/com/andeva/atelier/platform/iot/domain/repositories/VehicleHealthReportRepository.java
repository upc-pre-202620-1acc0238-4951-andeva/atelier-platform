package com.andeva.atelier.platform.iot.domain.repositories;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleHealthReport;
import com.andeva.atelier.platform.iot.domain.model.ids.HealthReportId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for issued AI vehicle health reports.
 *
 * @author Joel Huamani Estefanero
 */
public interface VehicleHealthReportRepository {

    VehicleHealthReport save(VehicleHealthReport report);

    Optional<VehicleHealthReport> findById(HealthReportId id);

    Optional<VehicleHealthReport> findLatestByTenantIdAndVehicleId(TenantId tenantId, VehicleId vehicleId);

    /**
     * Latest report of each given vehicle, used for fleet consolidations.
     */
    List<VehicleHealthReport> findLatestByTenantIdAndVehicleIds(TenantId tenantId, List<VehicleId> vehicleIds);

    long countByTenantIdGeneratedSince(TenantId tenantId, Instant since);
}
