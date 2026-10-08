package com.andeva.atelier.platform.inventory.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.inventory.application.queryservices.InventoryItemQueryService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemsByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryValuationQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetLowStockItemsQuery;
import com.andeva.atelier.platform.inventory.interfaces.rest.assemblers.StockAlertResourceAssembler;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.ResolveAlertResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.StockAlertResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.StockEvaluationSummaryResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for monitoring reorder points, low stock thresholds,
 * and automated inventory replenishment alerts.
 * Exposes canonical endpoints 20 through 22.
 *
 * @author Adiel Sanchez Santin
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/inventory/alerts")
@Tag(name = "Stock Alerts", description = "Endpoints for monitoring reorder points, low stock thresholds, and inventory replenishment alerts")
public class StockAlertsController {

    private final InventoryItemQueryService inventoryItemQueryService;

    public StockAlertsController(InventoryItemQueryService inventoryItemQueryService) {
        this.inventoryItemQueryService = Objects.requireNonNull(inventoryItemQueryService, "inventoryItemQueryService cannot be null");
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails, UUID tenantHeader) {
        if (tenantHeader != null) {
            if (userDetails != null && userDetails.getTenantId() != null && !userDetails.getTenantId().equals(tenantHeader)) {
                throw new org.springframework.security.access.AccessDeniedException("Tenant ID in header does not match authenticated user context");
            }
            return TenantId.of(tenantHeader);
        }
        if (userDetails != null && userDetails.getTenantId() != null) {
            return TenantId.of(userDetails.getTenantId());
        }
        throw new org.springframework.security.access.AccessDeniedException("Active tenant context is required");
    }

    /**
     * Endpoint 20: List items that have reached or dropped below reorder threshold.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('inventory:alerts:read') or hasRole('CHIEF_MECHANIC') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN') or isAuthenticated()")
    @Operation(summary = "List items that have reached or dropped below reorder threshold")
    public ResponseEntity<?> getLowStockAlerts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        List<InventoryItem> lowStockItems = inventoryItemQueryService.handle(new GetLowStockItemsQuery(tenantId));
        List<StockAlertResource> resources = StockAlertResourceAssembler.toResourceList(lowStockItems);
        return ResponseEntity.ok(resources);
    }

    /**
     * Endpoint 21: Acknowledge and mark low stock alert as resolved or replenishment in progress.
     */
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('inventory:alerts:resolve') or hasAuthority('inventory:alerts:manage') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Acknowledge and mark low stock alert as resolved or replenishment in progress")
    public ResponseEntity<?> resolveAlert(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody(required = false) ResolveAlertResource resource
    ) {
        Optional<InventoryItem> itemOpt = inventoryItemQueryService.handle(new GetInventoryItemByIdQuery(InventoryItemId.of(id)));
        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        InventoryItem item = itemOpt.get();
        return ResponseEntity.ok(Map.of(
                "alertId", id,
                "itemId", item.getId().value(),
                "itemName", item.getName(),
                "sku", item.getSku().value(),
                "status", "RESOLVED",
                "notes", resource != null && resource.resolutionNotes() != null ? resource.resolutionNotes() : "Replenishment scheduled",
                "resolvedAt", Instant.now()
        ));
    }

    /**
     * Endpoint 22: Trigger system-wide stock reorder threshold evaluation for workshop.
     */
    @PostMapping("/evaluate")
    @PreAuthorize("hasAuthority('inventory:alerts:evaluate') or hasAuthority('inventory:alerts:manage') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Trigger system-wide stock reorder threshold evaluation for workshop")
    public ResponseEntity<?> evaluateStockThresholds(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);

        List<InventoryItem> allItems = inventoryItemQueryService.handle(new GetInventoryItemsByTenantIdQuery(tenantId));
        List<InventoryItem> lowStockItems = inventoryItemQueryService.handle(new GetLowStockItemsQuery(tenantId));
        Money totalValuation = inventoryItemQueryService.handle(new GetInventoryValuationQuery(tenantId));

        StockEvaluationSummaryResource summary = new StockEvaluationSummaryResource(
                allItems.size(),
                lowStockItems.size(),
                totalValuation.amount(),
                Instant.now()
        );

        return ResponseEntity.ok(summary);
    }
}
