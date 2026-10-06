package com.andeva.atelier.platform.hr.application.queryservices;

import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.queries.GetWorkShiftByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListWorkShiftsByTenantQuery;

import java.util.List;
import java.util.Optional;

public interface WorkShiftQueryService {
    Optional<WorkShift> handle(GetWorkShiftByIdQuery query);
    List<WorkShift> handle(ListWorkShiftsByTenantQuery query);
}
