package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "services",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_services_tenant_name", columnNames = {"tenant_id", "name"})
        },
        indexes = {
                @Index(name = "idx_services_tenant_name", columnList = "tenant_id, name")
        }
)
public class ServicePersistenceEntity extends OperationsAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "estimated_time_m", nullable = false)
    private Integer estimatedTimeM;
}
