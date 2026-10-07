package com.andeva.atelier.platform.invoicing.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.invoicing.application.commandservices.SeriesConfigurationCommandService;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.commands.ConfigureSeriesCommand;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.application.queryservices.SeriesConfigurationQueryService;
import com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers.SeriesConfigurationResourceAssembler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.ConfigureSeriesRequest;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.UpdateSeriesStatusRequest;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.SeriesConfigurationResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller governing authorized fiscal series configurations,
 * physical branch series parameterization, and active issuance status.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/invoicing/series-configurations")
@Tag(name = "Series Configurations", description = "Endpoints for managing SUNAT series and correlative parameterizations")
public class SeriesConfigurationController {

    private final SeriesConfigurationCommandService seriesCommandService;
    private final SeriesConfigurationQueryService seriesQueryService;
    private final SeriesConfigurationResourceAssembler seriesAssembler;

    public SeriesConfigurationController(
            SeriesConfigurationCommandService seriesCommandService,
            SeriesConfigurationQueryService seriesQueryService,
            SeriesConfigurationResourceAssembler seriesAssembler
    ) {
        this.seriesCommandService = Objects.requireNonNull(seriesCommandService, "SeriesConfigurationCommandService cannot be null");
        this.seriesQueryService = Objects.requireNonNull(seriesQueryService, "SeriesConfigurationQueryService cannot be null");
        this.seriesAssembler = Objects.requireNonNull(seriesAssembler, "SeriesConfigurationResourceAssembler cannot be null");
    }

    @Operation(summary = "Configure a new authorized fiscal series for a branch")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Series configured successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid prefix"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Series already registered")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('invoicing:fiscal_config:manage') or hasAnyRole('ROLE_WORKSHOP_OWNER', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<SeriesConfigurationResource> configureSeries(
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ConfigureSeriesRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        VoucherType type = "01".equals(request.voucherType()) ? VoucherType.FACTURA :
                "03".equals(request.voucherType()) ? VoucherType.BOLETA :
                        "07".equals(request.voucherType()) ? VoucherType.NOTA_CREDITO :
                                VoucherType.valueOf(request.voucherType().toUpperCase().trim());

        ConfigureSeriesCommand command = new ConfigureSeriesCommand(
                tenantId,
                BranchId.of(request.branchId()),
                type,
                VoucherSerie.of(request.serie()),
                request.initialCorrelative()
        );

        SeriesConfiguration series = seriesCommandService.handle(command);
        SeriesConfigurationResource resource = seriesAssembler.toResource(series);

        return ResponseEntity.created(URI.create("/api/v1/invoicing/series-configurations/" + series.getId().value()))
                .body(resource);
    }

    @Operation(summary = "List all registered fiscal series for a workshop branch")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Series listed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyAuthority('invoicing:fiscal_config:manage', 'invoicing:invoices:read') or hasAnyRole('ROLE_CASHIER', 'ROLE_WORKSHOP_ADMINISTRATOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<List<SeriesConfigurationResource>> getSeriesByBranch(
            @PathVariable UUID branchId,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        List<SeriesConfiguration> seriesList = seriesQueryService.getSeriesByBranch(tenantId, BranchId.of(branchId));
        return ResponseEntity.ok(seriesAssembler.toResourceList(seriesList));
    }

    @Operation(summary = "Toggle active/inactive status of a fiscal series")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Series status updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Series not found")
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('invoicing:fiscal_config:manage') or hasAnyRole('ROLE_WORKSHOP_OWNER', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<Void> updateSeriesStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSeriesStatusRequest request,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        if (Boolean.TRUE.equals(request.active())) {
            seriesCommandService.activateSeries(SeriesConfigurationId.of(id), tenantId);
        } else {
            seriesCommandService.deactivateSeries(SeriesConfigurationId.of(id), tenantId);
        }

        return ResponseEntity.ok().build();
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails, UUID headerTenantId) {
        if (userDetails != null && userDetails.getTenantId() != null) {
            if (headerTenantId != null && !userDetails.getTenantId().equals(headerTenantId)) {
                throw new AccessDeniedException("Tenant ID in header does not match authenticated user context");
            }
            return TenantId.of(userDetails.getTenantId());
        }
        if (headerTenantId != null) {
            return TenantId.of(headerTenantId);
        }
        throw new AccessDeniedException("Active tenant context is required");
    }
}
