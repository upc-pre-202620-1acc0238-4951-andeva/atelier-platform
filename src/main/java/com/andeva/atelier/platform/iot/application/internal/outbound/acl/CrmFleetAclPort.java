package com.andeva.atelier.platform.iot.application.internal.outbound.acl;

import com.andeva.atelier.platform.iot.domain.model.dto.ai.RecommendedServiceActionDto;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Anti-Corruption Layer port for vehicle master data, driver tokens, and appointment creation in CRM & Fleet.
 *
 * @author Joel Huamani Estefanero
 */
public interface CrmFleetAclPort {

    /**
     * Retrieves the active driver's Firebase Cloud Messaging token for push notifications.
     *
     * @param vehicleId target vehicle identifier
     * @return FCM registration token string or null if unavailable
     */
    String getDriverFcmDeviceToken(VehicleId vehicleId);

    /**
     * Checks whether the vehicle exists and is actively registered in CRM.
     *
     * @param vehicleId target vehicle identifier
     * @return true if vehicle is registered
     */
    boolean isVehicleRegistered(VehicleId vehicleId);

    /**
     * Converts a critical predictive alert into a preventative workshop appointment in CRM.
     *
     * @param alertId           predictive alert identifier
     * @param tenantId          workshop tenant identifier
     * @param vehicleId         target vehicle identifier
     * @param description       appointment summary and alert context
     * @param recommendedAction suggested workshop maintenance action
     * @return appointment UUID generated in CRM
     */
    UUID convertAlertToAppointment(
            AlertId alertId,
            TenantId tenantId,
            VehicleId vehicleId,
            String description,
            RecommendedServiceActionDto recommendedAction
    );

    /**
     * Retrieves vehicle administrative and technical metadata for PDF reports.
     *
     * @param vehicleId target vehicle identifier
     * @return optional VehicleMetadataDto
     */
    Optional<VehicleMetadataDto> getVehicleMetadata(VehicleId vehicleId);

    /**
     * Vehicle technical and ownership metadata for reporting.
     */
    record VehicleMetadataDto(
            String licensePlate,
            String vin,
            String brand,
            String model,
            int year,
            String ownerName
    ) {}
}
