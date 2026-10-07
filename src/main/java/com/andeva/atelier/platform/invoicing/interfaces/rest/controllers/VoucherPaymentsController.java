package com.andeva.atelier.platform.invoicing.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.invoicing.application.commandservices.VoucherPaymentCommandService;
import com.andeva.atelier.platform.invoicing.application.queryservices.ElectronicVoucherQueryService;
import com.andeva.atelier.platform.invoicing.application.queryservices.VoucherPaymentQueryService;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.commands.RegisterVoucherPaymentCommand;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherByIdQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherPaymentsQuery;
import com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers.VoucherPaymentResourceAssembler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.RegisterPaymentRequest;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.DailyReconciliationResource;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.VoucherPaymentResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST Controller managing payment registrations, customer cash collections, and branch daily reconciliations.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/invoicing/payments")
@Tag(name = "Invoicing Payments & Reconciliations", description = "Endpoints for registering payments and cash reconciliation")
public class VoucherPaymentsController {

    private final VoucherPaymentCommandService paymentCommandService;
    private final VoucherPaymentQueryService paymentQueryService;
    private final ElectronicVoucherQueryService voucherQueryService;
    private final VoucherPaymentResourceAssembler paymentAssembler;

    public VoucherPaymentsController(
            VoucherPaymentCommandService paymentCommandService,
            VoucherPaymentQueryService paymentQueryService,
            ElectronicVoucherQueryService voucherQueryService,
            VoucherPaymentResourceAssembler paymentAssembler
    ) {
        this.paymentCommandService = Objects.requireNonNull(paymentCommandService, "VoucherPaymentCommandService cannot be null");
        this.paymentQueryService = Objects.requireNonNull(paymentQueryService, "VoucherPaymentQueryService cannot be null");
        this.voucherQueryService = Objects.requireNonNull(voucherQueryService, "ElectronicVoucherQueryService cannot be null");
        this.paymentAssembler = Objects.requireNonNull(paymentAssembler, "VoucherPaymentResourceAssembler cannot be null");
    }

    @Operation(summary = "Register customer payment against an electronic voucher")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment parameters or negative amount"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Electronic voucher not found"),
            @ApiResponse(responseCode = "409", description = "Voucher already paid or payment exceeds balance")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('invoicing:payments:create') or hasAnyRole('ROLE_CASHIER', 'ROLE_WORKSHOP_ADMINISTRATOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<VoucherPaymentResource> registerPayment(
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RegisterPaymentRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        PaymentMethod method = PaymentMethod.valueOf(request.paymentMethod().toUpperCase().trim());
        Currency paymentCurrency = Currency.valueOf(request.currency().toUpperCase().trim());

        RegisterVoucherPaymentCommand command = new RegisterVoucherPaymentCommand(
                VoucherId.of(request.voucherId()),
                tenantId,
                BranchId.of(request.branchId()),
                Money.of(request.amount(), paymentCurrency),
                method,
                request.transactionReference()
        );

        VoucherPayment payment = paymentCommandService.handle(command);
        VoucherPaymentResource resource = paymentAssembler.toResource(payment);

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @Operation(summary = "Get full payment history for an electronic voucher")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Voucher not found")
    })
    @GetMapping("/voucher/{voucherId}")
    @PreAuthorize("hasAuthority('invoicing:invoices:read') or hasAnyRole('ROLE_CASHIER', 'ROLE_SERVICE_ADVISOR', 'ROLE_WORKSHOP_ADMINISTRATOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<List<VoucherPaymentResource>> getPaymentsByVoucher(
            @PathVariable UUID voucherId,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        // BOLA / Tenant ownership verification (CRIT-01)
        ElectronicVoucher voucher = voucherQueryService.handle(new GetVoucherByIdQuery(VoucherId.of(voucherId)))
                .orElseThrow(() -> new VoucherNotFoundException(VoucherId.of(voucherId)));
        if (!voucher.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Voucher does not belong to the authenticated workshop");
        }

        List<VoucherPayment> payments = paymentQueryService.handle(new GetVoucherPaymentsQuery(VoucherId.of(voucherId)));
        return ResponseEntity.ok(paymentAssembler.toResourceList(payments));
    }

    @Operation(summary = "Get daily cash reconciliation breakdown for a workshop branch")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Daily reconciliation calculated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/branch/{branchId}/daily")
    @PreAuthorize("hasAuthority('invoicing:invoices:read') or hasAnyRole('ROLE_CASHIER', 'ROLE_WORKSHOP_ADMINISTRATOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<DailyReconciliationResource> getDailyReconciliation(
            @PathVariable UUID branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<VoucherPayment> payments = paymentQueryService.getDailyPaymentsByBranch(BranchId.of(branchId), targetDate);

        // BOLA / Tenant ownership verification (CRIT-02)
        boolean anyCrossTenant = payments.stream().anyMatch(p -> !p.getTenantId().equals(tenantId));
        if (anyCrossTenant) {
            throw new AccessDeniedException("Branch payments do not belong to the authenticated workshop");
        }

        DailyReconciliationResource resource = paymentAssembler.toDailyReconciliationResource(branchId, targetDate, payments);
        return ResponseEntity.ok(resource);
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
