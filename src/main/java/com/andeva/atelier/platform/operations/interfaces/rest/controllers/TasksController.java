package com.andeva.atelier.platform.operations.interfaces.rest.controllers;

import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.*;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskProduct;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskProductId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrderTaskByIdQuery;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.*;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.TaskProductResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderTaskImageResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderTaskResource;
import com.andeva.atelier.platform.operations.interfaces.rest.transform.*;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller managing technical work order tasks, mechanics assignments, consumed parts, and bay inspection evidences.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Work Order Tasks", description = "Endpoints for managing individual technical tasks, mechanics, parts, and evidence in workshop bays")
public class TasksController {

    private final WorkOrderCommandService workOrderCommandService;
    private final WorkOrderQueryService workOrderQueryService;

    public TasksController(WorkOrderCommandService workOrderCommandService, WorkOrderQueryService workOrderQueryService) {
        this.workOrderCommandService = Objects.requireNonNull(workOrderCommandService, "workOrderCommandService cannot be null");
        this.workOrderQueryService = Objects.requireNonNull(workOrderQueryService, "workOrderQueryService cannot be null");
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAuthority('operations:tasks:read') or isAuthenticated()")
    @Operation(summary = "Get detailed technical task by id")
    public ResponseEntity<?> getTaskById(@PathVariable UUID taskId) {
        return workOrderQueryService.handle(new GetWorkOrderTaskByIdQuery(new WorkOrderTaskId(taskId)))
                .map(task -> WorkOrderTaskResourceAssembler.toResourceFromEntity(task, "Service", null))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAuthority('operations:tasks:update') or hasAuthority('operations:tasks:write') or hasRole('MECHANIC') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Update task parameters and assigned mechanic")
    public ResponseEntity<?> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateWorkOrderTaskResource resource
    ) {
        if (resource.mechanicId() != null) {
            AssignTaskMechanicCommand assignCmd = new AssignTaskMechanicCommand(new WorkOrderTaskId(taskId), resource.mechanicId());
            Result<WorkOrder, ApplicationError> assignRes = workOrderCommandService.handle(assignCmd);
            if (assignRes.isFailure()) {
                return ErrorResponseAssembler.toErrorResponseFromApplicationError(assignRes.getError());
            }
        }

        return workOrderQueryService.handle(new GetWorkOrderTaskByIdQuery(new WorkOrderTaskId(taskId)))
                .map(task -> WorkOrderTaskResourceAssembler.toResourceFromEntity(task, "Service", null))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{taskId}/mechanic")
    @PreAuthorize("hasAuthority('operations:tasks:update') or hasAuthority('operations:tasks:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Assign or change mechanic for technical task")
    public ResponseEntity<?> assignMechanic(
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTaskMechanicResource resource
    ) {
        AssignTaskMechanicCommand command = AssignTaskMechanicCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return findTaskAndBuildResponse(result.getOrThrow(), taskId);
    }

    @PostMapping("/{taskId}/start")
    @PreAuthorize("hasAuthority('operations:tasks:track_time') or hasAuthority('operations:tasks:write') or hasRole('MECHANIC')")
    @Operation(summary = "Start or resume execution of a technical task")
    public ResponseEntity<?> startTask(@PathVariable UUID taskId) {
        StartWorkOrderTaskCommand command = new StartWorkOrderTaskCommand(new WorkOrderTaskId(taskId));
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return findTaskAndBuildResponse(result.getOrThrow(), taskId);
    }

    @PostMapping("/{taskId}/hold")
    @PreAuthorize("hasAuthority('operations:tasks:track_time') or hasAuthority('operations:tasks:write') or hasRole('MECHANIC')")
    @Operation(summary = "Put task on hold due to missing parts or technical inspection")
    public ResponseEntity<?> holdTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody HoldTaskResource resource
    ) {
        HoldWorkOrderTaskCommand command = HoldTaskCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return findTaskAndBuildResponse(result.getOrThrow(), taskId);
    }

    @PostMapping("/{taskId}/resume")
    @PreAuthorize("hasAuthority('operations:tasks:track_time') or hasAuthority('operations:tasks:write') or hasRole('MECHANIC')")
    @Operation(summary = "Resume paused task after parts arrival")
    public ResponseEntity<?> resumeTask(@PathVariable UUID taskId) {
        ResumeWorkOrderTaskCommand command = new ResumeWorkOrderTaskCommand(new WorkOrderTaskId(taskId));
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return findTaskAndBuildResponse(result.getOrThrow(), taskId);
    }

    @PostMapping("/{taskId}/complete")
    @PreAuthorize("hasAuthority('operations:tasks:complete') or hasAuthority('operations:tasks:write') or hasRole('MECHANIC')")
    @Operation(summary = "Mark technical task as completed with actual labor hours")
    public ResponseEntity<?> completeTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody CompleteTaskResource resource
    ) {
        CompleteWorkOrderTaskCommand command = CompleteWorkOrderTaskCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return findTaskAndBuildResponse(result.getOrThrow(), taskId);
    }

