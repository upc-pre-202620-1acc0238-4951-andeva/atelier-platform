package com.andeva.atelier.platform.inventory.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.inventory.application.commandservices.InventoryItemCommandService;
import com.andeva.atelier.platform.inventory.application.queryservices.InventoryItemQueryService;
import com.andeva.atelier.platform.inventory.domain.model.commands.AddInventoryBatchCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.AllocateStockFifoCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.ReleaseStockAllocationCommand;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryBatchId;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryBatchesByItemIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.inventory.interfaces.rest.assemblers.BatchResourceAssembler;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.DispatchBatchFifoResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.ReceiveBatchResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.RestoreBatchResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.BatchResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.StockAllocationResource;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller managing inventory batches, physical stock traceability,
 * and deterministic FIFO dispatching and restoration.
 * Exposes canonical endpoints 16 through 19.
 *
 * @author Adiel Sanchez Santin
 */
@RestController
@RequestMapping({"/api/v1/inventory/batches", "/api/v1/batches"})
@Tag(name = "Batches & FIFO Allocations", description = "Endpoints for batch reception, physical stock traceability, and deterministic FIFO dispatching and restoration")
public class BatchesController {

    private final InventoryItemCommandService inventoryItemCommandService;
    private final InventoryItemQueryService inventoryItemQueryService;

    public BatchesController(
            InventoryItemCommandService inventoryItemCommandService,
            InventoryItemQueryService inventoryItemQueryService
    ) {
        this.inventoryItemCommandService = Objects.requireNonNull(inventoryItemCommandService, "inventoryItemCommandService cannot be null");
        this.inventoryItemQueryService = Objects.requireNonNull(inventoryItemQueryService, "inventoryItemQueryService cannot be null");
    }

    /**
     * Endpoint 16: List all active batches for an inventory item ordered by FIFO.
     */
    @GetMapping("/by-part/{partId}")
    @PreAuthorize("hasAuthority('inventory:batches:read') or hasRole('CHIEF_MECHANIC') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN') or isAuthenticated()")
    @Operation(summary = "List all active batches for an inventory item ordered by FIFO")
    public ResponseEntity<?> getBatchesByPart(@PathVariable UUID partId) {
        List<InventoryBatch> batches = inventoryItemQueryService.handle(new GetInventoryBatchesByItemIdQuery(InventoryItemId.of(partId)));
        List<BatchResource> resources = BatchResourceAssembler.toResourceList(batches);
        return ResponseEntity.ok(resources);
    }

    /**
     * Endpoint 17: Direct physical batch reception into inventory item.
     */
    @PostMapping("/receive")
    @PreAuthorize("hasAuthority('inventory:batches:receive') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Direct physical batch reception into inventory item")
    public ResponseEntity<?> receiveDirectBatch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReceiveBatchResource resource,
            UriComponentsBuilder ucb
    ) {
        AddInventoryBatchCommand command = new AddInventoryBatchCommand(
                InventoryItemId.of(resource.itemId()),
                resource.supplierId() != null ? SupplierId.of(resource.supplierId()) : null,
                resource.purchaseOrderId() != null ? PurchaseOrderId.of(resource.purchaseOrderId()) : null,
                resource.batchNumber(),
                Quantity.of(resource.quantity()),
                Money.of(resource.unitCost(), Currency.PEN),
                resource.arrivalDate() != null ? resource.arrivalDate() : Instant.now(),
                resource.receiptImageUrl() != null ? StorageUrl.of(resource.receiptImageUrl()) : null
        );

        Result<InventoryBatch, ApplicationError> result = inventoryItemCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        InventoryBatch batch = result.getOrThrow();
        BatchResource responseResource = BatchResourceAssembler.toResource(batch);
        URI location = ucb.path("/api/v1/inventory/batches/by-part/{partId}").buildAndExpand(resource.itemId()).toUri();
        return ResponseEntity.created(location).body(responseResource);
    }

    /**
     * Endpoint 18: Dispatch stock using strict First-In, First-Out (FIFO) algorithm.
     */
    @PostMapping("/dispatch")
    @PreAuthorize("hasAuthority('inventory:batches:dispatch_fifo') or hasAuthority('inventory:batches:manage') or hasRole('MECHANIC') or hasRole('CHIEF_MECHANIC') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Dispatch stock using strict First-In, First-Out (FIFO) algorithm")
    public ResponseEntity<?> dispatchFifoStock(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DispatchBatchFifoResource resource
    ) {
        AllocateStockFifoCommand command = new AllocateStockFifoCommand(
                InventoryItemId.of(resource.itemId()),
                Quantity.of(resource.quantity()),
                resource.workOrderId(),
                resource.taskId()
        );

        Result<StockAllocation, ApplicationError> result = inventoryItemCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        StockAllocation allocation = result.getOrThrow();
        StockAllocationResource responseResource = BatchResourceAssembler.toAllocationResource(allocation);
        return ResponseEntity.ok(responseResource);
    }

    /**
     * Endpoint 19: Restore unused allocated stock back to its originating batch.
     */
    @PostMapping("/restore")
    @PreAuthorize("hasAuthority('inventory:batches:restore') or hasAuthority('inventory:batches:manage') or hasRole('CHIEF_MECHANIC') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Restore unused allocated stock back to its originating batch")
    public ResponseEntity<?> restoreStock(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RestoreBatchResource resource
    ) {
        ReleaseStockAllocationCommand command = ReleaseStockAllocationCommand.ofBatch(
                InventoryItemId.of(resource.itemId()),
                InventoryBatchId.of(resource.batchId()),
                Quantity.of(resource.quantity()),
                resource.workOrderId(),
                resource.reason() != null ? resource.reason() : "Restoration from workshop"
        );

        Result<Void, ApplicationError> result = inventoryItemCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Stock restored successfully to originating batch",
                "batchId", resource.batchId(),
                "restoredQuantity", resource.quantity()
        ));
    }
}
