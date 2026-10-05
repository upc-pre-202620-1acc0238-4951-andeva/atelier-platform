package com.andeva.atelier.platform.operations.domain.model.aggregates;

import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderImage;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskProduct;
import com.andeva.atelier.platform.operations.domain.model.enums.ProposalSeverity;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.domain.model.events.ProductStockReservationCancelledEvent;
import com.andeva.atelier.platform.operations.domain.model.events.ProductStockReservationRequestedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.TaskProposalApprovedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.TaskProposalRejectedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.TaskProposalSubmittedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkBayAssignedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkBayReleasedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderCompletedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderCreatedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderDeliveredEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderIntakeImageAttachedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderPaidEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderTaskCompletedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderTaskStartedEvent;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskProductId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class WorkOrder extends AbstractDomainAggregateRoot<WorkOrder> {
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    private final WorkOrderId id;
    private final TenantId tenantId;
    private final BranchId branchId;
    private UUID appointmentId;
    private final VehicleId vehicleId;
    private final CustomerId customerId;
    private final WorkOrderNumber internalNumber;
    private WorkBayId currentBayId;
    private Mileage mileageIn;
    private DiagnosticSummary diagnosticSummary;
    private Money subtotal;
    private Money tax;
    private Money totalAmount;
    private WorkOrderStatus status;
    private final List<WorkOrderTask> tasks = new ArrayList<>();
    private final List<TaskProposal> proposals = new ArrayList<>();
    private final List<WorkOrderImage> intakeImages = new ArrayList<>();

    public WorkOrder(
            WorkOrderId id,
            TenantId tenantId,
            BranchId branchId,
            UUID appointmentId,
            VehicleId vehicleId,
            CustomerId customerId,
            WorkOrderNumber internalNumber,
            WorkBayId currentBayId,
            Mileage mileageIn,
            DiagnosticSummary diagnosticSummary,
            Money subtotal,
            Money tax,
            Money totalAmount,
            WorkOrderStatus status
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "branchId cannot be null");
        this.appointmentId = appointmentId;
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "customerId cannot be null");
        this.internalNumber = Objects.requireNonNull(internalNumber, "internalNumber cannot be null");
        this.currentBayId = currentBayId;
        this.mileageIn = Objects.requireNonNull(mileageIn, "mileageIn cannot be null");
        this.diagnosticSummary = Objects.requireNonNull(diagnosticSummary, "diagnosticSummary cannot be null");
        this.subtotal = subtotal != null ? subtotal : Money.ZERO_PEN;
        this.tax = tax != null ? tax : Money.ZERO_PEN;
        this.totalAmount = totalAmount != null ? totalAmount : Money.ZERO_PEN;
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    public static WorkOrder create(
            TenantId tenantId,
            BranchId branchId,
            UUID appointmentId,
            VehicleId vehicleId,
            CustomerId customerId,
            WorkOrderNumber internalNumber,
            Mileage mileageIn,
            DiagnosticSummary diagnosticSummary
    ) {
        WorkOrderId id = WorkOrderId.generate();
        WorkOrder order = new WorkOrder(
                id,
                tenantId,
                branchId,
                appointmentId,
                vehicleId,
                customerId,
                internalNumber,
                null,
                mileageIn,
                diagnosticSummary,
                Money.ZERO_PEN,
                Money.ZERO_PEN,
                Money.ZERO_PEN,
                WorkOrderStatus.DRAFT
        );
        order.registerDomainEvent(WorkOrderCreatedEvent.of(id, tenantId, branchId, vehicleId, customerId, internalNumber));
        return order;
    }

    public void assignWorkBay(WorkBayId bayId) {
        validateMutable("Cannot assign work bay");
        this.currentBayId = Objects.requireNonNull(bayId, "bayId cannot be null");
        registerDomainEvent(WorkBayAssignedEvent.of(this.id, bayId));
    }

    public void releaseBay() {
        if (this.currentBayId != null) {
            WorkBayId released = this.currentBayId;
            this.currentBayId = null;
            registerDomainEvent(WorkBayReleasedEvent.of(this.id, released));
        }
    }

    public void startWork() {
        if (this.status == WorkOrderStatus.IN_PROGRESS) {
            return;
        }
        if (this.status != WorkOrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT order can be started directly: " + this.status);
        }
        this.status = WorkOrderStatus.IN_PROGRESS;
    }

    public WorkOrderTask addTask(ServiceId serviceId, UUID mechanicId, String description, Money price, LaborHours estimatedHours) {
        validateMutable("Cannot add task");
        WorkOrderTask task = WorkOrderTask.create(this.id, serviceId, mechanicId, description, price, estimatedHours);
        this.tasks.add(task);
        recalculateTotalAmount();
        return task;
    }

    public void removeTask(WorkOrderTaskId taskId) {
        validateMutable("Cannot remove task");
        WorkOrderTask task = findTask(taskId);
        if (task.getStatus() == WorkOrderTaskStatus.COMPLETED) {
            throw new IllegalStateException("Cannot remove already COMPLETED task");
        }
        this.tasks.remove(task);
        recalculateTotalAmount();
    }

    public void startTask(WorkOrderTaskId taskId) {
        WorkOrderTask task = findTask(taskId);
        task.start();
        if (this.status == WorkOrderStatus.DRAFT) {
            this.status = WorkOrderStatus.IN_PROGRESS;
        }
        registerDomainEvent(WorkOrderTaskStartedEvent.of(this.id, taskId, task.getMechanicId().orElse(null)));
    }

    public void completeTask(WorkOrderTaskId taskId, LaborHours actualHours) {
        WorkOrderTask task = findTask(taskId);
        task.complete(actualHours, null);
        registerDomainEvent(WorkOrderTaskCompletedEvent.of(this.id, taskId, task.getMechanicId().orElse(null), actualHours));

        boolean allCompleted = !this.tasks.isEmpty() && this.tasks.stream()
                .allMatch(t -> t.getStatus() == WorkOrderTaskStatus.COMPLETED || t.getStatus() == WorkOrderTaskStatus.CANCELLED);
        if (allCompleted && this.status == WorkOrderStatus.IN_PROGRESS) {
            this.status = WorkOrderStatus.COMPLETED;
            registerDomainEvent(WorkOrderCompletedEvent.of(this.id, this.tenantId, this.vehicleId, this.totalAmount));
        }
    }

    public TaskProposal submitProposal(UUID mechanicId, String desc, ProposalSeverity sev, StorageUrl url, ServiceId suggestedServiceId) {
        validateMutable("Cannot submit proposal");
        TaskProposal proposal = TaskProposal.create(this.id, mechanicId, desc, sev, url, suggestedServiceId);
        this.proposals.add(proposal);
        registerDomainEvent(TaskProposalSubmittedEvent.of(this.id, proposal.getId(), mechanicId, sev));
        return proposal;
    }

    public WorkOrderTask approveProposal(UUID proposalId, ServiceId serviceId, Money finalPrice, LaborHours hours, UUID mechanicId, String notes) {
        validateMutable("Cannot approve proposal");
        TaskProposal proposal = this.proposals.stream()
                .filter(p -> p.getId().equals(proposalId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("TaskProposal not found: " + proposalId));

        proposal.approve(notes);
        WorkOrderTask createdTask = addTask(serviceId, mechanicId, proposal.getDescription(), finalPrice, hours);
        proposal.linkTaskId(createdTask.getId());
        registerDomainEvent(TaskProposalApprovedEvent.of(this.id, proposalId, createdTask.getId()));
        return createdTask;
    }

    public void rejectProposal(UUID proposalId, String customerNotes) {
        TaskProposal proposal = this.proposals.stream()
                .filter(p -> p.getId().equals(proposalId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("TaskProposal not found: " + proposalId));

        proposal.reject(customerNotes);
        registerDomainEvent(TaskProposalRejectedEvent.of(this.id, proposalId, customerNotes));
    }

    public void addProductToTask(WorkOrderTaskId taskId, UUID productId, Quantity quantity, Money unitPrice) {
        validateMutable("Cannot add product to task");
        WorkOrderTask task = findTask(taskId);
        WorkOrderTaskProduct product = WorkOrderTaskProduct.create(taskId, productId, quantity, unitPrice);
        task.addProduct(product);
        recalculateTotalAmount();
        registerDomainEvent(ProductStockReservationRequestedEvent.of(this.id, taskId, productId, quantity));
    }

    public void removeProductFromTask(WorkOrderTaskId taskId, WorkOrderTaskProductId productItemId) {
        validateMutable("Cannot remove product from task");
        WorkOrderTask task = findTask(taskId);
        Optional<WorkOrderTaskProduct> found = task.getConsumedProducts().stream()
                .filter(p -> p.getId().equals(productItemId))
                .findFirst();

        found.ifPresent(p -> {
            task.removeProduct(productItemId);
            recalculateTotalAmount();
            registerDomainEvent(ProductStockReservationCancelledEvent.of(this.id, taskId, p.getProductId(), p.getQuantity()));
        });
    }

    public void attachIntakeImage(StorageUrl imageUrl, String description) {
        WorkOrderImage image = WorkOrderImage.create(this.id, imageUrl, description);
        this.intakeImages.add(image);
        registerDomainEvent(WorkOrderIntakeImageAttachedEvent.of(this.id, image.getId(), imageUrl));
    }

    public void recalculateTotalAmount() {
        Currency cur = this.subtotal.currency();
        BigDecimal laborTotal = BigDecimal.ZERO;
        BigDecimal productsTotal = BigDecimal.ZERO;

        for (WorkOrderTask task : this.tasks) {
            if (task.getStatus() != WorkOrderTaskStatus.CANCELLED) {
                laborTotal = laborTotal.add(task.getPrice().amount());
                for (WorkOrderTaskProduct product : task.getConsumedProducts()) {
                    productsTotal = productsTotal.add(product.getTotalAmount().amount());
                }
            }
        }

        BigDecimal sub = laborTotal.add(productsTotal).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal computedTax = sub.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal total = sub.add(computedTax).setScale(2, RoundingMode.HALF_EVEN);

        this.subtotal = new Money(sub, cur);
        this.tax = new Money(computedTax, cur);
        this.totalAmount = new Money(total, cur);
    }

    public void markPaid() {
        if (this.status != WorkOrderStatus.COMPLETED) {
            throw new IllegalStateException("Only COMPLETED orders can be marked as PAID: " + this.status);
        }
        this.status = WorkOrderStatus.PAID;
        registerDomainEvent(WorkOrderPaidEvent.of(this.id, this.tenantId, this.totalAmount));
    }

    public void deliverVehicle() {
        if (this.status != WorkOrderStatus.PAID) {
            throw new IllegalStateException("Vehicle can only be delivered after order is PAID: " + this.status);
        }
        releaseBay();
        registerDomainEvent(WorkOrderDeliveredEvent.of(this.id, this.tenantId, this.vehicleId));
    }

    public void cancel(String reason) {
        if (this.status.isTerminal()) {
            throw new IllegalStateException("Cannot cancel order in terminal status: " + this.status);
        }
        this.status = WorkOrderStatus.CANCELED;
        releaseBay();
        for (WorkOrderTask task : this.tasks) {
            for (WorkOrderTaskProduct product : task.getConsumedProducts()) {
                registerDomainEvent(ProductStockReservationCancelledEvent.of(this.id, task.getId(), product.getProductId(), product.getQuantity()));
            }
        }
    }

    public void updateDiagnosticAndMileage(DiagnosticSummary newSummary, Mileage newMileage) {
        validateMutable("Cannot update diagnostic and mileage");
        if (newSummary != null) this.diagnosticSummary = newSummary;
        if (newMileage != null) this.mileageIn = newMileage;
    }

    private WorkOrderTask findTask(WorkOrderTaskId taskId) {
        return this.tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("WorkOrderTask not found: " + taskId));
    }

    private void validateMutable(String operation) {
        if (!this.status.isMutable()) {
            throw new IllegalStateException(operation + " in non-mutable status: " + this.status);
        }
    }

    public WorkOrderId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public BranchId getBranchId() { return branchId; }
    public Optional<UUID> getAppointmentId() { return Optional.ofNullable(appointmentId); }
    public VehicleId getVehicleId() { return vehicleId; }
    public CustomerId getCustomerId() { return customerId; }
    public WorkOrderNumber getInternalNumber() { return internalNumber; }
    public Optional<WorkBayId> getCurrentBayId() { return Optional.ofNullable(currentBayId); }
    public Mileage getMileageIn() { return mileageIn; }
    public DiagnosticSummary getDiagnosticSummary() { return diagnosticSummary; }
    public Money getSubtotal() { return subtotal; }
    public Money getTax() { return tax; }
    public Money getTotalAmount() { return totalAmount; }
    public WorkOrderStatus getStatus() { return status; }
    public List<WorkOrderTask> getTasks() { return Collections.unmodifiableList(tasks); }
    public List<TaskProposal> getProposals() { return Collections.unmodifiableList(proposals); }
    public List<WorkOrderImage> getIntakeImages() { return Collections.unmodifiableList(intakeImages); }
}
