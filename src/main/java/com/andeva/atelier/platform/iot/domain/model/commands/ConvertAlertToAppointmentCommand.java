package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;

import java.util.Objects;

/**
 * Command to convert a predictive alert into a preventative workshop appointment.
 *
 * @param alertId the unique predictive alert identifier
 * @author Joel Huamani Estefanero
 */
public record ConvertAlertToAppointmentCommand(
        AlertId alertId
) {
    public ConvertAlertToAppointmentCommand {
        Objects.requireNonNull(alertId, "AlertId cannot be null");
    }
}
