package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Command to convert a predictive alert into a preventative workshop appointment.
 *
 * @param alertId                  the unique predictive alert identifier
 * @param branchId                 optional branch selected by the staff member
 * @param scheduledAt              optional date/time agreed with the customer
 * @param estimatedDurationMinutes optional estimated duration in minutes
 * @param reason                   optional appointment reason/notes
 * @author Joel Huamani Estefanero
 */
public record ConvertAlertToAppointmentCommand(
        AlertId alertId,
        UUID branchId,
        Instant scheduledAt,
        Integer estimatedDurationMinutes,
        String reason
) {
    public ConvertAlertToAppointmentCommand {
        Objects.requireNonNull(alertId, "AlertId cannot be null");
    }

    public ConvertAlertToAppointmentCommand(AlertId alertId) {
        this(alertId, null, null, null, null);
    }
}
