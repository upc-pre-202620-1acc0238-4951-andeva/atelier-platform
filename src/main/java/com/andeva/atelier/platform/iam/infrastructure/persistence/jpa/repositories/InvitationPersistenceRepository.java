package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.InvitationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link InvitationPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface InvitationPersistenceRepository extends JpaRepository<InvitationPersistenceEntity, UUID> {

    Optional<InvitationPersistenceEntity> findByToken(String token);

    Optional<InvitationPersistenceEntity> findByTenant_IdAndEmail(UUID tenantId, String email);

    List<InvitationPersistenceEntity> findByTenant_Id(UUID tenantId);
}
