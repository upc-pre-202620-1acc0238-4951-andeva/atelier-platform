package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters.BayStatusAttributeConverter;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters.BayTypeAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "work_bays",
        indexes = {
                @Index(name = "idx_work_bays_tenant_branch", columnList = "tenant_id, branch_id"),
                @Index(name = "idx_work_bays_status", columnList = "tenant_id, status")
        }
)
public class WorkBayPersistenceEntity extends OperationsAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Convert(converter = BayTypeAttributeConverter.class)
    @Column(name = "type", nullable = false, length = 20)
    private BayType type;

    @Convert(converter = BayStatusAttributeConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private BayStatus status;

    @Transient
    private UUID currentWorkOrderId;
}
