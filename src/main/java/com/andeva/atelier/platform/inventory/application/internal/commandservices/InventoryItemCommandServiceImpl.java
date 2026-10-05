package com.andeva.atelier.platform.inventory.application.internal.commandservices;

import com.andeva.atelier.platform.inventory.application.commandservices.InventoryItemCommandService;
import com.andeva.atelier.platform.inventory.domain.exceptions.InsufficientStockException;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.commands.AddInventoryBatchCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.AllocateStockFifoCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.DeactivateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.ReleaseStockAllocationCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.UpdateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryItemRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class InventoryItemCommandServiceImpl implements InventoryItemCommandService {

    private final InventoryItemRepository itemRepository;

    public InventoryItemCommandServiceImpl(InventoryItemRepository itemRepository) {
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository cannot be null");
    }

    @Override
    public Result<InventoryItem, ApplicationError> handle(CreateInventoryItemCommand command) {
        Objects.requireNonNull(command, "CreateInventoryItemCommand cannot be null");

        if (itemRepository.existsByTenantIdAndSku(command.tenantId(), command.sku())) {
            return Result.failure(ApplicationError.conflict("Inventory item with SKU '" + command.sku().value() + "' already exists in this workshop"));
        }

        InventoryItem item = InventoryItem.create(
                command.tenantId(),
                command.name(),
                command.sku(),
                command.category(),
                command.basePrice(),
                command.minimumStock(),
                command.unitOfMeasure()
        );

        InventoryItem saved = itemRepository.save(item);
        return Result.success(saved);
    }

    @Override
    public Result<InventoryBatch, ApplicationError> handle(AddInventoryBatchCommand command) {
        Objects.requireNonNull(command, "AddInventoryBatchCommand cannot be null");

        Optional<InventoryItem> itemOpt = itemRepository.findById(command.itemId());
        if (itemOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("InventoryItem", command.itemId().value()));
        }

        InventoryItem item = itemOpt.get();
        InventoryBatch batch = InventoryBatch.create(
                item.getTenantId(),
                command.itemId(),
                command.supplierId(),
                command.purchaseOrderId(),
                command.batchNumber(),
                command.quantity(),
                command.unitCost(),
                command.arrivalDate(),
                command.receiptImageUrl()
        );

        item.addBatch(batch);
        itemRepository.save(item);

        return Result.success(batch);
    }

    @Override
    public Result<StockAllocation, ApplicationError> handle(AllocateStockFifoCommand command) {
        Objects.requireNonNull(command, "AllocateStockFifoCommand cannot be null");

        Optional<InventoryItem> itemOpt = itemRepository.findById(command.itemId());
        if (itemOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("InventoryItem", command.itemId().value()));
        }

        InventoryItem item = itemOpt.get();
        try {
            StockAllocation allocation = item.allocateStockFifo(command.requestedQuantity());
            itemRepository.save(item);
            return Result.success(allocation);
        } catch (InsufficientStockException ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unprocessableEntity(ex.getMessage()));
        }
    }

    @Override
    public Result<Void, ApplicationError> handle(ReleaseStockAllocationCommand command) {
        Objects.requireNonNull(command, "ReleaseStockAllocationCommand cannot be null");

        Optional<InventoryItem> itemOpt = itemRepository.findById(command.itemId());
        if (itemOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("InventoryItem", command.itemId().value()));
        }

        InventoryItem item = itemOpt.get();
        try {
            if (command.allocation() != null) {
                item.releaseStockAllocation(command.allocation());
            } else if (command.batchId() != null && command.quantity() != null) {
                item.restoreBatchStock(command.batchId().value(), command.quantity());
            }
            itemRepository.save(item);
            return Result.success(null);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<InventoryItem, ApplicationError> handle(UpdateInventoryItemCommand command) {
        Objects.requireNonNull(command, "UpdateInventoryItemCommand cannot be null");

        Optional<InventoryItem> itemOpt = itemRepository.findById(command.itemId());
        if (itemOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("InventoryItem", command.itemId().value()));
        }

        InventoryItem item = itemOpt.get();
        item.updateDetails(
                command.name(),
                command.category(),
                command.basePrice(),
                command.minimumStock(),
                command.unitOfMeasure() != null ? command.unitOfMeasure() : "UNIT"
        );
        InventoryItem saved = itemRepository.save(item);
        return Result.success(saved);
    }

    @Override
    public Result<Void, ApplicationError> handle(DeactivateInventoryItemCommand command) {
        Objects.requireNonNull(command, "DeactivateInventoryItemCommand cannot be null");

        Optional<InventoryItem> itemOpt = itemRepository.findById(command.itemId());
        if (itemOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("InventoryItem", command.itemId().value()));
        }

        InventoryItem item = itemOpt.get();
        item.deactivate();
        itemRepository.save(item);
        return Result.success(null);
    }
}
