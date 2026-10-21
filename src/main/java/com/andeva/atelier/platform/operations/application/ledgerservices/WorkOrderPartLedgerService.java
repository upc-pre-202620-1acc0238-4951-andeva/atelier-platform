package com.andeva.atelier.platform.operations.application.ledgerservices;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderPartLedgerAclService.PartMovement;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Internal accounting of the parts of a work order: sale price against FIFO cost (US35) and the trail of
 * charges and returns (US33).
 */
public interface WorkOrderPartLedgerService {

    Result<Profitability, ApplicationError> profitability(TenantId tenantId, WorkOrderId workOrderId);

    Result<List<PartMovement>, ApplicationError> partMovements(TenantId tenantId, WorkOrderId workOrderId);

    /**
     * @param partsRevenue      sale value of the parts charged (catalog price fixed at charge time)
     * @param partsCostOfSales  FIFO acquisition cost of those parts
     * @param partsGrossProfit  revenue minus cost of the parts
     * @param laborRevenue      price of the tasks (labor cost is not tracked)
     */
    record Profitability(UUID workOrderId, BigDecimal laborRevenue, BigDecimal partsRevenue, BigDecimal partsCostOfSales,
                         BigDecimal partsGrossProfit, BigDecimal subtotal, String currency, List<PartLine> lines) {
    }

    record PartLine(UUID taskId, UUID productId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal revenue,
                    BigDecimal costOfSales, BigDecimal grossProfit) {
    }
}
