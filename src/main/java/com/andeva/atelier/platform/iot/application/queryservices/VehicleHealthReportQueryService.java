package com.andeva.atelier.platform.iot.application.queryservices;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleHealthReport;
import com.andeva.atelier.platform.iot.domain.model.queries.ExportVehicleHealthReportPdfQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetLatestVehicleHealthReportQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetVehicleHealthReportByIdQuery;

import java.util.Optional;

/**
 * Query Service port for issued vehicle health reports and their institutional PDF rendering.
 *
 * @author Joel Huamani Estefanero
 */
public interface VehicleHealthReportQueryService {

    /**
     * Retrieves the latest issued AI vehicle health report of a vehicle, without re-running inference.
     *
     * @param query query with target vehicle identifier
     * @return the latest stored report, if any
     */
    Optional<VehicleHealthReport> handle(GetLatestVehicleHealthReportQuery query);

    /**
     * Retrieves an issued report by identifier within the tenant.
     *
     * @param query query with report identifier
     * @return the stored report, if it exists in the tenant
     */
    Optional<VehicleHealthReport> handle(GetVehicleHealthReportByIdQuery query);

    /**
     * Renders the institutional PDF of an issued report.
     *
     * @param query query with report identifier
     * @return raw binary PDF byte array
     */
    byte[] handle(ExportVehicleHealthReportPdfQuery query);
}
