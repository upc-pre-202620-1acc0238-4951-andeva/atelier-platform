package com.andeva.atelier.platform.inventory.application.internal.commandservices;

import com.andeva.atelier.platform.inventory.application.commandservices.PurchaseOrderCommandService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.PurchaseOrder;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.commands.AddPurchaseOrderItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.CancelPurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreatePurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.IssuePurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.ReceivePurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.RemovePurchaseOrderItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.entities.PurchaseOrderItem;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.PurchaseOrderNumber;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryItemRepository;
import com.andeva.atelier.platform.inventory.domain.repositories.PurchaseOrderRepository;
import com.andeva.atelier.platform.inventory.domain.repositories.SupplierRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class PurchaseOrderCommandServiceImpl implements PurchaseOrderCommandService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryItemRepository itemRepository;

    public PurchaseOrderCommandServiceImpl(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            InventoryItemRepository itemRepository
    ) {
        this.purchaseOrderRepository = Objects.requireNonNull(purchaseOrderRepository, "purchaseOrderRepository cannot be null");
        this.supplierRepository = Objects.requireNonNull(supplierRepository, "supplierRepository cannot be null");
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository cannot be null");
    }

    @Override
    public Result<PurchaseOrder, ApplicationError> handle(CreatePurchaseOrderCommand command) {
        Objects.requireNonNull(command, "CreatePurchaseOrderCommand cannot be null");

        Optional<Supplier> supplierOpt = supplierRepository.findById(command.supplierId());
        if (supplierOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Supplier", command.supplierId().value()));
        }

        PurchaseOrderNumber orderNumber = command.orderNumber() != null ? command.orderNumber() : PurchaseOrderNumber.generate();
        if (purchaseOrderRepository.findByTenantIdAndOrderNumber(command.tenantId(), orderNumber).isPresent()) {
            return Result.failure(ApplicationError.conflict("Purchase order number '" + orderNumber.value() + "' already exists in this workshop"));
        }

        PurchaseOrder order = PurchaseOrder.create(
                command.tenantId(),
                command.supplierId(),
                command.branchId(),
                orderNumber
        );

        if (command.items() != null) {
            for (CreatePurchaseOrderCommand.CreatePurchaseOrderItemVo itemVo : command.items()) {
                Optional<InventoryItem> itemOpt = itemRepository.findById(itemVo.itemId());
                if (itemOpt.isEmpty()) {
                    return Result.failure(ApplicationError.notFound("InventoryItem", itemVo.itemId().value()));
                }
                PurchaseOrderItem line = PurchaseOrderItem.create(
                        order.getId(),
                        itemVo.itemId(),
                        itemVo.quantity(),
                        itemVo.unitCost()
                );
                order.addItem(line);
            }
        }

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return Result.success(saved);
    }

    @Override
    public Result<PurchaseOrder, ApplicationError> handle(AddPurchaseOrderItemCommand command) {
        Objects.requireNonNull(command, "AddPurchaseOrderItemCommand cannot be null");

        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(command.purchaseOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("PurchaseOrder", command.purchaseOrderId().value()));
        }

        Optional<InventoryItem> itemOpt = itemRepository.findById(command.itemId());
        if (itemOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("InventoryItem", command.itemId().value()));
        }

        PurchaseOrder order = orderOpt.get();
        try {
            PurchaseOrderItem line = PurchaseOrderItem.create(
                    order.getId(),
                    command.itemId(),
                    command.quantity(),
                    command.unitCost()
            );
            order.addItem(line);
            PurchaseOrder saved = purchaseOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<PurchaseOrder, ApplicationError> handle(RemovePurchaseOrderItemCommand command) {
        Objects.requireNonNull(command, "RemovePurchaseOrderItemCommand cannot be null");

        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(command.purchaseOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("PurchaseOrder", command.purchaseOrderId().value()));
        }

        PurchaseOrder order = orderOpt.get();
        try {
            order.removeItem(command.purchaseOrderItemId());
            PurchaseOrder saved = purchaseOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<PurchaseOrder, ApplicationError> handle(IssuePurchaseOrderCommand command) {
        Objects.requireNonNull(command, "IssuePurchaseOrderCommand cannot be null");

        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(command.purchaseOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("PurchaseOrder", command.purchaseOrderId().value()));
        }

        PurchaseOrder order = orderOpt.get();
        try {
            order.issue();
            PurchaseOrder saved = purchaseOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<PurchaseOrder, ApplicationError> handle(ReceivePurchaseOrderCommand command) {
        Objects.requireNonNull(command, "ReceivePurchaseOrderCommand cannot be null");

        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(command.purchaseOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("PurchaseOrder", command.purchaseOrderId().value()));
        }

        PurchaseOrder order = orderOpt.get();
        try {
            order.receive(command.receiptImageUrl(), command.receiptNumber(), command.receivedAt());

            // For each item line, generate a physical batch and add to InventoryItem
            for (PurchaseOrderItem line : order.getItems()) {
                Optional<InventoryItem> itemOpt = itemRepository.findById(line.getItemId());
                if (itemOpt.isPresent()) {
                    InventoryItem item = itemOpt.get();
                    String batchNumber = order.getOrderNumber().value() + "-L" + line.getId().value().toString().substring(0, 4);
                    InventoryBatch batch = InventoryBatch.create(
                            order.getTenantId(),
                            item.getId(),
                            order.getSupplierId(),
                            order.getId(),
                            batchNumber,
                            line.getQuantity(),
                            line.getUnitCost(),
                            command.receivedAt(),
                            command.receiptImageUrl()
                    );
                    item.addBatch(batch);
                    itemRepository.save(item);
                }
            }

            PurchaseOrder saved = purchaseOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<Void, ApplicationError> handle(CancelPurchaseOrderCommand command) {
        Objects.requireNonNull(command, "CancelPurchaseOrderCommand cannot be null");

        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(command.purchaseOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("PurchaseOrder", command.purchaseOrderId().value()));
        }

        PurchaseOrder order = orderOpt.get();
        try {
            order.cancel(command.reason());
            purchaseOrderRepository.save(order);
            return Result.success(null);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }
}
