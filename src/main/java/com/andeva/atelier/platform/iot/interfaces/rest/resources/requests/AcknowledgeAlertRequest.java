package com.andeva.atelier.platform.iot.interfaces.rest.resources.requests;

import java.io.Serializable;

/**
 * Request DTO for acknowledging receipt and technical review of a predictive maintenance alert.
 *
 * @author Joel Huamani Estefanero
 */
public record AcknowledgeAlertRequest(
        String acknowledgedBy
) implements Serializable {
}
