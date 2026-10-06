package com.andeva.atelier.platform.inventory.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.inventory.application.commandservices.SupplierCommandService;
import com.andeva.atelier.platform.inventory.application.queryservices.SupplierQueryService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.commands.RegisterSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.UpdateSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetSupplierByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetSuppliersByTenantIdQuery;
import com.andeva.atelier.platform.inventory.interfaces.rest.assemblers.SupplierResourceAssembler;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.RegisterSupplierResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.UpdateSupplierResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.SupplierResource;
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
 * REST controller for homologating, managing, and maintaining automotive parts suppliers.
 * Exposes canonical endpoints 7 through 10.
 *
 * @author Adiel Sanchez Santin
 */
@RestController
@RequestMapping({"/api/v1/inventory/suppliers", "/api/v1/suppliers"})
@Tag(name = "Suppliers", description = "Endpoints for homologating, managing, and rating automotive parts and consumables suppliers")
public class SuppliersController {

    private final SupplierCommandService supplierCommandService;
    private final SupplierQueryService supplierQueryService;

    public SuppliersController(
            SupplierCommandService supplierCommandService,
            SupplierQueryService supplierQueryService
    ) {
        this.supplierCommandService = Objects.requireNonNull(supplierCommandService, "supplierCommandService cannot be null");
        this.supplierQueryService = Objects.requireNonNull(supplierQueryService, "supplierQueryService cannot be null");
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
     * Endpoint 7: List registered and homologated automotive suppliers.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('inventory:suppliers:read') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN') or isAuthenticated()")
    @Operation(summary = "List registered and homologated automotive suppliers")
    public ResponseEntity<?> getSuppliers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        List<Supplier> suppliers = supplierQueryService.handle(new GetSuppliersByTenantIdQuery(tenantId));

        if (search != null && !search.isBlank()) {
            String lowerSearch = search.trim().toLowerCase();
            suppliers = suppliers.stream()
                    .filter(s -> s.getBusinessName().toLowerCase().contains(lowerSearch) || s.getTaxId().value().toLowerCase().contains(lowerSearch))
                    .toList();
        }

        if (status != null && !status.isBlank()) {
            boolean activeOnly = "active".equalsIgnoreCase(status.trim());
            suppliers = suppliers.stream()
                    .filter(s -> s.isActive() == activeOnly)
                    .toList();
        }

        List<SupplierResource> resources = SupplierResourceAssembler.toResourceList(suppliers);
        return ResponseEntity.ok(resources);
    }

    /**
     * Endpoint 8: Register new homologated supplier under tax identification.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('inventory:suppliers:manage') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Register new homologated supplier under RUC/tax identification")
    public ResponseEntity<?> registerSupplier(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @Valid @RequestBody RegisterSupplierResource resource,
            UriComponentsBuilder ucb
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);

        RegisterSupplierCommand command = SupplierResourceAssembler.toCommand(tenantId, resource);
        Result<Supplier, ApplicationError> result = supplierCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        Supplier supplier = result.getOrThrow();
        SupplierResource responseResource = SupplierResourceAssembler.toResource(supplier);
        URI location = ucb.path("/api/v1/inventory/suppliers/{id}").buildAndExpand(supplier.getId().value()).toUri();
        return ResponseEntity.created(location).body(responseResource);
    }

    /**
     * Endpoint 9: Get detailed profile of registered supplier.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:suppliers:read') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN') or isAuthenticated()")
    @Operation(summary = "Get detailed profile of registered supplier")
    public ResponseEntity<?> getSupplierById(@PathVariable UUID id) {
        Optional<Supplier> supplierOpt = supplierQueryService.handle(new GetSupplierByIdQuery(SupplierId.of(id)));

        if (supplierOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(SupplierResourceAssembler.toResource(supplierOpt.get()));
    }

    /**
     * Endpoint 10: Update supplier contact information and fiscal address.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:suppliers:manage') or hasRole('INVENTORY_MANAGER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Update supplier contact information and fiscal address")
    public ResponseEntity<?> updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplierResource resource
    ) {
        UpdateSupplierCommand command = SupplierResourceAssembler.toCommand(SupplierId.of(id), resource);
        Result<Supplier, ApplicationError> result = supplierCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(SupplierResourceAssembler.toResource(result.getOrThrow()));
    }
}
