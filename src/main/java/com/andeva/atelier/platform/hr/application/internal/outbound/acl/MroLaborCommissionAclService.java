package com.andeva.atelier.platform.hr.application.internal.outbound.acl;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public interface MroLaborCommissionAclService {
    List<LaborCommissionDto> getAccruedCommissions(TenantId tenantId, TenantMembershipId membershipId, LocalDate start, LocalDate end);

    record LaborCommissionDto(String workOrderNumber, String taskDescription, Money commissionAmount, LocalDate completedDate) {}
}
