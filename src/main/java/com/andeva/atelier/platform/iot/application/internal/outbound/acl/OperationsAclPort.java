package com.andeva.atelier.platform.iot.application.internal.outbound.acl;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.UUID;

/**
 * Outbound Anti-Corruption Layer port for querying workshop operations and MRO service catalog.
 *
 * @author Joel Huamani Estefanero
 */
public interface OperationsAclPort {

    /**
     * Retrieves the available catalog services for the specified workshop tenant.
     *
     * @param tenantId workshop tenant identifier
     * @return list of service catalog items
     */
    List<WorkshopServiceCatalogItemDto> getAvailableWorkshopServices(TenantId tenantId);

    /**
     * Workshop service catalog item representation.
     */
    record WorkshopServiceCatalogItemDto(
            UUID serviceId,
            String serviceCode,
            String name,
            String category
    ) {}
}
