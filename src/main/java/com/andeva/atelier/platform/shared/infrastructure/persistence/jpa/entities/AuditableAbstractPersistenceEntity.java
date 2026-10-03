package com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract JPA persistence superclass that provides all relational entities in the system
 * with a universal technical UUID primary key and automated auditing timestamps managed
 * by Spring Data JPA Auditing.
 * Architectural Decoupling:
 * This class belongs exclusively to the Infrastructure Layer. Pure Domain aggregates and
 * entities NEVER extend this class; only dedicated persistence entities (*PersistenceEntity)
 * inherit from it to keep domain logic strictly agnostic of ORM concerns.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAbstractPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    protected AuditableAbstractPersistenceEntity() {
    }

    protected AuditableAbstractPersistenceEntity(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditableAbstractPersistenceEntity that = (AuditableAbstractPersistenceEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
