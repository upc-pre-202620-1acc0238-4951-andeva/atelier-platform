package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.operations.domain.model.enums.EvidenceType;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters.EvidenceTypeAttributeConverter;
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

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "work_order_task_images",
        indexes = {
                @Index(name = "idx_task_images_task", columnList = "task_id")
        }
)
public class WorkOrderTaskImagePersistenceEntity extends OperationsChildPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private WorkOrderTaskPersistenceEntity task;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Convert(converter = EvidenceTypeAttributeConverter.class)
    @Column(name = "evidence_type", nullable = false, length = 30)
    private EvidenceType evidenceType;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}
