package com.andeva.atelier.platform.crm.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;

/**
 * Query for retrieving appointments within an explicit timestamp range.
 *
 * @author Adiel Sanchez Santin
 */
public record GetAppointmentsByDateRangeQuery(
        TenantId tenantId,
        BranchId branchId,
        Instant startDate,
        Instant endDate
) {}
