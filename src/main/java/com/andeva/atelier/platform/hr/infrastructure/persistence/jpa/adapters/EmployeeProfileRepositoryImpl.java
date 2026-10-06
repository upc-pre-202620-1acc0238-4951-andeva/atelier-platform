package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.enums.EmploymentStatus;
import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import com.andeva.atelier.platform.hr.domain.repositories.EmployeeProfileRepository;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.assemblers.EmployeeProfilePersistenceAssembler;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.EmployeeProfilePersistenceEntity;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.repositories.EmployeeProfilePersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class EmployeeProfileRepositoryImpl implements EmployeeProfileRepository {

    private final EmployeeProfilePersistenceRepository persistenceRepository;
    private final EmployeeProfilePersistenceAssembler assembler;
    private final ApplicationEventPublisher eventPublisher;

    public EmployeeProfileRepositoryImpl(
            EmployeeProfilePersistenceRepository persistenceRepository,
            EmployeeProfilePersistenceAssembler assembler,
            ApplicationEventPublisher eventPublisher
    ) {
        this.persistenceRepository = Objects.requireNonNull(persistenceRepository, "persistenceRepository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "assembler cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    @Override
    public Optional<EmployeeProfile> findById(EmployeeProfileId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<EmployeeProfile> findByMembershipId(TenantMembershipId membershipId) {
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        return persistenceRepository.findByMembershipId(membershipId.value()).map(assembler::toDomain);
    }

    @Override
    public List<EmployeeProfile> findAllByBranchId(BranchId branchId) {
        Objects.requireNonNull(branchId, "branchId cannot be null");
        return persistenceRepository.findAllByBranchId(branchId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public List<EmployeeProfile> findAllActiveByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return persistenceRepository.findAllByTenantIdAndEmploymentStatus(tenantId.value(), EmploymentStatus.ACTIVE)
                .stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public boolean existsByMembershipId(TenantMembershipId membershipId) {
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        return persistenceRepository.existsByMembershipId(membershipId.value());
    }

    @Override
    public EmployeeProfile save(EmployeeProfile profile) {
        Objects.requireNonNull(profile, "profile cannot be null");
        EmployeeProfilePersistenceEntity entity = persistenceRepository.findById(profile.getId().value())
                .map(existing -> {
                    assembler.updateEntity(existing, profile);
                    return existing;
                })
                .orElseGet(() -> assembler.toEntity(profile));

        EmployeeProfilePersistenceEntity saved = persistenceRepository.save(entity);
        EmployeeProfile domain = assembler.toDomain(saved);

        profile.domainEvents().forEach(eventPublisher::publishEvent);
        profile.clearDomainEvents();

        return domain;
    }
}
