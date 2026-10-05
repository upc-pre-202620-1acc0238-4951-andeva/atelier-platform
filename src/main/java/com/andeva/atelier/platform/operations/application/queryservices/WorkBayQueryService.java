package com.andeva.atelier.platform.operations.application.queryservices;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetAvailableWorkBaysQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkBaysByBranchIdQuery;

import java.util.List;
import java.util.Optional;

public interface WorkBayQueryService {

    List<WorkBay> handle(GetWorkBaysByBranchIdQuery query);

    List<WorkBay> handle(GetAvailableWorkBaysQuery query);

    Optional<WorkBay> getById(WorkBayId bayId);
}
