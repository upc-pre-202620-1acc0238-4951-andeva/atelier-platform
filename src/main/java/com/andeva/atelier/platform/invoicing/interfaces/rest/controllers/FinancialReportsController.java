package com.andeva.atelier.platform.invoicing.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.invoicing.application.queryservices.CashFlowQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetCashFlowSummaryQuery;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers.CashFlowResourceAssembler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.CashFlowSummaryResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller governing workshop cash flow aggregation, interactive statement queries,
 * and formal vector PDF financial exports.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/invoicing/financial-reports")
@Tag(name = "Financial Reports", description = "Endpoints for cash flow financial statements and PDF exports")
public class FinancialReportsController {

    private final CashFlowQueryService cashFlowQueryService;
    private final CashFlowResourceAssembler cashFlowAssembler;

    public FinancialReportsController(
            CashFlowQueryService cashFlowQueryService,
            CashFlowResourceAssembler cashFlowAssembler
    ) {
        this.cashFlowQueryService = Objects.requireNonNull(cashFlowQueryService, "CashFlowQueryService cannot be null");
        this.cashFlowAssembler = Objects.requireNonNull(cashFlowAssembler, "CashFlowResourceAssembler cannot be null");
    }

    @Operation(summary = "Get interactive JSON workshop cash flow statement for date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cash flow statement calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/cash-flow")
    @PreAuthorize("hasAuthority('invoicing:cashflow:export_pdf') or hasAnyRole('ROLE_WORKSHOP_OWNER', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<CashFlowSummaryResource> getCashFlow(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateParam,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromParam,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateParam,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toParam,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        LocalDate from = startDateParam != null ? startDateParam : fromParam;
        LocalDate to = endDateParam != null ? endDateParam : toParam;

        LocalDate startDate = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate endDate = to != null ? to : LocalDate.now();

        CashFlowSummary summary = cashFlowQueryService.handle(new GetCashFlowSummaryQuery(tenantId, startDate, endDate));
        List<CashFlowMovement> movements = cashFlowQueryService.getCashFlowMovements(tenantId, startDate, endDate);

        return ResponseEntity.ok(cashFlowAssembler.toResource(summary, movements));
    }

    @Operation(summary = "Download formal A4 vector PDF workshop cash flow statement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF statement generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/cash-flow/pdf")
    @PreAuthorize("hasAuthority('invoicing:cashflow:export_pdf') or hasAnyRole('ROLE_WORKSHOP_OWNER', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<byte[]> downloadCashFlowPdf(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateParam,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromParam,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateParam,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toParam,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        LocalDate from = startDateParam != null ? startDateParam : fromParam;
        LocalDate to = endDateParam != null ? endDateParam : toParam;

        LocalDate startDate = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate endDate = to != null ? to : LocalDate.now();

        byte[] pdfBytes = cashFlowQueryService.exportCashFlowPdf(tenantId, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "cash-flow-" + startDate + "-to-" + endDate + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
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
