package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.util.Optional;
import java.util.UUID;

public interface CustomerFleetAclService {

    boolean isVehicleValidForTenant(UUID vehicleId, UUID tenantId);

    boolean isCustomerValid(UUID customerId, UUID tenantId);

    Optional<VehicleSummaryDto> fetchVehicleDetails(UUID vehicleId);

    Optional<CustomerSummaryDto> fetchCustomerDetails(UUID customerId);
}
