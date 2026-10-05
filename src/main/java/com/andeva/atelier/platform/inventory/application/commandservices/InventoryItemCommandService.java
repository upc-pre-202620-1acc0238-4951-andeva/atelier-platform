package com.andeva.atelier.platform.inventory.application.commandservices;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.commands.AddInventoryBatchCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.AllocateStockFifoCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.DeactivateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.ReleaseStockAllocationCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.UpdateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface InventoryItemCommandService {

    Result<InventoryItem, ApplicationError> handle(CreateInventoryItemCommand command);

    Result<InventoryBatch, ApplicationError> handle(AddInventoryBatchCommand command);

    Result<StockAllocation, ApplicationError> handle(AllocateStockFifoCommand command);

    Result<Void, ApplicationError> handle(ReleaseStockAllocationCommand command);

    Result<InventoryItem, ApplicationError> handle(UpdateInventoryItemCommand command);

    Result<Void, ApplicationError> handle(DeactivateInventoryItemCommand command);
}
