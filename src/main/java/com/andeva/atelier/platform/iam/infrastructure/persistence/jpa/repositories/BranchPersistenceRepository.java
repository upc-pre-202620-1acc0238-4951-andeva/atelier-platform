package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link BranchPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface BranchPersistenceRepository extends JpaRepository<BranchPersistenceEntity, UUID> {

    List<BranchPersistenceEntity> findByTenant_Id(UUID tenantId);
}
