package com.andeva.atelier.platform.crm.domain.model.queries;

import com.andeva.atelier.platform.crm.domain.model.enums.AppointmentStatus;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalDate;

/**
 * Lists the appointments of a branch on a calendar date. Status is optional.
 */
public record GetAppointmentsByTenantAndBranchQuery(
        TenantId tenantId,
        BranchId branchId,
        LocalDate date,
        AppointmentStatus status
) {
}
