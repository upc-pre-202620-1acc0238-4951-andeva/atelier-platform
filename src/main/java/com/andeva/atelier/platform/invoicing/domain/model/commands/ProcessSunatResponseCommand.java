package com.andeva.atelier.platform.invoicing.domain.model.commands;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;

import java.io.Serializable;
import java.util.Objects;

/**
 * Command requesting ingestion of telematics fiscal validation results from SUNAT / PSE.
 *
 * @author Joel Huamani Estefanero
 */
public record ProcessSunatResponseCommand(
        VoucherId voucherId,
        boolean accepted,
        String responseCode,
        String description,
        String digitalSignatureHash,
        DigitalReceiptUrls urls
) implements Serializable {

    public ProcessSunatResponseCommand {
        Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
        Objects.requireNonNull(responseCode, "Response code cannot be null");
    }
}
