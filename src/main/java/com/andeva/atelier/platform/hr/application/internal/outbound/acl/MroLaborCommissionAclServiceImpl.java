package com.andeva.atelier.platform.hr.application.internal.outbound.acl;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class MroLaborCommissionAclServiceImpl implements MroLaborCommissionAclService {

    @Override
    public List<LaborCommissionDto> getAccruedCommissions(TenantId tenantId, TenantMembershipId membershipId, LocalDate start, LocalDate end) {
        return Collections.emptyList();
    }
}
