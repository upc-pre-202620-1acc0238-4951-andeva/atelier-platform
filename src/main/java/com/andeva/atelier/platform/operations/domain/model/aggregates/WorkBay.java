package com.andeva.atelier.platform.operations.domain.model.aggregates;

import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;
import java.util.Optional;

public class WorkBay extends AbstractDomainAggregateRoot<WorkBay> {
    private final WorkBayId id;
    private final TenantId tenantId;
    private final BranchId branchId;
    private String name;
    private BayType type;
    private BayStatus status;
    private WorkOrderId currentWorkOrderId;

    public WorkBay(WorkBayId id, TenantId tenantId, BranchId branchId, String name, BayType type, BayStatus status, WorkOrderId currentWorkOrderId) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "branchId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.currentWorkOrderId = currentWorkOrderId;
    }

    public static WorkBay create(TenantId tenantId, BranchId branchId, String name, BayType type) {
        return new WorkBay(WorkBayId.generate(), tenantId, branchId, name, type, BayStatus.AVAILABLE, null);
    }

    public void occupy(WorkOrderId orderId) {
        if (this.status != BayStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot occupy bay in status: " + this.status);
        }
        this.status = BayStatus.OCCUPIED;
        this.currentWorkOrderId = Objects.requireNonNull(orderId, "orderId cannot be null");
    }

    public void release() {
        if (this.status != BayStatus.OCCUPIED) {
            throw new IllegalStateException("Only OCCUPIED bay can be released: " + this.status);
        }
        this.status = BayStatus.AVAILABLE;
        this.currentWorkOrderId = null;
    }

    public void setUnderMaintenance(String reason) {
        if (this.status == BayStatus.OCCUPIED) {
            throw new IllegalStateException("Cannot put OCCUPIED bay under maintenance");
        }
        this.status = BayStatus.MAINTENANCE;
    }

    public void restoreAvailable() {
        if (this.status == BayStatus.OCCUPIED) {
            throw new IllegalStateException("Cannot restore bay while OCCUPIED");
        }
        this.status = BayStatus.AVAILABLE;
        this.currentWorkOrderId = null;
    }

    public WorkBayId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public BranchId getBranchId() { return branchId; }
    public String getName() { return name; }
    public BayType getType() { return type; }
    public BayStatus getStatus() { return status; }
    public Optional<WorkOrderId> getCurrentWorkOrderId() { return Optional.ofNullable(currentWorkOrderId); }
}
