package com.andeva.atelier.platform.invoicing.domain.exceptions;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

/**
 * Thrown when no active fiscal series configuration exists for a branch and voucher type.
 *
 * @author Joel Huamani Estefanero
 */
public class SeriesNotFoundException extends InvoicingDomainException {

    public static final String ERROR_CODE = "ERR_SERIES_NOT_FOUND";

    public SeriesNotFoundException(SeriesConfigurationId id) {
        super(ERROR_CODE, "Fiscal series configuration with ID '" + id.value() + "' was not found.");
    }

    public SeriesNotFoundException(BranchId branchId, VoucherType voucherType) {
        super(ERROR_CODE, "No active fiscal series found for branch '" + branchId.value() + "' and voucher type '" + voucherType.name() + "'.");
    }

    public SeriesNotFoundException(VoucherSerie serie) {
        super(ERROR_CODE, "Fiscal series '" + serie.value() + "' was not found.");
    }

    public SeriesNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
