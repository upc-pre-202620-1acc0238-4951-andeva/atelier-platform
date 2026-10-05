package com.andeva.atelier.platform.operations.domain.repositories;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;

import java.util.List;
import java.util.Optional;

public interface WorkBayRepository {

    WorkBay save(WorkBay workBay);

    Optional<WorkBay> findById(WorkBayId id);

    List<WorkBay> findByTenantIdAndBranchId(TenantId tenantId, BranchId branchId);

    List<WorkBay> findAvailableBays(TenantId tenantId, BranchId branchId, BayType bayType);
}
