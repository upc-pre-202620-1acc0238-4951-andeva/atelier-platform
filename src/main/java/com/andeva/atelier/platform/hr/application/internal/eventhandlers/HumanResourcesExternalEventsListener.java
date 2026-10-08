package com.andeva.atelier.platform.hr.application.internal.eventhandlers;

import com.andeva.atelier.platform.hr.domain.repositories.EmployeeProfileRepository;
import com.andeva.atelier.platform.iam.interfaces.events.TenantMembershipCreatedIntegrationEvent;
import com.andeva.atelier.platform.operations.interfaces.events.WorkOrderCompletedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;

/**
 * @author Joel Huamani Estefanero
 */
@Component
public class HumanResourcesExternalEventsListener {

    private static final Logger log = LoggerFactory.getLogger(HumanResourcesExternalEventsListener.class);
    
    private final EmployeeProfileRepository employeeProfileRepository;
    public HumanResourcesExternalEventsListener(EmployeeProfileRepository employeeProfileRepository) {
        this.employeeProfileRepository = employeeProfileRepository;
    }

    @EventListener
    public void on(TenantMembershipCreatedIntegrationEvent event) {
        log.info("Received member creation for tenant {}, user {}, membership {}", event.tenantId(), event.userId(), event.membershipId());
        if (!employeeProfileRepository.existsByMembershipId(TenantMembershipId.of(event.membershipId()))) {
            log.info("Employee profile does not exist for membership {}", event.membershipId());
        }
    }

    @EventListener
    public void on(WorkOrderCompletedIntegrationEvent event) {
        log.info("Received work order completion for tenant {}, work order {}, total {}", event.tenantId(), event.workOrderId(), event.totalAmount());
    }
}
