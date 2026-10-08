package com.andeva.atelier.platform.iot.infrastructure.external.acl.crm;

import com.andeva.atelier.platform.crm.interfaces.acl.CustomerFleetContextFacade;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.CustomerAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.VehicleAclDto;
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

    public CrmFleetAclAdapter() {
        this(null);
    }

    @Autowired(required = false)
    public CrmFleetAclAdapter(CustomerFleetContextFacade customerFleetContextFacade) {
        this.customerFleetContextFacade = customerFleetContextFacade;
    }

    @Override
    public String getDriverFcmDeviceToken(VehicleId vehicleId) {
        if (vehicleId == null) {
            return null;
        }
        return mockDeviceTokens.getOrDefault(
                vehicleId.value(),
                "fcm_token_simulated_" + vehicleId.value().toString().substring(0, 8)
        );
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
}
