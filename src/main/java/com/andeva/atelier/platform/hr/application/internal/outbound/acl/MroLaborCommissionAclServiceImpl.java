package com.andeva.atelier.platform.hr.application.internal.outbound.acl;

import com.andeva.atelier.platform.operations.interfaces.acl.WorkshopOperationsContextFacade;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * @author Joel Huamani Estefanero
 */
@Service
public class MroLaborCommissionAclServiceImpl implements MroLaborCommissionAclService {

    private final WorkshopOperationsContextFacade workshopOperationsContextFacade;

    @Autowired
    public MroLaborCommissionAclServiceImpl(@Autowired(required = false) WorkshopOperationsContextFacade workshopOperationsContextFacade) {
        this.workshopOperationsContextFacade = workshopOperationsContextFacade;
    }

    public MroLaborCommissionAclServiceImpl() {
        this(null);
    }

    @Override
    public List<LaborCommissionDto> getAccruedCommissions(TenantId tenantId, TenantMembershipId membershipId, LocalDate start, LocalDate end) {
        if (workshopOperationsContextFacade != null && tenantId != null && membershipId != null) {
            // Graceful integration with operations facade
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
