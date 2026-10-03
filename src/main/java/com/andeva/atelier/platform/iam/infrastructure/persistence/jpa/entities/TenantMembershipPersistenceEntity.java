package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code tenant_memberships} relational table.
 * Represents an employment contract and authorization binding between a user and a tenant.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "tenant_memberships",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_memberships_tenant_user", columnNames = {"tenant_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_memberships_tenant_status", columnList = "tenant_id, status"),
                @Index(name = "idx_memberships_user_id", columnList = "user_id")
        }
)
public class TenantMembershipPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantPersistenceEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserPersistenceEntity user;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "salary_type", nullable = false, length = 20)
    private String salaryType;

    @Column(name = "base_salary", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseSalary;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "membership_roles",
            joinColumns = @JoinColumn(name = "membership_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RolePersistenceEntity> assignedRoles = new HashSet<>();

    public TenantMembershipPersistenceEntity(UUID id) {
        super(id);
    }

    public TenantMembershipPersistenceEntity(
            UUID id,
            TenantPersistenceEntity tenant,
            UserPersistenceEntity user,
            String status,
            String salaryType,
            BigDecimal baseSalary,
            Set<RolePersistenceEntity> assignedRoles) {
        super(id);
        this.tenant = tenant;
        this.user = user;
        this.status = status;
        this.salaryType = salaryType;
        this.baseSalary = baseSalary;
        this.assignedRoles = assignedRoles != null ? new HashSet<>(assignedRoles) : new HashSet<>();
    }
}
