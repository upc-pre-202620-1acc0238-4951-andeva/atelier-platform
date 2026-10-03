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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code roles} relational table.
 * Encapsulates RBAC security roles scoped to a specific workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "roles",
        indexes = {
                @Index(name = "idx_roles_tenant_name", columnList = "tenant_id, name"),
                @Index(name = "idx_roles_tenant_code", columnList = "tenant_id, code")
        }
)
public class RolePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantPersistenceEntity tenant;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "is_system_role", nullable = false)
    private boolean systemRole;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionPersistenceEntity> permissions = new HashSet<>();

    public RolePersistenceEntity(UUID id) {
        super(id);
    }

    public RolePersistenceEntity(
            UUID id,
            TenantPersistenceEntity tenant,
            String code,
            String name,
            String description,
            boolean systemRole,
            Set<PermissionPersistenceEntity> permissions) {
        super(id);
        this.tenant = tenant;
        this.code = code;
        this.name = name;
        this.description = description;
        this.systemRole = systemRole;
        this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }
}
