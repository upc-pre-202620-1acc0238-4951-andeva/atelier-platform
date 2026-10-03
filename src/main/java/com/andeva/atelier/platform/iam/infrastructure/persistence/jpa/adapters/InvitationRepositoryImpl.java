package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.iam.domain.repositories.InvitationRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.InvitationPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.InvitationPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.InvitationPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.TenantPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repository adapter implementing {@link InvitationRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional
public class InvitationRepositoryImpl implements InvitationRepository {

    private final InvitationPersistenceRepository invitationPersistenceRepository;
    private final TenantPersistenceRepository tenantPersistenceRepository;

    public InvitationRepositoryImpl(
            InvitationPersistenceRepository invitationPersistenceRepository,
            TenantPersistenceRepository tenantPersistenceRepository) {
        this.invitationPersistenceRepository = invitationPersistenceRepository;
        this.tenantPersistenceRepository = tenantPersistenceRepository;
    }

    @Override
    public Invitation save(Invitation invitation) {
        Objects.requireNonNull(invitation, "Invitation cannot be null");
        TenantPersistenceEntity tenant = tenantPersistenceRepository.getReferenceById(invitation.tenantId().value());
        InvitationPersistenceEntity entity = InvitationPersistenceAssembler.toEntity(invitation, tenant);
        InvitationPersistenceEntity saved = invitationPersistenceRepository.save(entity);
        return InvitationPersistenceAssembler.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invitation> findById(InvitationId id) {
        if (id == null) {
            return Optional.empty();
        }
        return invitationPersistenceRepository.findById(id.value())
                .map(InvitationPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invitation> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return invitationPersistenceRepository.findByToken(token.trim())
                .map(InvitationPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invitation> findByTenantIdAndEmail(TenantId tenantId, EmailAddress email) {
        if (tenantId == null || email == null) {
            return Optional.empty();
        }
        return invitationPersistenceRepository.findByTenant_IdAndEmail(tenantId.value(), email.value())
                .map(InvitationPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invitation> findByTenantId(TenantId tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }
        return invitationPersistenceRepository.findByTenant_Id(tenantId.value())
                .stream()
                .map(InvitationPersistenceAssembler::toDomain)
                .toList();
    }
}
