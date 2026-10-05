package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
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
        name = "work_order_images",
        indexes = {
                @Index(name = "idx_images_work_order", columnList = "work_order_id")
        }
)
public class WorkOrderImagePersistenceEntity extends OperationsChildPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrderPersistenceEntity workOrder;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}
