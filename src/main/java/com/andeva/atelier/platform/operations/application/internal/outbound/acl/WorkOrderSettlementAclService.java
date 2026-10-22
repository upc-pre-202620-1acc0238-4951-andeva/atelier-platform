package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.util.UUID;

/**
 * Outbound port to Invoicing: whether the electronic vouchers of a work order are fully paid.
 */
public interface WorkOrderSettlementAclService {

    /**
     * @return true when the order has at least one valid voucher and every valid voucher is fully paid
     */
    boolean isSettled(UUID workOrderId);
}
