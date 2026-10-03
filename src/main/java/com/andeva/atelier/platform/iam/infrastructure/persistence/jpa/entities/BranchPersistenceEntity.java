package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
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

import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code branches} relational table.
 * Represents a physical workshop location or operating yard.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "branches",
        indexes = {
                @Index(name = "idx_branches_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_branches_coords", columnList = "latitude, longitude")
        }
)
public class BranchPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantPersistenceEntity tenant;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sunat_code", length = 10)
    private String sunatCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "geofence_radius_m", nullable = false)
    private int geofenceRadiusMeters;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public BranchPersistenceEntity(UUID id) {
        super(id);
    }

    public BranchPersistenceEntity(
            UUID id,
            TenantPersistenceEntity tenant,
            String name,
            String sunatCode,
            Double latitude,
            Double longitude,
            int geofenceRadiusMeters,
            boolean active) {
        super(id);
        this.tenant = tenant;
        this.name = name;
        this.sunatCode = sunatCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geofenceRadiusMeters = geofenceRadiusMeters;
        this.active = active;
    }
}
