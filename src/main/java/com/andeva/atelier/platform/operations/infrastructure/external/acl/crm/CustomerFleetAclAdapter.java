package com.andeva.atelier.platform.operations.infrastructure.external.acl.crm;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerFleetAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.VehicleSummaryDto;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CustomerFleetAclAdapter implements CustomerFleetAclService {

    @Override
    public boolean isVehicleValidForTenant(UUID vehicleId, UUID tenantId) {
        return vehicleId != null && tenantId != null;
    }

    @Override
    public boolean isCustomerValid(UUID customerId, UUID tenantId) {
        return customerId != null && tenantId != null;
    }

    @Override
    public Optional<VehicleSummaryDto> fetchVehicleDetails(UUID vehicleId) {
        if (vehicleId == null) return Optional.empty();
        return Optional.of(new VehicleSummaryDto(
                vehicleId,
                "ABC-123",
                "1HGCR2F83HA000000",
                "Toyota",
                "Corolla",
                2022,
                UUID.randomUUID()
        ));
    }

    @Override
    public Optional<CustomerSummaryDto> fetchCustomerDetails(UUID customerId) {
        if (customerId == null) return Optional.empty();
        return Optional.of(new CustomerSummaryDto(
                customerId,
                "Customer Name",
                "10456789012",
                "customer@atelier.com",
                "+51987654321",
                "INDIVIDUAL"
        ));
    }
}
