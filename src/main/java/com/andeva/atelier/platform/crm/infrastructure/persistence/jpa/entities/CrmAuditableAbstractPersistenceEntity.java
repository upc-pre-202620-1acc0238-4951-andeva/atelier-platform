package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Base mapped superclass for CRM persistence entities supporting domain-assigned UUID identifiers
 * and automated JPA auditing timestamps.
 *
 * @author Adiel Sanchez Santin
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class CrmAuditableAbstractPersistenceEntity implements Persistable<UUID> {

    @Id
    @Setter
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Setter
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Setter
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @jakarta.persistence.Version
    @Setter
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Setter
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Transient
    private boolean isNew = true;

    protected CrmAuditableAbstractPersistenceEntity() {
    }

    protected CrmAuditableAbstractPersistenceEntity(UUID id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrmAuditableAbstractPersistenceEntity that = (CrmAuditableAbstractPersistenceEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
