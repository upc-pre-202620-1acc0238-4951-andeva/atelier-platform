package com.andeva.atelier.platform.invoicing.domain.model.queries;

import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Query requesting an electronic voucher by its official series and correlative number.
 *
 * @author Joel Huamani Estefanero
 */
public record GetVoucherBySerieAndNumberQuery(
        TenantId tenantId,
        VoucherSerie serie,
        VoucherNumber number
) implements Serializable {

    public GetVoucherBySerieAndNumberQuery {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(serie, "Voucher series cannot be null");
        Objects.requireNonNull(number, "Voucher number cannot be null");
    }
}
