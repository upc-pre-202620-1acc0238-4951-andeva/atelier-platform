package com.andeva.atelier.platform.inventory.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.inventory.application.commandservices.InventoryItemCommandService;
import com.andeva.atelier.platform.inventory.application.queryservices.InventoryItemQueryService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.UpdateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemBySkuQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemDetailQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemsByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetLowStockItemsQuery;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.interfaces.rest.assemblers.PartResourceAssembler;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.CreatePartResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.UpdatePartResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.PartDetailResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.PartResource;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller managing the automotive spare parts catalog, technical specifications,
 * base pricing, and physical stock thresholds.
 * Exposes canonical endpoints 1 through 6.
 *
 * @author Adiel Sanchez Santin
 */
@RestController
@RequestMapping({"/api/v1/inventory/parts", "/api/v1/inventory/items", "/api/v1/parts", "/api/v1/items"})
@Tag(name = "Parts Catalog", description = "Endpoints for managing automotive spare parts catalog, technical specifications, and physical stock thresholds")
public class PartsCatalogController {

    private final InventoryItemCommandService inventoryItemCommandService;
    private final InventoryItemQueryService inventoryItemQueryService;

    public PartsCatalogController(
            InventoryItemCommandService inventoryItemCommandService,
            InventoryItemQueryService inventoryItemQueryService
    ) {
        this.inventoryItemCommandService = Objects.requireNonNull(inventoryItemCommandService, "inventoryItemCommandService cannot be null");
        this.inventoryItemQueryService = Objects.requireNonNull(inventoryItemQueryService, "inventoryItemQueryService cannot be null");
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
     * Endpoint 1: List and filter parts catalog.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('inventory:parts:read') or hasRole('MECHANIC') or hasRole('CHIEF_MECHANIC') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN') or isAuthenticated()")
    @Operation(summary = "List and filter parts catalog")
    public ResponseEntity<?> getParts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") Boolean lowStockOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);

        List<InventoryItem> items;
        if (Boolean.TRUE.equals(lowStockOnly)) {
            items = inventoryItemQueryService.handle(new GetLowStockItemsQuery(tenantId));
        } else {
            items = inventoryItemQueryService.handle(new GetInventoryItemsByTenantIdQuery(tenantId));
        }

        // Apply in-memory filtering for optional parameters
        if (search != null && !search.isBlank()) {
            String lowerSearch = search.trim().toLowerCase();
            items = items.stream()
                    .filter(i -> i.getName().toLowerCase().contains(lowerSearch) || i.getSku().value().toLowerCase().contains(lowerSearch))
                    .toList();
        }

        if (category != null && !category.isBlank()) {
            items = items.stream()
                    .filter(i -> i.getCategory().name().equalsIgnoreCase(category.trim()))
                    .toList();
        }

        if (status != null && !status.isBlank()) {
            items = items.stream()
                    .filter(i -> i.getStatus().name().equalsIgnoreCase(status.trim()))
                    .toList();
        }

        List<PartResource> resources = items.stream()
                .map(PartResourceAssembler::toResource)
                .toList();

        if (size <= 0) {
            size = 20;
        }
        int fromIndex = Math.min(Math.max(0, page) * size, resources.size());
        int toIndex = Math.min(fromIndex + size, resources.size());
        List<PartResource> paged = resources.subList(fromIndex, toIndex);

        return ResponseEntity.ok(paged);
    }

    /**
     * Endpoint 2: Register new automotive part or consumable in workshop catalog.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('inventory:parts:manage') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Register new automotive part or consumable in workshop catalog")
    public ResponseEntity<?> createPart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @Valid @RequestBody CreatePartResource resource,
            UriComponentsBuilder ucb
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);

        CreateInventoryItemCommand command = PartResourceAssembler.toCommand(tenantId, resource);
        Result<InventoryItem, ApplicationError> result = inventoryItemCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        InventoryItem item = result.getOrThrow();
        PartResource responseResource = PartResourceAssembler.toResource(item);
        URI location = ucb.path("/api/v1/inventory/parts/{id}").buildAndExpand(item.getId().value()).toUri();
        return ResponseEntity.created(location).body(responseResource);
    }

    /**
     * Endpoint 3: Get detailed technical specifications and active batches for a part.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:parts:read') or hasRole('MECHANIC') or hasRole('CHIEF_MECHANIC') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN') or isAuthenticated()")
    @Operation(summary = "Get detailed technical specifications and active batches for a part")
    public ResponseEntity<?> getPartById(@PathVariable UUID id) {
        InventoryItemId itemId = InventoryItemId.of(id);
        Optional<InventoryItem> itemOpt = inventoryItemQueryService.handle(new GetInventoryItemDetailQuery(itemId));

        if (itemOpt.isEmpty()) {
            itemOpt = inventoryItemQueryService.handle(new GetInventoryItemByIdQuery(itemId));
        }

        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PartDetailResource detail = PartResourceAssembler.toDetailResource(itemOpt.get());
        return ResponseEntity.ok(detail);
    }

    /**
     * Endpoint 4: Update technical specifications, base price, or minimum stock.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:parts:manage') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Update technical specifications, base price, or minimum stock")
    public ResponseEntity<?> updatePart(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePartResource resource
    ) {
        UpdateInventoryItemCommand command = PartResourceAssembler.toCommand(InventoryItemId.of(id), resource);
        Result<InventoryItem, ApplicationError> result = inventoryItemCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(PartResourceAssembler.toResource(result.getOrThrow()));
    }

    /**
     * Endpoint 5: Lookup part by commercial SKU barcode.
     */
    @GetMapping("/by-sku/{sku}")
    @PreAuthorize("hasAuthority('inventory:parts:read') or hasRole('MECHANIC') or hasRole('CHIEF_MECHANIC') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN') or isAuthenticated()")
    @Operation(summary = "Lookup part by commercial SKU barcode")
    public ResponseEntity<?> getPartBySku(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable String sku
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        Optional<InventoryItem> itemOpt = inventoryItemQueryService.handle(new GetInventoryItemBySkuQuery(tenantId, Sku.of(sku)));

        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(PartResourceAssembler.toResource(itemOpt.get()));
    }

    /**
     * Endpoint 6: List parts belonging to a specific automotive category.
     */
    @GetMapping("/by-category/{category}")
    @PreAuthorize("hasAuthority('inventory:parts:read') or hasRole('MECHANIC') or hasRole('CHIEF_MECHANIC') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN') or isAuthenticated()")
    @Operation(summary = "List parts belonging to a specific automotive category")
    public ResponseEntity<?> getPartsByCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable String category
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);

        try {
            ItemCategory itemCategory = ItemCategory.valueOf(category.trim().toUpperCase());
            List<InventoryItem> items = inventoryItemQueryService.handle(new GetInventoryItemsByTenantIdQuery(tenantId, itemCategory));
            List<PartResource> resources = items.stream()
                    .map(PartResourceAssembler::toResource)
                    .toList();
            return ResponseEntity.ok(resources);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid item category: " + category);
        }
    }
}
