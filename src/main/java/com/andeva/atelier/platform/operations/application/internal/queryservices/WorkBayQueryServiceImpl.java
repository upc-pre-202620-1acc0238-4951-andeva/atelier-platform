package com.andeva.atelier.platform.operations.application.internal.queryservices;

import com.andeva.atelier.platform.operations.application.queryservices.WorkBayQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetAvailableWorkBaysQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkBaysByBranchIdQuery;
import com.andeva.atelier.platform.operations.domain.repositories.WorkBayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class WorkBayQueryServiceImpl implements WorkBayQueryService {

    private final WorkBayRepository workBayRepository;

    public WorkBayQueryServiceImpl(WorkBayRepository workBayRepository) {
        this.workBayRepository = Objects.requireNonNull(workBayRepository);
    }

    @Override
    public List<WorkBay> handle(GetWorkBaysByBranchIdQuery query) {
        if (query == null || query.tenantId() == null || query.branchId() == null) return List.of();
        return workBayRepository.findByTenantIdAndBranchId(query.tenantId(), query.branchId());
    }

    @Override
    public List<WorkBay> handle(GetAvailableWorkBaysQuery query) {
        if (query == null || query.tenantId() == null || query.branchId() == null) return List.of();
        return workBayRepository.findAvailableBays(query.tenantId(), query.branchId(), query.bayType());
    }

    @Override
    public Optional<WorkBay> getById(WorkBayId bayId) {
        if (bayId == null) return Optional.empty();
        return workBayRepository.findById(bayId);
    }
}
