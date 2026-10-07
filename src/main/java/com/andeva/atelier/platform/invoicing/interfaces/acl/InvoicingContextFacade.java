package com.andeva.atelier.platform.invoicing.interfaces.acl;

import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.GenerateVoucherFromWorkOrderCommandDto;
import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.VoucherGenerationResultDto;
import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.VoucherSummaryDto;

import java.util.List;
import java.util.UUID;

/**
 * Open Host Service (OHS) Inbound ACL Facade providing an anti-corruption boundary
 * for external bounded contexts such as Workshop Operations (MRO) to settle work orders
 * and verify fiscal billing status without direct coupling to SUNAT/UBL 2.1 intricacies.
 *
 * @author Joel Huamani Estefanero
 */
public interface InvoicingContextFacade {

    /**
     * Issues an electronic voucher from an MRO Work Order upon technical job completion.
     *
     * @param command Work order billing specifications
     * @return Generated voucher metadata and digital document links
     */
    VoucherGenerationResultDto generateVoucherFromWorkOrder(GenerateVoucherFromWorkOrderCommandDto command);

    /**
     * Retrieves all electronic vouchers issued against a specific work order.
     *
     * @param workOrderId Workshop work order identifier
     * @return List of voucher summaries
     */
    List<VoucherSummaryDto> getVouchersByWorkOrderId(UUID workOrderId);

    /**
     * Checks if a work order is fully invoiced and paid for gate-pass release authorization.
     *
     * @param workOrderId Workshop work order identifier
     * @return true if all balances are zero and paid
     */
    boolean isWorkOrderFullySettled(UUID workOrderId);
}
