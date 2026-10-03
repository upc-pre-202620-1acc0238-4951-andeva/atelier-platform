package com.andeva.atelier.platform.billing.interfaces.rest.controllers;

import com.andeva.atelier.platform.billing.application.queryservices.SaasInvoiceQueryService;
import com.andeva.atelier.platform.billing.domain.exceptions.SaasInvoiceNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.queries.ListTenantInvoicesQuery;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.SaasInvoiceResource;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.SaasInvoiceSummaryResource;
import com.andeva.atelier.platform.billing.interfaces.rest.transform.SaasInvoiceResourceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller providing tenant invoice history and direct Stripe receipt/PDF downloads.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/billing/invoices")
@Tag(name = "SaaS Invoices", description = "Endpoints for viewing workshop SaaS billing invoices and receipts")
public class SaasInvoicesController {

    private final SaasInvoiceQueryService invoiceQueryService;
    private final SaasInvoiceResourceAssembler invoiceResourceAssembler;

    public SaasInvoicesController(
            SaasInvoiceQueryService invoiceQueryService,
            SaasInvoiceResourceAssembler invoiceResourceAssembler
    ) {
        this.invoiceQueryService = Objects.requireNonNull(invoiceQueryService, "SaasInvoiceQueryService cannot be null");
        this.invoiceResourceAssembler = Objects.requireNonNull(invoiceResourceAssembler, "SaasInvoiceResourceAssembler cannot be null");
    }

    /**
     * Lists all platform subscription invoices issued to the authenticated workshop.
     *
     * @param status optional status filter (PAID, OPEN, VOID, UNCOLLECTIBLE)
     * @param userDetails authenticated user context
     * @return list of SaaS invoice summaries
     */
    @Operation(summary = "List workshop billing invoices")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('billing:invoices:read') or hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_WORKSHOP_OWNER', 'ROLE_ACCOUNTANT')")
    public ResponseEntity<List<SaasInvoiceSummaryResource>> getTenantInvoices(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);

        List<SaasInvoice> invoices = invoiceQueryService.handle(new ListTenantInvoicesQuery(tenantId));

        if (status != null && !status.isBlank()) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.status().name().equalsIgnoreCase(status.trim()))
                    .toList();
        }

        return ResponseEntity.ok(invoiceResourceAssembler.toSummaryResourceList(invoices));
    }

    /**
     * Retrieves detailed information and Stripe URLs for an individual SaaS invoice.
     *
     * @param id internal SaaS invoice identifier
     * @param userDetails authenticated user context
     * @return detailed invoice resource
     */
    @Operation(summary = "Get invoice details by identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice details retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request / Invalid UUID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Tenant isolation breach)"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('billing:invoices:read') or hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_WORKSHOP_OWNER', 'ROLE_ACCOUNTANT')")
    public ResponseEntity<SaasInvoiceResource> getInvoiceById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);

        SaasInvoice invoice = invoiceQueryService.findById(new SaasInvoiceId(id))
                .orElseThrow(() -> new SaasInvoiceNotFoundException(new SaasInvoiceId(id)));

        if (!invoice.tenantId().equals(tenantId)) {
            throw new AccessDeniedException("Access denied: invoice does not belong to authenticated workshop");
        }

        return ResponseEntity.ok(invoiceResourceAssembler.toResource(invoice));
    }

    /**
     * Redirects to the hosted Stripe Invoice PDF document via HTTP 302 Found.
     *
     * @param id internal SaaS invoice identifier
     * @param userDetails authenticated user context
     * @return 302 redirect to the external PDF file
     */
    @Operation(summary = "Redirect to official Stripe invoice PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirecting to official invoice PDF"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Tenant isolation breach)"),
            @ApiResponse(responseCode = "404", description = "Invoice or PDF not found")
    })
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('billing:invoices:read') or hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_WORKSHOP_OWNER', 'ROLE_ACCOUNTANT')")
    public ResponseEntity<Void> redirectToInvoicePdf(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);

        SaasInvoice invoice = invoiceQueryService.findById(new SaasInvoiceId(id))
                .orElseThrow(() -> new SaasInvoiceNotFoundException(new SaasInvoiceId(id)));

        if (!invoice.tenantId().equals(tenantId)) {
            throw new AccessDeniedException("Access denied: invoice does not belong to authenticated workshop");
        }

        if (invoice.invoicePdfUrl() == null || invoice.invoicePdfUrl().isBlank()) {
            throw new SaasInvoiceNotFoundException("PDF link is not available for invoice: " + id);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(invoice.invoicePdfUrl()))
                .build();
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new IllegalArgumentException("Tenant context could not be resolved from authenticated principal");
        }
        return new TenantId(userDetails.getTenantId());
    }
}
