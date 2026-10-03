package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code tenants} relational table.
 * Represents an automotive workshop company serving as the root of multi-tenant isolation.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "tenants",
        indexes = {
                @Index(name = "idx_tenants_tax_id", columnList = "tax_id", unique = true),
                @Index(name = "idx_tenants_status", columnList = "status")
        }
)
public class TenantPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "legal_name", nullable = false, length = 150)
    private String legalName;

    @Column(name = "tax_id", nullable = false, unique = true, length = 20)
    private String taxId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BranchPersistenceEntity> branches = new ArrayList<>();

    public TenantPersistenceEntity(UUID id) {
        super(id);
    }

    public TenantPersistenceEntity(
            UUID id,
            String name,
            String legalName,
            String taxId,
            String status,
            String stripeCustomerId) {
        super(id);
        this.name = name;
        this.legalName = legalName;
        this.taxId = taxId;
        this.status = status;
        this.stripeCustomerId = stripeCustomerId;
        this.branches = new ArrayList<>();
    }

    /**
     * Helper method to maintain bidirectional relationship with branches.
     */
    public void addBranch(BranchPersistenceEntity branch) {
        this.branches.add(branch);
        branch.setTenant(this);
    }

    /**
     * Helper method to remove a branch and clear relationship.
     */
    public void removeBranch(BranchPersistenceEntity branch) {
        this.branches.remove(branch);
        branch.setTenant(null);
    }
}
