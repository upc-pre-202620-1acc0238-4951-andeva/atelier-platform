package com.andeva.atelier.platform.invoicing.domain.model.commands;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Command requesting cancellation or voiding of an electronic voucher (Comunicación de Baja).
 *
 * @author Joel Huamani Estefanero
 */
public record VoidElectronicVoucherCommand(
        com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId tenantId,
        VoucherId voucherId,
        String reason
) implements Serializable {

    public VoidElectronicVoucherCommand(VoucherId voucherId, String reason) {
        this(null, voucherId, reason);
    }

    public VoidElectronicVoucherCommand {
        Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
        Objects.requireNonNull(reason, "Void reason cannot be null");
        reason = reason.trim();
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Void reason cannot be blank");
        }
    }
}
