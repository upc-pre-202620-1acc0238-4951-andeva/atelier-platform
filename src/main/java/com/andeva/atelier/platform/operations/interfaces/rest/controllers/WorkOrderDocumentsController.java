package com.andeva.atelier.platform.operations.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.application.documentservices.WorkOrderDocumentService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.CancelWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.StartWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.CancelWorkOrderResource;
import com.andeva.atelier.platform.operations.interfaces.rest.transform.WorkOrderResourceAssembler;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Customer-facing documents and the quotation decision of a work order (US24, US25, US39).
 */
@RestController
@RequestMapping("/api/v1/work-orders")
@Tag(name = "Work Order Documents", description = "Quotation PDF, quotation decision and vehicle exit pass")
public class WorkOrderDocumentsController {

    private final WorkOrderDocumentService documentService;
    private final WorkOrderCommandService workOrderCommandService;

    public WorkOrderDocumentsController(WorkOrderDocumentService documentService, WorkOrderCommandService workOrderCommandService) {
        this.documentService = Objects.requireNonNull(documentService, "documentService cannot be null");
        this.workOrderCommandService = Objects.requireNonNull(workOrderCommandService, "workOrderCommandService cannot be null");
    }

    @GetMapping("/{workOrderId}/quotation/pdf")
    @PreAuthorize("hasAuthority('operations:quotations:read')")
    @Operation(summary = "Download the quotation (proforma) PDF with labor, parts, IGV and validity")
    public ResponseEntity<?> downloadQuotation(@PathVariable UUID workOrderId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return pdf(documentService.quotationPdf(tenantOf(userDetails), new WorkOrderId(workOrderId)), "proforma-" + workOrderId + ".pdf");
    }

    @PostMapping("/{workOrderId}/quotation/send")
    @PreAuthorize("hasAuthority('operations:quotations:send')")
    @Operation(summary = "Email the quotation PDF to the customer's contact email")
    public ResponseEntity<?> sendQuotation(@PathVariable UUID workOrderId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Result<String, ApplicationError> result = documentService.sendQuotation(tenantOf(userDetails), new WorkOrderId(workOrderId));
        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.ok(Map.of("message", "Quotation sent", "recipient", result.getOrThrow()));
    }

    @PostMapping("/{workOrderId}/quotation/approve")
    @PreAuthorize("hasAuthority('operations:quotations:approve')")
    @Operation(summary = "Record the customer's acceptance: the work order starts (IN_PROGRESS)")
    public ResponseEntity<?> approveQuotation(@PathVariable UUID workOrderId) {
        return order(workOrderCommandService.handle(new StartWorkOrderCommand(new WorkOrderId(workOrderId))));
    }

    @PostMapping("/{workOrderId}/quotation/reject")
    @PreAuthorize("hasAuthority('operations:quotations:approve')")
    @Operation(summary = "Record the customer's refusal: the work order is canceled and its materials released")
    public ResponseEntity<?> rejectQuotation(@PathVariable UUID workOrderId, @Valid @RequestBody CancelWorkOrderResource resource) {
        return order(workOrderCommandService.handle(new CancelWorkOrderCommand(new WorkOrderId(workOrderId),
                "Presupuesto rechazado por el cliente: " + resource.reason())));
    }

    @PostMapping("/{workOrderId}/proposals/{proposalId}/notify")
    @PreAuthorize("hasAuthority('operations:proposals:notify')")
    @Operation(summary = "Email a pending defect proposal with its photo evidence to the customer")
    public ResponseEntity<?> notifyProposal(@PathVariable UUID workOrderId, @PathVariable UUID proposalId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Result<String, ApplicationError> result = documentService.notifyProposal(tenantOf(userDetails), new WorkOrderId(workOrderId), proposalId);
        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.ok(Map.of("message", "Proposal sent", "recipient", result.getOrThrow()));
    }

    @GetMapping("/{workOrderId}/exit-pass/pdf")
    @PreAuthorize("hasAuthority('invoicing:exit_passes:issue')")
    @Operation(summary = "Issue the vehicle exit pass (only for paid orders)")
    public ResponseEntity<?> downloadExitPass(@PathVariable UUID workOrderId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return pdf(documentService.exitPassPdf(tenantOf(userDetails), new WorkOrderId(workOrderId)), "pase-salida-" + workOrderId + ".pdf");
    }

    private static TenantId tenantOf(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new AccessDeniedException("Active tenant context is required");
        }
        return TenantId.of(userDetails.getTenantId());
    }

    private static ResponseEntity<?> pdf(Result<byte[], ApplicationError> result, String fileName) {
        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(result.getOrThrow());
    }

    private static ResponseEntity<?> order(Result<WorkOrder, ApplicationError> result) {
        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.ok(WorkOrderResourceAssembler.toResourceFromEntity(result.getOrThrow()));
    }
}
