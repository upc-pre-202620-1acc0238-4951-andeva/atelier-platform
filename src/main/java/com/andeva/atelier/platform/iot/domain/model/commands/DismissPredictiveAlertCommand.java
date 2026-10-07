package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain command to dismiss or discard a false positive or rejected predictive alert.
 *
 * @author Joel Huamani Estefanero
 */
public record DismissPredictiveAlertCommand(
        AlertId alertId
) implements Serializable {

    public DismissPredictiveAlertCommand {
        Objects.requireNonNull(alertId, "AlertId cannot be null");
    }
}
