package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code suppliers} relational table.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "suppliers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_suppliers_tenant_tax_id", columnNames = {"tenant_id", "tax_id"})
        },
        indexes = {
                @Index(name = "idx_suppliers_tenant_tax_id", columnList = "tenant_id, tax_id"),
                @Index(name = "idx_suppliers_tenant_business_name", columnList = "tenant_id, business_name")
        }
)
public class SupplierPersistenceEntity extends InventoryAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "tax_id", nullable = false, length = 20)
    private String taxId;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public SupplierPersistenceEntity(UUID id) {
        super(id);
    }

    public SupplierPersistenceEntity(
            UUID id,
            UUID tenantId,
            String businessName,
            String taxId,
            String contactName,
            String phone,
            String email,
            String address,
            boolean active
    ) {
        super(id);
        this.tenantId = tenantId;
        this.businessName = businessName;
        this.taxId = taxId;
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.active = active;
    }
}
