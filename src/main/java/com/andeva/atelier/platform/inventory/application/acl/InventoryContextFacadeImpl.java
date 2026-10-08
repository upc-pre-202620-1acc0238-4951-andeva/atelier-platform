package com.andeva.atelier.platform.inventory.application.acl;

import com.andeva.atelier.platform.inventory.application.commandservices.InventoryItemCommandService;
import com.andeva.atelier.platform.inventory.application.queryservices.InventoryItemQueryService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.commands.AllocateStockFifoCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.ReleaseStockAllocationCommand;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryValuationQuery;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;
import com.andeva.atelier.platform.inventory.interfaces.acl.InventoryContextFacade;
import com.andeva.atelier.platform.inventory.interfaces.acl.dto.PartSummaryDto;
import com.andeva.atelier.platform.inventory.interfaces.acl.dto.StockAllocationDto;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
/**
 * @author Joel Huamani Estefanero
 */
public class InventoryContextFacadeImpl implements InventoryContextFacade {

    private final InventoryItemCommandService itemCommandService;
    private final InventoryItemQueryService itemQueryService;

    public InventoryContextFacadeImpl(
            InventoryItemCommandService itemCommandService,
            InventoryItemQueryService itemQueryService
    ) {
        this.itemCommandService = Objects.requireNonNull(itemCommandService, "itemCommandService cannot be null");
        this.itemQueryService = Objects.requireNonNull(itemQueryService, "itemQueryService cannot be null");
    }

    @Override
    public Optional<PartSummaryDto> getPartSummary(UUID tenantId, UUID partId) {
        if (partId == null) {
            return Optional.empty();
        }

        return itemQueryService.handle(new GetInventoryItemByIdQuery(InventoryItemId.of(partId)))
                .filter(item -> tenantId == null || item.getTenantId().value().equals(tenantId))
                .map(item -> new PartSummaryDto(
                        item.getId().value(),
                        item.getName(),
                        item.getSku().value(),
                        item.getCategory().name(),
                        item.getBasePrice().amount(),
                        item.getTotalStock().value(),
                        item.getStatus().name()
                ));
    }

    @Override
    public Optional<BigDecimal> getPartCurrentStock(UUID tenantId, UUID partId) {
        return getPartSummary(tenantId, partId).map(PartSummaryDto::totalStock);
    }

    @Override
    public boolean hasAvailableStock(UUID tenantId, UUID partId, BigDecimal requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        return getPartCurrentStock(tenantId, partId)
                .map(stock -> stock.compareTo(requestedQuantity) >= 0)
                .orElse(false);
    }

    @Override
    public Result<StockAllocationDto, ApplicationError> reserveStockForWorkOrder(
            UUID tenantId,
            UUID partId,
            BigDecimal quantity,
            UUID workOrderId,
            UUID taskId
    ) {
        Objects.requireNonNull(partId, "partId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");

        AllocateStockFifoCommand command = new AllocateStockFifoCommand(
                InventoryItemId.of(partId),
                Quantity.of(quantity),
                workOrderId,
                taskId
        );

        Result<StockAllocation, ApplicationError> result = itemCommandService.handle(command);
        return result.map(allocation -> new StockAllocationDto(
                allocation.allocationId(),
                partId,
                allocation.allocatedQuantity().value(),
                allocation.totalCogs().amount()
        ));
    }

    @Override
    public Result<Void, ApplicationError> releaseStockReservation(
            UUID tenantId,
            UUID partId,
            BigDecimal quantity,
            UUID workOrderId
    ) {
        Objects.requireNonNull(partId, "partId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");

        ReleaseStockAllocationCommand command = ReleaseStockAllocationCommand.ofBatch(
                InventoryItemId.of(partId),
                null,
                Quantity.of(quantity),
                workOrderId,
                "Work order stock release via OHS facade"
        );

        return itemCommandService.handle(command);
    }

    @Override
    public BigDecimal calculateInventoryValuation(UUID tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Money valuation = itemQueryService.handle(new GetInventoryValuationQuery(TenantId.of(tenantId)));
        return valuation != null ? valuation.amount() : BigDecimal.ZERO;
    }
}
