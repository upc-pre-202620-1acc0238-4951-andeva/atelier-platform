package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * Request payload for communicating the voiding or cancellation (Comunicación de Baja)
 * of an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record VoidVoucherRequest(
        @NotBlank(message = "{error.void.reason.required}")
        @Size(max = 250, message = "{error.void.reason.length}")
        String reason
) implements Serializable {
}
