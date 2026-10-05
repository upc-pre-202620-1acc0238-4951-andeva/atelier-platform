package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters.WorkOrderStatusAttributeConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "work_orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_work_orders_tenant_number", columnNames = {"tenant_id", "internal_number"})
        },
        indexes = {
                @Index(name = "idx_work_orders_tenant_branch_status", columnList = "tenant_id, branch_id, status"),
                @Index(name = "idx_work_orders_vehicle", columnList = "vehicle_id"),
                @Index(name = "idx_work_orders_customer", columnList = "customer_id"),
                @Index(name = "idx_work_orders_current_bay", columnList = "current_bay_id")
        }
)
public class WorkOrderPersistenceEntity extends OperationsAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "internal_number", nullable = false)
    private Integer internalNumber;

    @Column(name = "current_bay_id")
    private UUID currentBayId;

    @Column(name = "mileage_in", nullable = false)
    private Integer mileageIn;

    @Column(name = "diagnostic_summary", nullable = false, columnDefinition = "TEXT")
    private String diagnosticSummary;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax", nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Convert(converter = WorkOrderStatusAttributeConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private WorkOrderStatus status;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkOrderImagePersistenceEntity> images = new ArrayList<>();

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkOrderTaskPersistenceEntity> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaskProposalPersistenceEntity> proposals = new ArrayList<>();
}
