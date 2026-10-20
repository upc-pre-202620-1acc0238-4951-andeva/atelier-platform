package com.andeva.atelier.platform.iot.infrastructure.external.acl.crm;

import com.andeva.atelier.platform.crm.interfaces.acl.CustomerFleetContextFacade;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.CustomerAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.VehicleAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.RecommendedServiceActionDto;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti-Corruption Layer adapter for communicating with Customer & Fleet Management (CRM).
 * Provides resilient fallbacks and simulated device token lookups while delegating to
 * {@link CustomerFleetContextFacade} for real CRM queries and preventative appointments.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class CrmFleetAclAdapter implements CrmFleetAclPort {

    private static final Logger log = LoggerFactory.getLogger(CrmFleetAclAdapter.class);

    private final ConcurrentHashMap<UUID, String> mockDeviceTokens = new ConcurrentHashMap<>();

    private final CustomerFleetContextFacade customerFleetContextFacade;
    private final TenancyContextFacade tenancyContextFacade;

    public CrmFleetAclAdapter() {
        this(null, null);
    }

    public CrmFleetAclAdapter(CustomerFleetContextFacade customerFleetContextFacade) {
        this(customerFleetContextFacade, null);
    }

    @Autowired
    public CrmFleetAclAdapter(
            @Autowired(required = false) CustomerFleetContextFacade customerFleetContextFacade,
            @Autowired(required = false) TenancyContextFacade tenancyContextFacade) {
        this.customerFleetContextFacade = customerFleetContextFacade;
        this.tenancyContextFacade = tenancyContextFacade;
    }

    /**
     * Resolves the FCM token of a driver linked to the vehicle's current owner
     * (vehicle → owner customer → active member accounts → registered device token).
     */
    @Override
    public String getDriverFcmDeviceToken(VehicleId vehicleId) {
        if (vehicleId == null) {
            return null;
        }
        String registered = mockDeviceTokens.get(vehicleId.value());
        if (registered != null) {
            return registered;
        }
        if (customerFleetContextFacade == null || tenancyContextFacade == null) {
            return "fcm_token_simulated_" + vehicleId.value().toString().substring(0, 8);
        }
        try {
            return customerFleetContextFacade.fetchCurrentOwnerId(vehicleId.value())
                    .flatMap(ownerId -> customerFleetContextFacade.fetchActiveMembershipsByCustomerId(ownerId).stream()
                            .map(membership -> tenancyContextFacade.fetchUserDeviceToken(membership.userId()))
                            .flatMap(Optional::stream)
                            .findFirst())
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Failed to resolve driver FCM token for vehicle {}: {}", vehicleId.value(), e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isVehicleRegistered(VehicleId vehicleId) {
        if (vehicleId == null) {
            return false;
        }
        if (customerFleetContextFacade != null) {
            try {
                return customerFleetContextFacade.fetchVehicleById(vehicleId.value()).isPresent();
            } catch (Exception e) {
                log.warn("Failed to check if vehicle is registered, falling back", e);
            }
        }
        return true;
    }

    @Override
    public UUID convertAlertToAppointment(
            AlertId alertId,
            TenantId tenantId,
            VehicleId vehicleId,
            String description,
            RecommendedServiceActionDto recommendedAction) {

        if (customerFleetContextFacade != null && tenantId != null && vehicleId != null) {
            try {
                Optional<UUID> appointmentIdOpt = customerFleetContextFacade.schedulePreventiveAppointment(
                        tenantId.value(),
                        vehicleId.value(),
                        description
                );
                if (appointmentIdOpt.isPresent()) {
                    return appointmentIdOpt.get();
                }
            } catch (Exception e) {
                log.warn("Failed to schedule appointment via CRM facade", e);
            }
        }

        UUID appointmentId = UUID.randomUUID();
        log.info("Simulated CRM preventative appointment {} generated for vehicle {} from alert {}",
                appointmentId, vehicleId != null ? vehicleId.value() : "null", alertId != null ? alertId.value() : "null");
        return appointmentId;
    }

    @Override
    public Optional<VehicleMetadataDto> getVehicleMetadata(VehicleId vehicleId) {
        if (vehicleId == null) {
            return Optional.empty();
        }

        if (customerFleetContextFacade != null) {
            try {
                Optional<VehicleAclDto> vehicleOpt = customerFleetContextFacade.fetchVehicleById(vehicleId.value());
                if (vehicleOpt.isPresent()) {
                    VehicleAclDto v = vehicleOpt.get();
                    String ownerName = "Unknown Owner";
                    if (v.currentOwnerId() != null) {
                        Optional<CustomerAclDto> customerOpt = customerFleetContextFacade.fetchCustomerById(v.currentOwnerId());
                        if (customerOpt.isPresent()) {
                            ownerName = customerOpt.get().displayName();
                        }
                    }
                    return Optional.of(new VehicleMetadataDto(v.plate(), v.vin(), v.brand(), v.model(), v.year(), ownerName));
                }
            } catch (Exception e) {
                log.warn("Failed to fetch vehicle metadata via CRM facade, falling back", e);
            }
        }

        return Optional.of(new VehicleMetadataDto(
                "ABC-123",
                "1HGCR2F83HA" + vehicleId.value().toString().substring(0, 6).toUpperCase(),
                "Toyota",
                "Hilux D-4D",
                2022,
                "Workshop Fleet Customer"
        ));
    }

    /**
     * Test helper to register custom FCM tokens for verification.
     */
    public void registerDriverFcmToken(UUID vehicleId, String token) {
        this.mockDeviceTokens.put(vehicleId, token);
    }

    @Override
    public java.util.List<FleetVehicleDto> listCustomerFleet(java.util.UUID customerId) {
        if (customerId == null || customerFleetContextFacade == null) {
            return java.util.List.of();
        }
        return customerFleetContextFacade.fetchVehiclesByCustomerId(customerId).stream()
                .map(v -> new FleetVehicleDto(VehicleId.of(v.id()), v.plate(), v.brand(), v.model()))
                .toList();
    }

    @Override
    public Optional<String> getCustomerName(java.util.UUID tenantId, java.util.UUID customerId) {
        if (tenantId == null || customerId == null || customerFleetContextFacade == null) {
            return Optional.empty();
        }
        return customerFleetContextFacade.fetchCustomerById(customerId)
                .filter(customer -> tenantId.equals(customer.tenantId()))
                .map(CustomerAclDto::displayName);
    }
}
