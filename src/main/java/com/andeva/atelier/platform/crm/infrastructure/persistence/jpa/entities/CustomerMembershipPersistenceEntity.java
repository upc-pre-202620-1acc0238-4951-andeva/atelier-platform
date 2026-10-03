package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.FleetRole;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code customer_memberships} relational table.
 *
 * @author Adiel Sanchez Santin
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "customer_memberships",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cm_customer_user", columnNames = {"customer_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_cm_customer", columnList = "customer_id"),
                @Index(name = "idx_cm_user", columnList = "user_id, status")
        }
)
public class CustomerMembershipPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private FleetRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CustomerMembershipStatus status;

    public CustomerMembershipPersistenceEntity(UUID id) {
        super(id);
    }

    public CustomerMembershipPersistenceEntity(
            UUID id,
            UUID customerId,
            UUID userId,
            FleetRole role,
            CustomerMembershipStatus status
    ) {
        super(id);
        this.customerId = customerId;
        this.userId = userId;
        this.role = role;
        this.status = status;
    }
}
