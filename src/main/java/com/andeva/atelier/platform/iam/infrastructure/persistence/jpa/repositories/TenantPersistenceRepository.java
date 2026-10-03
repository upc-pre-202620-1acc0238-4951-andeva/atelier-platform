package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link TenantPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface TenantPersistenceRepository extends JpaRepository<TenantPersistenceEntity, UUID> {

    Optional<TenantPersistenceEntity> findByTaxId(String taxId);

    boolean existsByTaxId(String taxId);
}
