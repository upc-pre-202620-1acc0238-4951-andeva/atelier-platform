package com.andeva.atelier.platform.operations.infrastructure.external.acl.hr;

import com.andeva.atelier.platform.hr.interfaces.acl.HumanResourcesContextFacade;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkforceShiftAclService;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Anti-Corruption Layer adapter reading the mechanic's scheduled shift from Human Resources.
 */
@Component
public class WorkforceShiftAclAdapter implements WorkforceShiftAclService {

    private final HumanResourcesContextFacade humanResourcesContextFacade;

    public WorkforceShiftAclAdapter(HumanResourcesContextFacade humanResourcesContextFacade) {
        this.humanResourcesContextFacade = Objects.requireNonNull(humanResourcesContextFacade, "humanResourcesContextFacade cannot be null");
    }

    @Override
    public Optional<ShiftWindow> findShift(UUID tenantId, UUID mechanicMembershipId) {
        if (tenantId == null || mechanicMembershipId == null) {
            return Optional.empty();
        }
        return humanResourcesContextFacade.getEmployeeWorkShift(tenantId, mechanicMembershipId)
                .map(shift -> new ShiftWindow(shift.startTime(), shift.endTime()));
    }
}