    @PostMapping("/{taskId}/reopen")
    @PreAuthorize("hasAuthority('operations:tasks:update') or hasAuthority('operations:tasks:write') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Reopen completed task for quality rework")
    public ResponseEntity<?> reopenTask(
            @PathVariable UUID taskId,
            @RequestParam(required = false, defaultValue = "Reapertura de control de calidad") String reason
    ) {
        ReopenWorkOrderTaskCommand command = new ReopenWorkOrderTaskCommand(new WorkOrderTaskId(taskId), reason);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return findTaskAndBuildResponse(result.getOrThrow(), taskId);
    }

    @PostMapping("/{taskId}/products")
    @PreAuthorize("hasAuthority('operations:tasks:update') or hasAuthority('operations:tasks:write') or hasRole('MECHANIC') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Add consumed product or spare part to technical task")
    public ResponseEntity<?> addProduct(
            @PathVariable UUID taskId,
            @Valid @RequestBody AddTaskProductResource resource,
            UriComponentsBuilder ucb
    ) {
        AddProductToTaskCommand command = AddTaskProductCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        TaskProductResource productResource = new TaskProductResource(
                UUID.randomUUID(),
                taskId,
                resource.productId(),
                "Product " + resource.productId(),
                resource.quantity(),
                resource.unitPrice(),
                resource.unitPrice().multiply(resource.quantity()),
                resource.currency() != null ? resource.currency() : "PEN"
        );

        URI location = ucb.path("/api/v1/tasks/{taskId}/products/{productId}")
                .buildAndExpand(taskId, resource.productId()).toUri();
        return ResponseEntity.created(location).body(productResource);
    }

    @PutMapping("/{taskId}/products/{productId}")
    @PreAuthorize("hasAuthority('operations:tasks:update') or hasAuthority('operations:tasks:write') or hasRole('SERVICE_ADVISOR') or hasRole('MECHANIC')")
    @Operation(summary = "Update quantity of consumed spare part")
    public ResponseEntity<?> updateProductQuantity(
            @PathVariable UUID taskId,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateTaskProductResource resource
    ) {
        UpdateTaskProductQuantityCommand command = UpdateTaskProductQuantityCommandFromResourceAssembler.toCommandFromResource(taskId, productId, resource);
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        TaskProductResource productResource = new TaskProductResource(
                UUID.randomUUID(),
                taskId,
                productId,
                "Product " + productId,
                resource.quantity(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "PEN"
        );

        return ResponseEntity.ok(productResource);
    }

    @DeleteMapping("/{taskId}/products/{productId}")
    @PreAuthorize("hasAuthority('operations:tasks:update') or hasAuthority('operations:tasks:write') or hasRole('MECHANIC') or hasRole('SERVICE_ADVISOR')")
    @Operation(summary = "Remove consumed product or spare part from technical task")
    public ResponseEntity<?> removeTaskProduct(
            @PathVariable UUID taskId,
            @PathVariable UUID productId
    ) {
        RemoveProductFromTaskCommand command = new RemoveProductFromTaskCommand(
                new WorkOrderTaskId(taskId),
                new WorkOrderTaskProductId(productId)
        );
        Result<WorkOrder, ApplicationError> result = workOrderCommandService.handle(command);
        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/evidence-images")
    @PreAuthorize("hasAuthority('operations:tasks:upload_photos') or hasAuthority('operations:tasks:write') or hasRole('MECHANIC')")
    @Operation(summary = "Attach photographic evidence to technical task")
    public ResponseEntity<?> attachEvidence(
            @PathVariable UUID taskId,
            @Valid @RequestBody AttachTaskEvidenceResource resource,
            UriComponentsBuilder ucb
    ) {
        AttachTaskEvidenceImageCommand command = AttachTaskEvidenceCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        Result<WorkOrderTask, ApplicationError> result = workOrderCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        WorkOrderTask task = result.getOrThrow();
        var imgOpt = task.getTaskImages().stream()
                .filter(img -> img.getImageUrl().value().equals(resource.imageUrl()))
                .reduce((first, second) -> second);

        UUID imgId = imgOpt.map(com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskImage::getId).orElseGet(UUID::randomUUID);
        java.time.Instant uploadedAt = imgOpt.map(com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskImage::getUploadedAt).orElseGet(java.time.Instant::now);

        WorkOrderTaskImageResource imgResource = new WorkOrderTaskImageResource(
                imgId,
                taskId,
                resource.imageUrl(),
                resource.description(),
                uploadedAt
        );

        URI location = ucb.path("/api/v1/tasks/{taskId}/evidence-images").buildAndExpand(taskId).toUri();
        return ResponseEntity.created(location).body(imgResource);
    }

    private ResponseEntity<?> findTaskAndBuildResponse(WorkOrder order, UUID taskId) {
        return order.getTasks().stream()
                .filter(t -> t.getId().value().equals(taskId))
                .findFirst()
                .map(task -> WorkOrderTaskResourceAssembler.toResourceFromEntity(task, "Service", null))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
