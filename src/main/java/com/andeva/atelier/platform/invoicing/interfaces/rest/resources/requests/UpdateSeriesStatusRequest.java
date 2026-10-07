package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Request payload for toggling the active status of a fiscal series.
 *
 * @author Joel Huamani Estefanero
 */
public record UpdateSeriesStatusRequest(
        @NotNull(message = "{error.series.active.required}")
        Boolean active
) implements Serializable {
}
