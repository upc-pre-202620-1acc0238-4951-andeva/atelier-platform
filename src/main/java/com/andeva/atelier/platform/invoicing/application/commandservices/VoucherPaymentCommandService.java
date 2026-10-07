package com.andeva.atelier.platform.invoicing.application.commandservices;

import com.andeva.atelier.platform.invoicing.domain.model.commands.RegisterVoucherPaymentCommand;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;

/**
 * Command Service interface orchestrating financial amortization, cash settlement,
 * and balance tracking against electronic vouchers.
 *
 * @author Joel Huamani Estefanero
 */
public interface VoucherPaymentCommandService {

    /**
     * Registers a payment amortization against an electronic voucher.
     */
    VoucherPayment handle(RegisterVoucherPaymentCommand command);
}
