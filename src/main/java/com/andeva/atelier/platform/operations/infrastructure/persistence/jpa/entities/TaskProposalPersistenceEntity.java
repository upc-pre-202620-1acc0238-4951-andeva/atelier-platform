package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.operations.domain.model.enums.ProposalSeverity;
import com.andeva.atelier.platform.operations.domain.model.enums.ProposalStatus;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters.ProposalSeverityAttributeConverter;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters.ProposalStatusAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "task_proposals",
        indexes = {
                @Index(name = "idx_proposals_work_order", columnList = "work_order_id"),
                @Index(name = "idx_proposals_status", columnList = "status")
        }
)
public class TaskProposalPersistenceEntity extends OperationsChildPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrderPersistenceEntity workOrder;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "mechanic_id", nullable = false)
    private UUID mechanicId;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Convert(converter = ProposalSeverityAttributeConverter.class)
    @Column(name = "severity", nullable = false, length = 20)
    private ProposalSeverity severity;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Convert(converter = ProposalStatusAttributeConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private ProposalStatus status;

    @Column(name = "customer_notes", length = 500)
    private String customerNotes;
}
