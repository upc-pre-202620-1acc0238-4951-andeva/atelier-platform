package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.TenantMembershipPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantMembershipPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.RolePersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.TenantMembershipPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.TenantPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repository adapter implementing {@link TenantMembershipRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional
public class TenantMembershipRepositoryImpl implements TenantMembershipRepository {

    private final TenantMembershipPersistenceRepository membershipPersistenceRepository;
    private final TenantPersistenceRepository tenantPersistenceRepository;
    private final UserPersistenceRepository userPersistenceRepository;
    private final RolePersistenceRepository rolePersistenceRepository;

    public TenantMembershipRepositoryImpl(
            TenantMembershipPersistenceRepository membershipPersistenceRepository,
            TenantPersistenceRepository tenantPersistenceRepository,
            UserPersistenceRepository userPersistenceRepository,
            RolePersistenceRepository rolePersistenceRepository) {
        this.membershipPersistenceRepository = membershipPersistenceRepository;
        this.tenantPersistenceRepository = tenantPersistenceRepository;
        this.userPersistenceRepository = userPersistenceRepository;
        this.rolePersistenceRepository = rolePersistenceRepository;
    }

    @Override
    public TenantMembership save(TenantMembership membership) {
        Objects.requireNonNull(membership, "TenantMembership cannot be null");
        TenantPersistenceEntity tenant = tenantPersistenceRepository.findById(membership.tenantId().value())
                .orElseGet(() -> tenantPersistenceRepository.getReferenceById(membership.tenantId().value()));
        UserPersistenceEntity user = userPersistenceRepository.findById(membership.userId().value())
                .orElseGet(() -> userPersistenceRepository.getReferenceById(membership.userId().value()));

        List<UUID> roleUuids = membership.assignedRoles() != null
                ? membership.assignedRoles().stream().map(r -> r.id().value()).toList()
                : Collections.emptyList();
        Set<RolePersistenceEntity> roles = roleUuids.isEmpty()
                ? new HashSet<>()
                : new HashSet<>(rolePersistenceRepository.findAllById(roleUuids));

        TenantMembershipPersistenceEntity entity = TenantMembershipPersistenceAssembler.toEntity(
                membership,
                tenant,
                user,
                roles
        );
        TenantMembershipPersistenceEntity saved = membershipPersistenceRepository.save(entity);
        return TenantMembershipPersistenceAssembler.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenantMembership> findById(TenantMembershipId id) {
        if (id == null) {
            return Optional.empty();
        }
        return membershipPersistenceRepository.findById(id.value())
                .map(TenantMembershipPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenantMembership> findByTenantIdAndUserId(TenantId tenantId, UserId userId) {
        if (tenantId == null || userId == null) {
            return Optional.empty();
        }
        return membershipPersistenceRepository.findByTenant_IdAndUser_Id(tenantId.value(), userId.value())
                .map(TenantMembershipPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantMembership> findByTenantId(TenantId tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }
        return membershipPersistenceRepository.findByTenant_Id(tenantId.value())
                .stream()
                .map(TenantMembershipPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantMembership> findByUserId(UserId userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return membershipPersistenceRepository.findByUser_Id(userId.value())
                .stream()
                .map(TenantMembershipPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTenantId(TenantId tenantId) {
        if (tenantId == null) {
            return 0;
        }
        return membershipPersistenceRepository.countByTenant_Id(tenantId.value());
    }
}
