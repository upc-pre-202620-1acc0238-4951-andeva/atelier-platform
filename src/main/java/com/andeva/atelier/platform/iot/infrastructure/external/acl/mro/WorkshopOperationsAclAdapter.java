package com.andeva.atelier.platform.iot.infrastructure.external.acl.mro;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.OperationsAclPort;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Anti-Corruption Layer adapter for communicating with Workshop Operations (MRO) service catalog.
 * Provides standard catalog services (cooling, electrical, ignition, exhaust) for the tenant
 * while MRO bounded context is running in parallel.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class WorkshopOperationsAclAdapter implements OperationsAclPort {

    @Override
    public List<WorkshopServiceCatalogItemDto> getAvailableWorkshopServices(TenantId tenantId) {
        return List.of(
                new WorkshopServiceCatalogItemDto(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        "SRV-COOL-01",
                        "Cooling System & Thermostat Overhaul",
                        "Cooling"
                ),
                new WorkshopServiceCatalogItemDto(
                        UUID.fromString("00000000-0000-0000-0000-000000000002"),
                        "SRV-ELEC-01",
                        "Battery & Alternator Electrical Diagnostics",
                        "Electrical"
                ),
                new WorkshopServiceCatalogItemDto(
                        UUID.fromString("00000000-0000-0000-0000-000000000003"),
                        "SRV-IGN-01",
                        "Cylinder Ignition Coils & Spark Plugs Replacement",
                        "Ignition"
                ),
                new WorkshopServiceCatalogItemDto(
                        UUID.fromString("00000000-0000-0000-0000-000000000004"),
                        "SRV-EMIS-01",
                        "Catalytic Converter & O2 Sensors Inspection",
                        "Emissions"
                )
        );
    }
}
