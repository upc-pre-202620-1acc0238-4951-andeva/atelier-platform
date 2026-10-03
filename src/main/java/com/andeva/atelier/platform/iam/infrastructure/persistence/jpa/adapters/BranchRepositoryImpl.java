package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.repositories.BranchRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.BranchPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.TenantPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repository adapter implementing {@link BranchRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional
public class BranchRepositoryImpl implements BranchRepository {

    private final BranchPersistenceRepository branchPersistenceRepository;
    private final TenantPersistenceRepository tenantPersistenceRepository;

    public BranchRepositoryImpl(
            BranchPersistenceRepository branchPersistenceRepository,
            TenantPersistenceRepository tenantPersistenceRepository) {
        this.branchPersistenceRepository = branchPersistenceRepository;
        this.tenantPersistenceRepository = tenantPersistenceRepository;
    }

    @Override
    public Branch save(Branch branch) {
        Objects.requireNonNull(branch, "Branch cannot be null");
        TenantPersistenceEntity tenant = tenantPersistenceRepository.getReferenceById(branch.tenantId().value());
        BranchPersistenceEntity entity = BranchPersistenceAssembler.toEntity(branch, tenant);
        BranchPersistenceEntity saved = branchPersistenceRepository.save(entity);
        return BranchPersistenceAssembler.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Branch> findById(BranchId id) {
        if (id == null) {
            return Optional.empty();
        }
        return branchPersistenceRepository.findById(id.value())
                .map(BranchPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Branch> findByTenantId(TenantId tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }
        return branchPersistenceRepository.findByTenant_Id(tenantId.value())
                .stream()
                .map(BranchPersistenceAssembler::toDomain)
                .toList();
    }
}
