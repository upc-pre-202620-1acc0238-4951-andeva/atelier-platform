package com.andeva.atelier.platform.hr.application.internal.queryservices;

import com.andeva.atelier.platform.hr.application.queryservices.WorkShiftQueryService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.queries.GetWorkShiftByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListWorkShiftsByTenantQuery;
import com.andeva.atelier.platform.hr.domain.repositories.WorkShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class WorkShiftQueryServiceImpl implements WorkShiftQueryService {

    private final WorkShiftRepository workShiftRepository;

    public WorkShiftQueryServiceImpl(WorkShiftRepository workShiftRepository) {
        this.workShiftRepository = Objects.requireNonNull(workShiftRepository, "workShiftRepository cannot be null");
    }

    @Override
    public Optional<WorkShift> handle(GetWorkShiftByIdQuery query) {
        return workShiftRepository.findById(query.shiftId())
                .filter(s -> s.getTenantId().equals(query.tenantId()));
    }

    @Override
    public List<WorkShift> handle(ListWorkShiftsByTenantQuery query) {
        return workShiftRepository.findAllByTenantId(query.tenantId());
    }
}
