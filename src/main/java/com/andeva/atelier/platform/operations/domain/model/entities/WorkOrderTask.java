package com.andeva.atelier.platform.operations.domain.model.entities;

import com.andeva.atelier.platform.operations.domain.model.enums.EvidenceType;
import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskProductId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class WorkOrderTask {
    private final WorkOrderTaskId id;
    private final WorkOrderId workOrderId;
    private final ServiceId serviceId;
    private UUID mechanicId;
    private WorkOrderTaskStatus status;
    private String description;
    private Money price;
    private LaborHours estimatedHours;
    private LaborHours actualHours;
    private HoldReason holdReason;
    private String missingItemDescription;
    private Instant pausedAt;
    private long totalPausedSeconds;
    private Instant startedAt;
    private Instant completedAt;
    private final List<WorkOrderTaskProduct> consumedProducts = new ArrayList<>();
    private final List<WorkOrderTaskImage> taskImages = new ArrayList<>();

    public WorkOrderTask(WorkOrderTaskId id, WorkOrderId workOrderId, ServiceId serviceId, UUID mechanicId, WorkOrderTaskStatus status, String description, Money price, LaborHours estimatedHours, LaborHours actualHours, HoldReason holdReason, String missingItemDescription, Instant pausedAt, long totalPausedSeconds, Instant startedAt, Instant completedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.workOrderId = Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId cannot be null");
        this.mechanicId = mechanicId;
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.price = Objects.requireNonNull(price, "price cannot be null");
        this.estimatedHours = Objects.requireNonNull(estimatedHours, "estimatedHours cannot be null");
        this.actualHours = actualHours;
        this.holdReason = holdReason;
        this.missingItemDescription = missingItemDescription;
        this.pausedAt = pausedAt;
        this.totalPausedSeconds = totalPausedSeconds;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static WorkOrderTask create(WorkOrderId workOrderId, ServiceId serviceId, UUID mechanicId, String description, Money price, LaborHours estimatedHours) {
        WorkOrderTaskStatus initialStatus = mechanicId != null ? WorkOrderTaskStatus.ASSIGNED : WorkOrderTaskStatus.PENDING;
        return new WorkOrderTask(
                WorkOrderTaskId.generate(),
                workOrderId,
                serviceId,
                mechanicId,
                initialStatus,
                description,
                price,
                estimatedHours,
                null,
                null,
                null,
                null,
                0L,
                null,
                null
        );
    }

    public void assignMechanic(UUID mechanicMembershipId) {
        if (this.status == WorkOrderTaskStatus.COMPLETED || this.status == WorkOrderTaskStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reassign mechanic in terminal task state: " + this.status);
        }
        this.mechanicId = Objects.requireNonNull(mechanicMembershipId, "mechanicMembershipId cannot be null");
        if (this.status == WorkOrderTaskStatus.PENDING) {
            this.status = WorkOrderTaskStatus.ASSIGNED;
        }
    }

    public void start() {
        if (this.status == WorkOrderTaskStatus.IN_PROGRESS) {
            return;
        }
        if (this.status == WorkOrderTaskStatus.COMPLETED || this.status == WorkOrderTaskStatus.CANCELLED) {
            throw new IllegalStateException("Cannot start task in status: " + this.status);
        }
        this.status = WorkOrderTaskStatus.IN_PROGRESS;
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public void holdForWaitingParts(String missingDesc, UUID itemId) {
        if (this.status != WorkOrderTaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task must be IN_PROGRESS to be put on hold: " + this.status);
        }
        this.status = WorkOrderTaskStatus.ON_HOLD;
        this.holdReason = HoldReason.WAITING_PARTS;
        this.missingItemDescription = missingDesc;
        this.pausedAt = Instant.now();
    }

    public void resume() {
        if (this.status != WorkOrderTaskStatus.ON_HOLD) {
            throw new IllegalStateException("Task must be ON_HOLD to resume: " + this.status);
        }
        if (this.pausedAt != null) {
            this.totalPausedSeconds += Duration.between(this.pausedAt, Instant.now()).getSeconds();
            this.pausedAt = null;
        }
        this.status = WorkOrderTaskStatus.IN_PROGRESS;
        this.holdReason = null;
        this.missingItemDescription = null;
    }

    public void complete(LaborHours actualHours, String notes) {
        if (this.status == WorkOrderTaskStatus.COMPLETED || this.status == WorkOrderTaskStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete task in status: " + this.status);
        }
        this.status = WorkOrderTaskStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.actualHours = Objects.requireNonNull(actualHours, "actualHours cannot be null");
    }

    public void reopen(String reason) {
        if (this.status != WorkOrderTaskStatus.COMPLETED) {
            throw new IllegalStateException("Only COMPLETED tasks can be reopened: " + this.status);
        }
        this.status = WorkOrderTaskStatus.IN_PROGRESS;
        this.completedAt = null;
    }

    public void updatePrice(Money newPrice) {
        this.price = Objects.requireNonNull(newPrice, "newPrice cannot be null");
    }

    public void addProduct(WorkOrderTaskProduct product) {
        this.consumedProducts.add(Objects.requireNonNull(product, "product cannot be null"));
    }

    public void removeProduct(WorkOrderTaskProductId productId) {
        this.consumedProducts.removeIf(p -> p.getId().equals(productId));
    }

    public void attachEvidenceImage(StorageUrl url, EvidenceType evidenceType, String description) {
        this.taskImages.add(WorkOrderTaskImage.create(this.id, url, evidenceType, description));
    }

    public WorkOrderTaskId getId() { return id; }
    public WorkOrderId getWorkOrderId() { return workOrderId; }
    public ServiceId getServiceId() { return serviceId; }
    public Optional<UUID> getMechanicId() { return Optional.ofNullable(mechanicId); }
    public WorkOrderTaskStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public Money getPrice() { return price; }
    public LaborHours getEstimatedHours() { return estimatedHours; }
    public Optional<LaborHours> getActualHours() { return Optional.ofNullable(actualHours); }
    public Optional<HoldReason> getHoldReason() { return Optional.ofNullable(holdReason); }
    public Optional<String> getMissingItemDescription() { return Optional.ofNullable(missingItemDescription); }
    public Optional<Instant> getPausedAt() { return Optional.ofNullable(pausedAt); }
    public long getTotalPausedSeconds() { return totalPausedSeconds; }
    public Optional<Instant> getStartedAt() { return Optional.ofNullable(startedAt); }
    public Optional<Instant> getCompletedAt() { return completedAt != null ? Optional.of(completedAt) : Optional.empty(); }
    public List<WorkOrderTaskProduct> getConsumedProducts() { return Collections.unmodifiableList(consumedProducts); }
    public List<WorkOrderTaskImage> getTaskImages() { return Collections.unmodifiableList(taskImages); }

    public void addExistingImage(WorkOrderTaskImage image) {
        if (image != null) {
            this.taskImages.add(image);
        }
    }
}
