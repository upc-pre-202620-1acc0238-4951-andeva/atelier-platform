package com.andeva.atelier.platform.inventory.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.inventory.application.commandservices.PurchaseOrderCommandService;
import com.andeva.atelier.platform.inventory.application.queryservices.PurchaseOrderQueryService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.PurchaseOrder;
import com.andeva.atelier.platform.inventory.domain.model.commands.CancelPurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreatePurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.ReceivePurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetPurchaseOrderDetailQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetPurchaseOrdersByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.inventory.interfaces.rest.assemblers.PurchaseOrderResourceAssembler;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.CancelPurchaseOrderResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.CreatePurchaseOrderResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.ReceivePurchaseOrderResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.PurchaseOrderResource;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller managing replenishment purchase orders, supplier fulfillment,
 * goods reception, and acquisition costs.
 * Exposes canonical endpoints 11 through 15.
 *
 * @author Adiel Sanchez Santin
 */
@RestController
@RequestMapping({"/api/v1/inventory/purchase-orders", "/api/v1/purchase-orders"})
@Tag(name = "Purchase Orders", description = "Endpoints for managing replenishment orders, supplier fulfillment, goods reception, and acquisition costs")
public class PurchaseOrdersController {

    private final PurchaseOrderCommandService purchaseOrderCommandService;
    private final PurchaseOrderQueryService purchaseOrderQueryService;

    public PurchaseOrdersController(
            PurchaseOrderCommandService purchaseOrderCommandService,
            PurchaseOrderQueryService purchaseOrderQueryService
    ) {
        this.purchaseOrderCommandService = Objects.requireNonNull(purchaseOrderCommandService, "purchaseOrderCommandService cannot be null");
        this.purchaseOrderQueryService = Objects.requireNonNull(purchaseOrderQueryService, "purchaseOrderQueryService cannot be null");
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails, UUID tenantHeader) {
        if (tenantHeader != null) {
            return TenantId.of(tenantHeader);
        }
        if (userDetails != null && userDetails.getTenantId() != null) {
            return TenantId.of(userDetails.getTenantId());
        }
        return TenantId.of(UUID.randomUUID());
    }

    /**
     * Endpoint 11: List purchase orders filtered by status or supplier.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('inventory:purchase_orders:read') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN') or isAuthenticated()")
    @Operation(summary = "List purchase orders filtered by status or supplier")
    public ResponseEntity<?> getPurchaseOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID supplierId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        List<PurchaseOrder> orders = purchaseOrderQueryService.handle(new GetPurchaseOrdersByTenantIdQuery(tenantId));

        if (status != null && !status.isBlank()) {
            orders = orders.stream()
                    .filter(po -> po.getStatus().name().equalsIgnoreCase(status.trim()))
                    .toList();
        }

        if (supplierId != null) {
            SupplierId filterSupplier = SupplierId.of(supplierId);
            orders = orders.stream()
                    .filter(po -> po.getSupplierId().equals(filterSupplier))
                    .toList();
        }

        List<PurchaseOrderResource> resources = PurchaseOrderResourceAssembler.toResourceList(orders);
        return ResponseEntity.ok(resources);
    }

    /**
     * Endpoint 12: Draft new multi-item purchase order to supplier.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('inventory:purchase_orders:create') or hasAuthority('inventory:purchase_orders:manage') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Draft new multi-item purchase order to supplier")
    public ResponseEntity<?> createPurchaseOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @Valid @RequestBody CreatePurchaseOrderResource resource,
            UriComponentsBuilder ucb
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);

        CreatePurchaseOrderCommand command = PurchaseOrderResourceAssembler.toCommand(tenantId, resource);
        Result<PurchaseOrder, ApplicationError> result = purchaseOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        PurchaseOrder order = result.getOrThrow();
        PurchaseOrderResource responseResource = PurchaseOrderResourceAssembler.toResource(order);
        URI location = ucb.path("/api/v1/inventory/purchase-orders/{id}").buildAndExpand(order.getId().value()).toUri();
        return ResponseEntity.created(location).body(responseResource);
    }

    /**
     * Endpoint 13: Get detailed purchase order with lines and reception status.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:purchase_orders:read') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN') or isAuthenticated()")
    @Operation(summary = "Get detailed purchase order with lines and reception status")
    public ResponseEntity<?> getPurchaseOrderById(@PathVariable UUID id) {
        Optional<PurchaseOrder> orderOpt = purchaseOrderQueryService.handle(new GetPurchaseOrderDetailQuery(PurchaseOrderId.of(id)));

        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(PurchaseOrderResourceAssembler.toResource(orderOpt.get()));
    }

    /**
     * Endpoint 14: Confirm technical goods reception, physical count, and invoice registration.
     */
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('inventory:purchase_orders:receive') or hasAuthority('inventory:purchase_orders:manage') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Confirm technical goods reception, physical count, and invoice registration")
    public ResponseEntity<?> receivePurchaseOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ReceivePurchaseOrderResource resource
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);

        String receiptUrl = resource != null && resource.receiptImageUrl() != null && !resource.receiptImageUrl().isBlank()
                ? resource.receiptImageUrl()
                : "https://storage.atelier.internal/receipts/" + id + ".pdf";

        String receiptNo = resource != null && resource.receiptNumber() != null && !resource.receiptNumber().isBlank()
                ? resource.receiptNumber()
                : "REC-" + id.toString().substring(0, 8);

        Instant receivedAt = resource != null && resource.receivedAt() != null
                ? resource.receivedAt()
                : Instant.now();

        ReceivePurchaseOrderCommand command = new ReceivePurchaseOrderCommand(
                tenantId,
                PurchaseOrderId.of(id),
                StorageUrl.of(receiptUrl),
                receiptNo,
                receivedAt
        );

        Result<PurchaseOrder, ApplicationError> result = purchaseOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(PurchaseOrderResourceAssembler.toResource(result.getOrThrow()));
    }

    /**
     * Endpoint 15: Cancel pending purchase order before delivery.
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('inventory:purchase_orders:cancel') or hasAuthority('inventory:purchase_orders:manage') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Cancel pending purchase order before delivery")
    public ResponseEntity<?> cancelPurchaseOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID id,
            @RequestBody(required = false) CancelPurchaseOrderResource resource
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        String reason = resource != null ? resource.reason() : "Cancelled by user";

        CancelPurchaseOrderCommand command = new CancelPurchaseOrderCommand(tenantId, PurchaseOrderId.of(id), reason);
        Result<Void, ApplicationError> result = purchaseOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        // Return updated purchase order
        Optional<PurchaseOrder> orderOpt = purchaseOrderQueryService.handle(new GetPurchaseOrderDetailQuery(PurchaseOrderId.of(id)));
        return orderOpt.map(order -> ResponseEntity.ok(PurchaseOrderResourceAssembler.toResource(order)))
                .orElseGet(() -> ResponseEntity.ok().build());
    }
}
