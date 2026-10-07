package com.andeva.atelier.platform.iot.infrastructure.external.acl.crm;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.RecommendedServiceActionDto;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti-Corruption Layer adapter for communicating with Customer & Fleet Management (CRM).
 * Provides resilient fallbacks and simulated device token lookups while the CRM module
 * is being developed in parallel.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class CrmFleetAclAdapter implements CrmFleetAclPort {

    private static final Logger log = LoggerFactory.getLogger(CrmFleetAclAdapter.class);

    private final ConcurrentHashMap<UUID, String> mockDeviceTokens = new ConcurrentHashMap<>();

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
        return vehicleId != null;
    }

    @Override
    public UUID convertAlertToAppointment(
            AlertId alertId,
            TenantId tenantId,
            VehicleId vehicleId,
            String description,
            RecommendedServiceActionDto recommendedAction) {
        UUID appointmentId = UUID.randomUUID();
        log.info("Simulated CRM preventative appointment {} generated for vehicle {} from alert {}",
                appointmentId, vehicleId.value(), alertId.value());
        return appointmentId;
    }

    @Override
    public Optional<VehicleMetadataDto> getVehicleMetadata(VehicleId vehicleId) {
        if (vehicleId == null) {
            return Optional.empty();
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
