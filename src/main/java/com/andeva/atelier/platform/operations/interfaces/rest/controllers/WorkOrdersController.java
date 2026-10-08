package com.andeva.atelier.platform.operations.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkBayQueryService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.*;
import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetTaskProposalsByWorkOrderIdQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrdersByBranchIdQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrdersByTenantIdQuery;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.*;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.*;
import com.andeva.atelier.platform.operations.interfaces.rest.transform.*;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing workshop vehicle work orders, bay allocation, and lifecycle orchestration.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/work-orders")
@Tag(name = "Work Orders", description = "Endpoints for managing workshop vehicle work orders, bay allocation, and lifecycle orchestration")
public class WorkOrdersController {

    private final WorkOrderCommandService workOrderCommandService;
    private final WorkOrderQueryService workOrderQueryService;
    private final WorkBayQueryService workBayQueryService;

    public WorkOrdersController(
            WorkOrderCommandService workOrderCommandService,
            WorkOrderQueryService workOrderQueryService,
            WorkBayQueryService workBayQueryService) {
        this.workOrderCommandService = Objects.requireNonNull(workOrderCommandService, "workOrderCommandService cannot be null");
        this.workOrderQueryService = Objects.requireNonNull(workOrderQueryService, "workOrderQueryService cannot be null");
        this.workBayQueryService = Objects.requireNonNull(workBayQueryService, "workBayQueryService cannot be null");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Open formal work order upon technical vehicle reception")
    public ResponseEntity<?> createWorkOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateWorkOrderResource resource,
            UriComponentsBuilder ucb
    ) {
        UUID tenantId = userDetails != null && userDetails.getTenantId() != null
                ? userDetails.getTenantId()
                : UUID.randomUUID();

        CreateWorkOrderCommand command = CreateWorkOrderCommandFromResourceAssembler.toCommandFromResource(resource, tenantId);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        WorkOrder order = result.getOrThrow();
        WorkOrderResource responseResource = WorkOrderResourceAssembler.toResourceFromEntity(order);
        URI location = ucb.path("/api/v1/work-orders/{id}").buildAndExpand(order.getId().value()).toUri();
        return ResponseEntity.created(location).body(responseResource);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('operations:work-orders:read') or isAuthenticated()")
    @Operation(summary = "List and search work orders by branch or tenant")
    public ResponseEntity<?> getWorkOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) UUID branchId
    ) {
        UUID tenantId = userDetails != null && userDetails.getTenantId() != null
                ? userDetails.getTenantId()
                : UUID.randomUUID();

        List<WorkOrder> orders = branchId != null
                ? workOrderQueryService.handle(new GetWorkOrdersByBranchIdQuery(new TenantId(tenantId), new BranchId(branchId)))
                : workOrderQueryService.handle(new GetWorkOrdersByTenantIdQuery(new TenantId(tenantId)));

        List<WorkOrderSummaryResource> resources = orders.stream()
                .map(order -> {
                    String bayName = order.getCurrentBayId()
                            .flatMap(workBayQueryService::getById)
                            .map(b -> b.getName())
                            .orElse(null);
                    return WorkOrderResourceAssembler.toSummaryResourceFromEntity(order, bayName);
                })
                .toList();

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{workOrderId}")
    @PreAuthorize("hasAuthority('operations:work-orders:read') or isAuthenticated()")
    @Operation(summary = "Get exhaustive work order details with tasks, proposals, and images")
    public ResponseEntity<?> getWorkOrderById(@PathVariable UUID workOrderId) {
        return workOrderQueryService.handle(new GetWorkOrderByIdQuery(new WorkOrderId(workOrderId)))
                .map(order -> {
                    String bayName = order.getCurrentBayId()
                            .flatMap(workBayQueryService::getById)
                            .map(b -> b.getName())
                            .orElse(null);

                    List<WorkOrderTaskResource> taskResources = order.getTasks().stream()
                            .map(t -> WorkOrderTaskResourceAssembler.toResourceFromEntity(t, "Standard Service", null))
                            .collect(Collectors.toList());

                    List<TaskProposalResource> proposalResources = order.getProposals().stream()
                            .map(p -> TaskProposalResourceAssembler.toResourceFromEntity(p, "Suggested Service", null))
                            .collect(Collectors.toList());

                    return WorkOrderResourceAssembler.toDetailResourceFromEntity(order, bayName, taskResources, proposalResources);
                })
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{workOrderId}")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Update verified mileage or diagnostic summary")
    public ResponseEntity<?> updateWorkOrder(
            @PathVariable UUID workOrderId,
            @Valid @RequestBody UpdateWorkOrderResource resource
    ) {
        return workOrderQueryService.handle(new GetWorkOrderByIdQuery(new WorkOrderId(workOrderId)))
                .map(order -> {
                    order.updateDiagnosticAndMileage(
                            resource.diagnosticSummary() != null ? DiagnosticSummary.of(resource.diagnosticSummary()) : null,
                            resource.mileageIn() != null ? Mileage.of(resource.mileageIn()) : null
                    );
                    return ResponseEntity.ok(WorkOrderResourceAssembler.toResourceFromEntity(order));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{workOrderId}/bay")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Assign or relocate vehicle to physical work bay")
    public ResponseEntity<?> assignBay(
            @PathVariable UUID workOrderId,
            @Valid @RequestBody AssignWorkBayResource resource
    ) {
        AssignWorkBayCommand command = AssignWorkBayCommandFromResourceAssembler.toCommandFromResource(workOrderId, resource);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(WorkOrderResourceAssembler.toResourceFromEntity(result.getOrThrow()));
    }

    @DeleteMapping("/{workOrderId}/bay")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Release and deallocate current work bay")
    public ResponseEntity<?> releaseBay(@PathVariable UUID workOrderId) {
        ReleaseWorkBayCommand command = new ReleaseWorkBayCommand(new WorkOrderId(workOrderId));
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{workOrderId}/tasks")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Add authorized technical task to work order")
    public ResponseEntity<?> createTask(
            @PathVariable UUID workOrderId,
            @Valid @RequestBody CreateWorkOrderTaskResource resource,
            UriComponentsBuilder ucb
    ) {
        AddTaskToWorkOrderCommand command = CreateWorkOrderTaskCommandFromResourceAssembler.toCommandFromResource(workOrderId, resource);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        WorkOrder updated = result.getOrThrow();
        WorkOrderTask task = updated.getTasks().get(updated.getTasks().size() - 1);
        WorkOrderTaskResource taskRes = WorkOrderTaskResourceAssembler.toResourceFromEntity(task, "Service", null);

        URI location = ucb.path("/api/v1/tasks/{taskId}").buildAndExpand(task.getId().value()).toUri();
        return ResponseEntity.created(location).body(taskRes);
    }

    @PostMapping("/{workOrderId}/proposals")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('MECHANIC')")
    @Operation(summary = "Submit defect proposal or hidden flaw discovered during bay inspection")
    public ResponseEntity<?> submitProposal(
            @PathVariable UUID workOrderId,
            @Valid @RequestBody SubmitTaskProposalResource resource,
            UriComponentsBuilder ucb
    ) {
        SubmitTaskProposalCommand command = SubmitTaskProposalCommandFromResourceAssembler.toCommandFromResource(workOrderId, resource);
        Result<TaskProposal, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        TaskProposal proposal = result.getOrThrow();
        TaskProposalResource proposalRes = TaskProposalResourceAssembler.toResourceFromEntity(proposal, "Suggested Service", null);
        URI location = ucb.path("/api/v1/work-orders/{workOrderId}/proposals/{id}")
                .buildAndExpand(workOrderId, proposal.getId()).toUri();
        return ResponseEntity.created(location).body(proposalRes);
    }

    @GetMapping("/{workOrderId}/proposals")
    @PreAuthorize("hasAuthority('operations:work-orders:read') or isAuthenticated()")
    @Operation(summary = "List technical defect proposals for work order")
    public ResponseEntity<?> getProposals(@PathVariable UUID workOrderId) {
        List<TaskProposal> proposals = workOrderQueryService.handle(new GetTaskProposalsByWorkOrderIdQuery(new WorkOrderId(workOrderId)));
        List<TaskProposalResource> resources = proposals.stream()
                .map(p -> TaskProposalResourceAssembler.toResourceFromEntity(p, "Suggested Service", null))
                .toList();

        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{workOrderId}/proposals/{proposalId}/approve")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Approve proposal converting it to formal authorized task")
    public ResponseEntity<?> approveProposal(
            @PathVariable UUID workOrderId,
            @PathVariable UUID proposalId,
            @Valid @RequestBody ApproveTaskProposalResource resource
    ) {
        ApproveTaskProposalCommand command = ApproveTaskProposalCommandFromResourceAssembler.toCommandFromResource(workOrderId, proposalId, resource);
        Result<WorkOrderTask, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(WorkOrderTaskResourceAssembler.toResourceFromEntity(result.getOrThrow(), "Approved Service", null));
    }

    @PostMapping("/{workOrderId}/proposals/{proposalId}/reject")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Reject proposal per customer decision")
    public ResponseEntity<?> rejectProposal(
            @PathVariable UUID workOrderId,
            @PathVariable UUID proposalId,
            @Valid @RequestBody RejectTaskProposalResource resource
    ) {
        RejectTaskProposalCommand command = RejectTaskProposalCommandFromResourceAssembler.toCommandFromResource(workOrderId, proposalId, resource);
        Result<TaskProposal, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(TaskProposalResourceAssembler.toResourceFromEntity(result.getOrThrow(), "Rejected Service", null));
    }

    @PostMapping("/{workOrderId}/intake-images")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Attach reception intake photograph metadata from Firebase Storage")
    public ResponseEntity<?> attachIntakeImage(
            @PathVariable UUID workOrderId,
            @Valid @RequestBody AttachImageResource resource,
            UriComponentsBuilder ucb
    ) {
        AttachIntakeImageCommand command = AttachIntakeImageCommandFromResourceAssembler.toCommandFromResource(workOrderId, resource);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        WorkOrderImageResource imgResource = new WorkOrderImageResource(
                UUID.randomUUID(),
                workOrderId,
                resource.imageUrl(),
                resource.description(),
                java.time.Instant.now()
        );

        URI location = ucb.path("/api/v1/work-orders/{workOrderId}/intake-images").buildAndExpand(workOrderId).toUri();
        return ResponseEntity.created(location).body(imgResource);
    }

    @PostMapping("/{workOrderId}/start")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR') or hasRole('MECHANIC')")
    @Operation(summary = "Transition work order into active execution (IN_PROGRESS)")
    public ResponseEntity<?> startWorkOrder(@PathVariable UUID workOrderId) {
        StartWorkOrderCommand command = new StartWorkOrderCommand(new WorkOrderId(workOrderId));
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(WorkOrderResourceAssembler.toResourceFromEntity(result.getOrThrow()));
    }

    @PostMapping("/{workOrderId}/complete")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Complete work order closing all technical bay operations")
    public ResponseEntity<?> completeWorkOrder(@PathVariable UUID workOrderId) {
        CompleteWorkOrderCommand command = new CompleteWorkOrderCommand(new WorkOrderId(workOrderId));
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(WorkOrderResourceAssembler.toResourceFromEntity(result.getOrThrow()));
    }

    @PostMapping("/{workOrderId}/mark-as-paid")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('CASHIER') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Mark work order as formally paid enabling vehicle exit")
    public ResponseEntity<?> markAsPaid(@PathVariable UUID workOrderId) {
        MarkWorkOrderAsPaidCommand command = new MarkWorkOrderAsPaidCommand(new WorkOrderId(workOrderId));
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(WorkOrderResourceAssembler.toResourceFromEntity(result.getOrThrow()));
    }

    @PostMapping("/{workOrderId}/cancel")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Cancel work order releasing occupied bay and inventory reservations")
    public ResponseEntity<?> cancelWorkOrder(
            @PathVariable UUID workOrderId,
            @Valid @RequestBody CancelWorkOrderResource resource
    ) {
        CancelWorkOrderCommand command = CancelWorkOrderCommandFromResourceAssembler.toCommandFromResource(workOrderId, resource);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(WorkOrderResourceAssembler.toResourceFromEntity(result.getOrThrow()));
    }

    @PostMapping("/{workOrderId}/deliver")
    @PreAuthorize("hasAuthority('operations:work-orders:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Deliver vehicle back to owner following payment confirmation")
    public ResponseEntity<?> deliverVehicle(@PathVariable UUID workOrderId) {
        DeliverVehicleCommand command = new DeliverVehicleCommand(new WorkOrderId(workOrderId));
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(WorkOrderResourceAssembler.toResourceFromEntity(result.getOrThrow()));
    }
}
