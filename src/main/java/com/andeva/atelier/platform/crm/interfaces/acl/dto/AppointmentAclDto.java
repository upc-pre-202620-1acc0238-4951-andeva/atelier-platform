package com.andeva.atelier.platform.crm.interfaces.acl.dto;

import java.util.UUID;

/**
 * Immutable DTO exposing appointment scheduling metadata across bounded contexts.
 *
 * @param id                       Identifier of the appointment
 * @param tenantId                 Identifier of the workshop tenant
 * @param branchId                 Identifier of the branch facility
 * @param customerId               Identifier of the customer
 * @param vehicleId                Identifier of the vehicle
 * @param scheduledAt              Scheduled timestamp in ISO-8601 string representation
 * @param estimatedDurationMinutes Estimated duration in minutes
 * @param status                   Operational status of the appointment
 * @author Adiel Sanchez Santin
 */
public record AppointmentAclDto(
        UUID id,
        UUID tenantId,
        UUID branchId,
        UUID customerId,
        UUID vehicleId,
        String scheduledAt,
        int estimatedDurationMinutes,
        String status
) {}
