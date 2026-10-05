package com.andeva.atelier.platform.operations.domain.model.entities;

import com.andeva.atelier.platform.operations.domain.model.enums.EvidenceType;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class WorkOrderTaskImage {
    private final UUID id;
    private final WorkOrderTaskId taskId;
    private final StorageUrl imageUrl;
    private final EvidenceType evidenceType;
    private final String description;
    private final Instant uploadedAt;

    public WorkOrderTaskImage(UUID id, WorkOrderTaskId taskId, StorageUrl imageUrl, EvidenceType evidenceType, String description, Instant uploadedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.taskId = Objects.requireNonNull(taskId, "taskId cannot be null");
        this.imageUrl = Objects.requireNonNull(imageUrl, "imageUrl cannot be null");
        this.evidenceType = Objects.requireNonNull(evidenceType, "evidenceType cannot be null");
        this.description = description;
        this.uploadedAt = Objects.requireNonNull(uploadedAt, "uploadedAt cannot be null");
    }

    public static WorkOrderTaskImage create(WorkOrderTaskId taskId, StorageUrl imageUrl, EvidenceType evidenceType, String description) {
        return new WorkOrderTaskImage(UUID.randomUUID(), taskId, imageUrl, evidenceType, description, Instant.now());
    }

    public UUID getId() { return id; }
    public WorkOrderTaskId getTaskId() { return taskId; }
    public StorageUrl getImageUrl() { return imageUrl; }
    public EvidenceType getEvidenceType() { return evidenceType; }
    public String getDescription() { return description; }
    public Instant getUploadedAt() { return uploadedAt; }
}
