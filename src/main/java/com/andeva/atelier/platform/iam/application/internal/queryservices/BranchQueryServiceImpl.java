package com.andeva.atelier.platform.iam.application.internal.queryservices;

import com.andeva.atelier.platform.iam.application.queryservices.BranchQueryService;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchesByTenantIdQuery;
import com.andeva.atelier.platform.iam.domain.repositories.BranchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Transactional read-only query service implementation for physical Branches.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class BranchQueryServiceImpl implements BranchQueryService {

    private final BranchRepository branchRepository;

    public BranchQueryServiceImpl(BranchRepository branchRepository) {
        this.branchRepository = Objects.requireNonNull(branchRepository, "BranchRepository cannot be null");
    }

    @Override
    public Optional<Branch> handle(GetBranchByIdQuery query) {
        Objects.requireNonNull(query, "GetBranchByIdQuery cannot be null");
        return branchRepository.findById(query.branchId());
    }

    @Override
    public List<Branch> handle(GetBranchesByTenantIdQuery query) {
        Objects.requireNonNull(query, "GetBranchesByTenantIdQuery cannot be null");
        return branchRepository.findByTenantId(query.tenantId());
    }
}
