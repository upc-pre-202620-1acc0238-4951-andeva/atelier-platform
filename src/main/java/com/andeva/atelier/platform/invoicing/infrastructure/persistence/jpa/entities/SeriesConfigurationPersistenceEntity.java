package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

/**
 * JPA Persistence Entity mapping official SUNAT series configurations with atomic correlative counter.
 *
 * @author Joel Huamani Estefanero
 */
@Entity
@Table(name = "sunat_series_configurations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_series_branch_type_serie", columnNames = {"branch_id", "voucher_type", "serie"})
})
public class SeriesConfigurationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "branch_id", nullable = false, updatable = false)
    private UUID branchId;

    @Column(name = "voucher_type", nullable = false, length = 20)
    private String voucherType;

    @Column(name = "serie", nullable = false, length = 4)
    private String serie;

    @Column(name = "current_correlative", nullable = false)
    private int currentCorrelative;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public SeriesConfigurationPersistenceEntity() {
    }

    public SeriesConfigurationPersistenceEntity(
            UUID id,
            UUID tenantId,
            UUID branchId,
            String voucherType,
            String serie,
            int currentCorrelative,
            boolean isActive
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.branchId = branchId;
        this.voucherType = voucherType;
        this.serie = serie;
        this.currentCorrelative = currentCorrelative;
        this.isActive = isActive;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public int getCurrentCorrelative() {
        return currentCorrelative;
    }

    public void setCurrentCorrelative(int currentCorrelative) {
        this.currentCorrelative = currentCorrelative;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
