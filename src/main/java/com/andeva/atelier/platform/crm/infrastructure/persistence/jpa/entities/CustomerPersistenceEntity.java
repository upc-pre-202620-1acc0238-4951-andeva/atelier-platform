package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.converters.CustomerTypeAttributeConverter;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code customers} relational table.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customers_tenant_tax_id", columnNames = {"tenant_id", "tax_id"})
        },
        indexes = {
                @Index(name = "idx_customers_tenant_tax_id", columnList = "tenant_id, tax_id"),
                @Index(name = "idx_customers_search", columnList = "tenant_id, status")
        }
)
public class CustomerPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Convert(converter = CustomerTypeAttributeConverter.class)
    @Column(name = "type", nullable = false, length = 20)
    private CustomerType type;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "tax_id", nullable = false, length = 20)
    private String taxId;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public CustomerPersistenceEntity(UUID id) {
        super(id);
    }

    public CustomerPersistenceEntity(
            UUID id,
            UUID tenantId,
            CustomerType type,
            String firstName,
            String lastName,
            String companyName,
            String taxId,
            String email,
            String phone,
            String status
    ) {
        super(id);
        this.tenantId = tenantId;
        this.type = type;
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
        this.taxId = taxId;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }
}
