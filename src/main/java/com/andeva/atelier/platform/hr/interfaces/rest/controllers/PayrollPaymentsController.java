package com.andeva.atelier.platform.hr.interfaces.rest.controllers;

import com.andeva.atelier.platform.hr.application.commandservices.PayrollPaymentCommandService;
import com.andeva.atelier.platform.hr.application.queryservices.PayrollPaymentQueryService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.commands.*;
import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import com.andeva.atelier.platform.hr.domain.model.queries.ExportSunatPlameRemQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetPayrollPaymentByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListPayrollPaymentsByPeriodQuery;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.AddPayrollBonusRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.AddPayrollDeductionRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.DisbursePayrollRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.GeneratePayrollRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.PayrollPaymentResource;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.PayrollPaymentSummaryResource;
import com.andeva.atelier.platform.hr.interfaces.rest.transform.PayrollPaymentResourceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/hr/payrolls")
@Tag(name = "Payroll Payments", description = "Endpoints for staff payroll settlements, deductions, bonuses and SUNAT PLAME export")
public class PayrollPaymentsController {

    private final PayrollPaymentCommandService payrollPaymentCommandService;
    private final PayrollPaymentQueryService payrollPaymentQueryService;

    public PayrollPaymentsController(
            PayrollPaymentCommandService payrollPaymentCommandService,
            PayrollPaymentQueryService payrollPaymentQueryService
    ) {
        this.payrollPaymentCommandService = Objects.requireNonNull(payrollPaymentCommandService, "payrollPaymentCommandService cannot be null");
        this.payrollPaymentQueryService = Objects.requireNonNull(payrollPaymentQueryService, "payrollPaymentQueryService cannot be null");
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

    private TenantMembershipId resolveMembershipId(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getUserId() != null) {
            return TenantMembershipId.of(userDetails.getUserId());
        }
        throw new org.springframework.security.access.AccessDeniedException("Authenticated user membership context is required");
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('iam:members:compensate') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Generate a new payroll draft settlement")
    public ResponseEntity<?> generatePayroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @Valid @RequestBody GeneratePayrollRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        GeneratePayrollCommand command = PayrollPaymentResourceAssembler.toCommand(tenantId, request);
        Result<PayrollPayment, ApplicationError> result = payrollPaymentCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        PayrollPayment created = result.getOrThrow();
        URI location = uriBuilder.path("/api/v1/hr/payrolls/{payrollId}")
                .buildAndExpand(created.getId().value()).toUri();
        return ResponseEntity.created(location).body(PayrollPaymentResourceAssembler.toResource(created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('iam:members:compensate') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "List all payroll payments with optional date and status filters")
    public ResponseEntity<Page<PayrollPaymentSummaryResource>> listPayrolls(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @RequestParam(value = "periodStart", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(value = "periodEnd", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @RequestParam(value = "status", required = false) String status,
            Pageable pageable
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        ListPayrollPaymentsByPeriodQuery query = new ListPayrollPaymentsByPeriodQuery(tenantId, periodStart, periodEnd, status);
        List<PayrollPayment> list = payrollPaymentQueryService.handle(query);
        List<PayrollPaymentSummaryResource> resources = PayrollPaymentResourceAssembler.toSummaryResourceList(list);

        int start = Math.min((int) pageable.getOffset(), resources.size());
        int end = Math.min((start + pageable.getPageSize()), resources.size());
        List<PayrollPaymentSummaryResource> pagedContent = resources.subList(start, end);
        Page<PayrollPaymentSummaryResource> page = new PageImpl<>(pagedContent, pageable, resources.size());

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{payrollId}")
    @PreAuthorize("hasAuthority('iam:members:compensate') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Get detailed payroll payment by ID")
    public ResponseEntity<?> getPayrollById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID payrollId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        Optional<PayrollPayment> paymentOpt = payrollPaymentQueryService.handle(
                new GetPayrollPaymentByIdQuery(tenantId, PayrollPaymentId.of(payrollId))
        );

        if (paymentOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("No se encontró la liquidación de nómina")
            );
        }

        return ResponseEntity.ok(PayrollPaymentResourceAssembler.toResource(paymentOpt.get()));
    }

    @PostMapping("/{payrollId}/deductions")
    @PreAuthorize("hasAuthority('iam:members:compensate') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Add deduction or withholding to payroll draft")
    public ResponseEntity<?> addPayrollDeduction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID payrollId,
            @Valid @RequestBody AddPayrollDeductionRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        AddPayrollDeductionCommand command = PayrollPaymentResourceAssembler.toCommand(tenantId, PayrollPaymentId.of(payrollId), request);
        Result<PayrollPayment, ApplicationError> result = payrollPaymentCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(PayrollPaymentResourceAssembler.toResource(result.getOrThrow()));
    }

    @PostMapping("/{payrollId}/bonuses")
    @PreAuthorize("hasAuthority('iam:members:compensate') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Add bonus or commission to payroll draft")
    public ResponseEntity<?> addPayrollBonus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID payrollId,
            @Valid @RequestBody AddPayrollBonusRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        AddPayrollBonusCommand command = PayrollPaymentResourceAssembler.toCommand(tenantId, PayrollPaymentId.of(payrollId), request);
        Result<PayrollPayment, ApplicationError> result = payrollPaymentCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(PayrollPaymentResourceAssembler.toResource(result.getOrThrow()));
    }

    @PostMapping("/{payrollId}/approve")
    @PreAuthorize("hasAuthority('iam:members:compensate') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Calculate and formally approve payroll settlement")
    public ResponseEntity<?> calculateAndApprovePayroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID payrollId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        TenantMembershipId approver = resolveMembershipId(userDetails);
        ApprovePayrollCommand command = new ApprovePayrollCommand(tenantId, PayrollPaymentId.of(payrollId), approver);
        Result<PayrollPayment, ApplicationError> result = payrollPaymentCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(PayrollPaymentResourceAssembler.toResource(result.getOrThrow()));
    }

    @PostMapping("/{payrollId}/disburse")
    @PreAuthorize("hasAuthority('iam:members:compensate') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Mark payroll as disbursed and paid with bank reference")
    public ResponseEntity<?> disbursePayroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID payrollId,
            @Valid @RequestBody DisbursePayrollRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        DisbursePayrollPaymentCommand command = PayrollPaymentResourceAssembler.toCommand(tenantId, PayrollPaymentId.of(payrollId), request);
        Result<PayrollPayment, ApplicationError> result = payrollPaymentCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(PayrollPaymentResourceAssembler.toResource(result.getOrThrow()));
    }

    @GetMapping("/export/sunat-rem")
    @PreAuthorize("hasAuthority('hr:plame:export') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Export SUNAT PLAME structured .rem text file (Formato 0601)")
    public ResponseEntity<?> exportSunatRem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @RequestParam("period") String period,
            @RequestParam(value = "branchId", required = false) UUID branchId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        BranchId branch = branchId != null ? BranchId.of(branchId) : null;
        ExportSunatPlameRemQuery query = new ExportSunatPlameRemQuery(tenantId, period, branch);

        try {
            String remContent = payrollPaymentQueryService.handle(query);
            byte[] bytes = remContent.getBytes(StandardCharsets.ISO_8859_1);
            String cleanPeriod = period.replace("-", "");
            String filename = String.format("0601%s.rem", cleanPeriod);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/plain;charset=ISO-8859-1"))
                    .body(bytes);
        } catch (Exception ex) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.badRequest(ex.getMessage())
            );
        }
    }
}
