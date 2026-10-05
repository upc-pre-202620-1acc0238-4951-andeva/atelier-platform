package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters.HoldReasonAttributeConverter;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters.WorkOrderTaskStatusAttributeConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "work_order_tasks",
        indexes = {
                @Index(name = "idx_tasks_work_order", columnList = "work_order_id"),
                @Index(name = "idx_tasks_mechanic_status", columnList = "mechanic_id, status")
        }
)
public class WorkOrderTaskPersistenceEntity extends OperationsChildPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrderPersistenceEntity workOrder;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "mechanic_id")
    private UUID mechanicId;

    @Convert(converter = WorkOrderTaskStatusAttributeConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private WorkOrderTaskStatus status;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "estimated_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "actual_hours", precision = 4, scale = 2)
    private BigDecimal actualHours;

    @Convert(converter = HoldReasonAttributeConverter.class)
    @Column(name = "hold_reason", length = 50)
    private HoldReason holdReason;

    @Column(name = "missing_item_description", columnDefinition = "TEXT")
    private String missingItemDescription;

    @Column(name = "paused_at")
    private Instant pausedAt;

    @Column(name = "total_paused_seconds", nullable = false)
    private Long totalPausedSeconds = 0L;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkOrderTaskProductPersistenceEntity> products = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkOrderTaskImagePersistenceEntity> images = new ArrayList<>();
}
