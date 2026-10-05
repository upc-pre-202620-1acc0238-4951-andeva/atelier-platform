package com.andeva.atelier.platform.operations.domain.model.entities;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class WorkOrderImage {
    private final UUID id;
    private final WorkOrderId workOrderId;
    private final StorageUrl imageUrl;
    private final String description;
    private final Instant uploadedAt;

    public WorkOrderImage(UUID id, WorkOrderId workOrderId, StorageUrl imageUrl, String description, Instant uploadedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.workOrderId = Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        this.imageUrl = Objects.requireNonNull(imageUrl, "imageUrl cannot be null");
        this.description = description;
        this.uploadedAt = Objects.requireNonNull(uploadedAt, "uploadedAt cannot be null");
    }

    public static WorkOrderImage create(WorkOrderId workOrderId, StorageUrl imageUrl, String description) {
        return new WorkOrderImage(UUID.randomUUID(), workOrderId, imageUrl, description, Instant.now());
    }

    public UUID getId() { return id; }
    public WorkOrderId getWorkOrderId() { return workOrderId; }
    public StorageUrl getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public Instant getUploadedAt() { return uploadedAt; }
}
