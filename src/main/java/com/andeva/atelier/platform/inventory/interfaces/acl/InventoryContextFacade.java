package com.andeva.atelier.platform.inventory.interfaces.acl;

import com.andeva.atelier.platform.inventory.interfaces.acl.dto.PartSummaryDto;
import com.andeva.atelier.platform.inventory.interfaces.acl.dto.StockAllocationDto;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Open Host Service (OHS) facade for the Inventory Bounded Context,
 * consumed by Workshop Operations, Billing, and other peer contexts.
 *
 * @author Joel Huamani Estefanero
 */
public interface InventoryContextFacade {

    Optional<PartSummaryDto> getPartSummary(UUID tenantId, UUID partId);

    Optional<BigDecimal> getPartCurrentStock(UUID tenantId, UUID partId);

    boolean hasAvailableStock(UUID tenantId, UUID partId, BigDecimal requestedQuantity);

    Result<StockAllocationDto, ApplicationError> reserveStockForWorkOrder(
            UUID tenantId,
            UUID partId,
            BigDecimal quantity,
            UUID workOrderId,
            UUID taskId
    );

    Result<Void, ApplicationError> releaseStockReservation(
            UUID tenantId,
            UUID partId,
            BigDecimal quantity,
            UUID workOrderId
    );

    BigDecimal calculateInventoryValuation(UUID tenantId);

    /**
     * FIFO cost of the units still charged to a work order, per task and batch (cost of sales, US35).
     */
    default java.util.List<com.andeva.atelier.platform.inventory.interfaces.acl.dto.PartCostDto> getPartCostsOfWorkOrder(UUID workOrderId) {
        return java.util.List.of();
    }

    /**
     * Every part charge of a work order with its returns: who, when and how many units went back (US33).
     */
    default java.util.List<com.andeva.atelier.platform.inventory.interfaces.acl.dto.PartMovementDto> getPartMovementsOfWorkOrder(UUID workOrderId) {
        return java.util.List.of();
    }

    default Optional<PartSummaryDto> fetchItemSummary(UUID itemId) {
        return getPartSummary(null, itemId);
    }

    default BigDecimal fetchItemCurrentStock(UUID itemId) {
        return getPartCurrentStock(null, itemId).orElse(BigDecimal.ZERO);
    }

    default Result<StockAllocationDto, ApplicationError> reserveStockForWorkOrder(UUID tenantId, UUID itemId, BigDecimal quantity) {
        return reserveStockForWorkOrder(tenantId, itemId, quantity, null, null);
    }

    default Result<Void, ApplicationError> releaseStockReservation(UUID tenantId, UUID itemId, BigDecimal quantity) {
        return releaseStockReservation(tenantId, itemId, quantity, null);
    }
}
