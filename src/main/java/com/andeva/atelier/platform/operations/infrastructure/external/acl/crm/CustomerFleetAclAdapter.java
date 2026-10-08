package com.andeva.atelier.platform.operations.infrastructure.external.acl.crm;

import com.andeva.atelier.platform.crm.interfaces.acl.CustomerFleetContextFacade;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerFleetAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.VehicleSummaryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Anti-Corruption Layer adapter integrating Workshop Operations with CRM and Fleet Management.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class CustomerFleetAclAdapter implements CustomerFleetAclService {

    private final CustomerFleetContextFacade customerFleetContextFacade;

    public CustomerFleetAclAdapter() {
        this(null);
    }

    @Autowired
    public CustomerFleetAclAdapter(@Autowired(required = false) CustomerFleetContextFacade customerFleetContextFacade) {
        this.customerFleetContextFacade = customerFleetContextFacade;
    }

    @Override
    public boolean isVehicleValidForTenant(UUID vehicleId, UUID tenantId) {
        if (vehicleId == null || tenantId == null) {
            return false;
        }
        if (customerFleetContextFacade == null) {
            return true;
        }
        return customerFleetContextFacade.fetchVehicleById(vehicleId)
                .map(v -> {
                    if (v.currentOwnerId() == null) {
                        return false;
                    }
                    return customerFleetContextFacade.fetchCustomerById(v.currentOwnerId())
                            .map(c -> tenantId.equals(c.tenantId()))
                            .orElse(false);
                })
                .orElse(false);
    }

    @Override
    public boolean isCustomerValid(UUID customerId, UUID tenantId) {
        if (customerId == null || tenantId == null) {
            return false;
        }
        if (customerFleetContextFacade == null) {
            return true;
        }
        return customerFleetContextFacade.fetchCustomerById(customerId)
                .map(customer -> tenantId.equals(customer.tenantId()) && "ACTIVE".equalsIgnoreCase(customer.status()))
                .orElse(false);
    }

    @Override
    public Optional<VehicleSummaryDto> fetchVehicleDetails(UUID vehicleId) {
        if (vehicleId == null) {
            return Optional.empty();
        }
        if (customerFleetContextFacade == null) {
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
        return customerFleetContextFacade.fetchVehicleById(vehicleId)
                .map(v -> new VehicleSummaryDto(
                        v.id(),
                        v.plate(),
                        v.vin(),
                        v.brand(),
                        v.model(),
                        v.year(),
                        v.currentOwnerId()
                ));
    }

    @Override
    public Optional<CustomerSummaryDto> fetchCustomerDetails(UUID customerId) {
        if (customerId == null) {
            return Optional.empty();
        }
        if (customerFleetContextFacade == null) {
            return Optional.of(new CustomerSummaryDto(
                    customerId,
                    "Customer Name",
                    "10456789012",
                    "customer@atelier.com",
                    "+51987654321",
                    "INDIVIDUAL"
            ));
        }
        return customerFleetContextFacade.fetchCustomerById(customerId)
                .map(c -> new CustomerSummaryDto(
                        c.id(),
                        c.displayName(),
                        c.taxId(),
                        c.email(),
                        c.phone(),
                        c.type()
                ));
    }
}
