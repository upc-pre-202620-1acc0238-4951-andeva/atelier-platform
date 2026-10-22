package com.andeva.atelier.platform.operations.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.operations.application.ledgerservices.WorkOrderPartLedgerService;
import com.andeva.atelier.platform.operations.application.ledgerservices.WorkOrderPartLedgerService.Profitability;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderPartLedgerAclService.PartMovement;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.PartMovementResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.PartProfitabilityResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderProfitabilityResource;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/work-orders")
@Tag(name = "Work Order Parts Ledger", description = "Cost of sales, gross profit and part returns of a work order")
public class WorkOrderPartLedgerController {

    private final WorkOrderPartLedgerService ledgerService;

    public WorkOrderPartLedgerController(WorkOrderPartLedgerService ledgerService) {
        this.ledgerService = Objects.requireNonNull(ledgerService, "ledgerService cannot be null");
    }

    @GetMapping("/{workOrderId}/profitability")
    @PreAuthorize("hasAnyAuthority('inventory:batches:read', 'invoicing:cashflow:export_pdf')")
    @Operation(summary = "Parts revenue against their FIFO cost of sales and gross profit of the work order (US35)")
    public ResponseEntity<?> getProfitability(@PathVariable UUID workOrderId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Result<Profitability, ApplicationError> result = ledgerService.profitability(tenantOf(userDetails), new WorkOrderId(workOrderId));
        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        Profitability p = result.getOrThrow();
        return ResponseEntity.ok(new WorkOrderProfitabilityResource(p.workOrderId(), p.laborRevenue(), p.partsRevenue(),
                p.partsCostOfSales(), p.partsGrossProfit(), p.subtotal(), p.currency(),
                p.lines().stream().map(l -> new PartProfitabilityResource(l.taskId(), l.productId(), l.quantity(),
                        l.unitPrice(), l.revenue(), l.costOfSales(), l.grossProfit())).toList()));
    }

    @GetMapping("/{workOrderId}/part-movements")
    @PreAuthorize("hasAnyAuthority('inventory:batches:read', 'inventory:batches:restore')")
    @Operation(summary = "Audit trail of the parts charged to the work order and returned to their batch (US33)")
    public ResponseEntity<?> getPartMovements(@PathVariable UUID workOrderId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Result<List<PartMovement>, ApplicationError> result = ledgerService.partMovements(tenantOf(userDetails), new WorkOrderId(workOrderId));
        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.ok(result.getOrThrow().stream()
                .map(m -> new PartMovementResource(m.allocationId(), m.itemId(), m.taskId(), m.batchId(), m.chargedQuantity(),
                        m.returnedQuantity(), m.unitCost(), m.currency(), m.chargedAt(), m.lastReturnedAt(),
                        m.lastReturnedBy(), m.lastReturnReason()))
                .toList());
    }

    private static TenantId tenantOf(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new AccessDeniedException("Active tenant context is required");
        }
        return TenantId.of(userDetails.getTenantId());
    }
}
