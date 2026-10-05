package com.andeva.atelier.platform.operations.domain.model.entities;

import com.andeva.atelier.platform.operations.domain.model.enums.ProposalSeverity;
import com.andeva.atelier.platform.operations.domain.model.enums.ProposalStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class TaskProposal {
    private final UUID id;
    private final WorkOrderId workOrderId;
    private WorkOrderTaskId taskId;
    private ServiceId serviceId;
    private final UUID mechanicId;
    private final String description;
    private final ProposalSeverity severity;
    private final StorageUrl imageUrl;
    private ProposalStatus status;
    private String customerNotes;
    private final Instant createdAt;
    private Instant updatedAt;

    public TaskProposal(UUID id, WorkOrderId workOrderId, WorkOrderTaskId taskId, ServiceId serviceId, UUID mechanicId, String description, ProposalSeverity severity, StorageUrl imageUrl, ProposalStatus status, String customerNotes, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.workOrderId = Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        this.taskId = taskId;
        this.serviceId = serviceId;
        this.mechanicId = Objects.requireNonNull(mechanicId, "mechanicId cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.severity = Objects.requireNonNull(severity, "severity cannot be null");
        this.imageUrl = Objects.requireNonNull(imageUrl, "imageUrl cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.customerNotes = customerNotes;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    public static TaskProposal create(WorkOrderId workOrderId, UUID mechanicId, String description, ProposalSeverity severity, StorageUrl imageUrl, ServiceId suggestedServiceId) {
        Instant now = Instant.now();
        return new TaskProposal(
                UUID.randomUUID(),
                workOrderId,
                null,
                suggestedServiceId,
                mechanicId,
                description,
                severity,
                imageUrl,
                ProposalStatus.PENDING_REVIEW,
                null,
                now,
                now
        );
    }

    public void approve(String notes) {
        if (this.status != ProposalStatus.PENDING_REVIEW) {
            throw new IllegalStateException("TaskProposal already evaluated: " + this.status);
        }
        this.status = ProposalStatus.APPROVED;
        this.customerNotes = notes;
        this.updatedAt = Instant.now();
    }

    public void reject(String customerReason) {
        if (this.status != ProposalStatus.PENDING_REVIEW) {
            throw new IllegalStateException("TaskProposal already evaluated: " + this.status);
        }
        this.status = ProposalStatus.REJECTED;
        this.customerNotes = customerReason;
        this.updatedAt = Instant.now();
    }

    public void linkTaskId(WorkOrderTaskId taskId) {
        this.taskId = taskId;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public WorkOrderId getWorkOrderId() { return workOrderId; }
    public Optional<WorkOrderTaskId> getTaskId() { return Optional.ofNullable(taskId); }
    public Optional<ServiceId> getServiceId() { return Optional.ofNullable(serviceId); }
    public UUID getMechanicId() { return mechanicId; }
    public String getDescription() { return description; }
    public ProposalSeverity getSeverity() { return severity; }
    public StorageUrl getImageUrl() { return imageUrl; }
    public ProposalStatus getStatus() { return status; }
    public Optional<String> getCustomerNotes() { return Optional.ofNullable(customerNotes); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
