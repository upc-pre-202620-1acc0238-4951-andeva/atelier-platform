package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain command to acknowledge receipt of a predictive maintenance alert.
 *
 * @author Joel Huamani Estefanero
 */
public record AcknowledgePredictiveAlertCommand(
        AlertId alertId
) implements Serializable {

    public AcknowledgePredictiveAlertCommand {
        Objects.requireNonNull(alertId, "AlertId cannot be null");
    }
}
