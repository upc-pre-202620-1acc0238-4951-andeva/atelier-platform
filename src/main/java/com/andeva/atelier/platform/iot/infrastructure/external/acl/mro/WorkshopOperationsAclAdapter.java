package com.andeva.atelier.platform.iot.infrastructure.external.acl.mro;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.OperationsAclPort;
import com.andeva.atelier.platform.operations.interfaces.acl.WorkshopOperationsContextFacade;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkshopServiceCatalogAclDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Anti-Corruption Layer adapter for communicating with Workshop Operations (MRO) service catalog.
 * Provides standard catalog services (cooling, electrical, ignition, exhaust) for the tenant
 * while delegating to {@link WorkshopOperationsContextFacade} for real catalog retrieval.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class WorkshopOperationsAclAdapter implements OperationsAclPort {

    private static final Logger log = LoggerFactory.getLogger(WorkshopOperationsAclAdapter.class);

    private final WorkshopOperationsContextFacade operationsFacade;

    public WorkshopOperationsAclAdapter() {
        this(null);
    }

    @Autowired(required = false)
    public WorkshopOperationsAclAdapter(WorkshopOperationsContextFacade operationsFacade) {
        this.operationsFacade = operationsFacade;
    }

    @Override
    public List<WorkshopServiceCatalogItemDto> getAvailableWorkshopServices(TenantId tenantId) {
        if (operationsFacade != null && tenantId != null) {
            try {
                List<WorkshopServiceCatalogAclDto> services = operationsFacade.fetchAvailableServices(tenantId.value());
                if (services != null && !services.isEmpty()) {
                    return services.stream()
                            .map(s -> new WorkshopServiceCatalogItemDto(
                                    s.id(),
                                    "SRV-" + s.id().toString().substring(0, 8).toUpperCase(),
                                    s.name(),
                                    "General Workshop"
                            ))
                            .toList();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch available workshop services via Operations facade, falling back to defaults", e);
            }
        }

        return getDefaultCatalogItems();
    }

    private List<WorkshopServiceCatalogItemDto> getDefaultCatalogItems() {
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
