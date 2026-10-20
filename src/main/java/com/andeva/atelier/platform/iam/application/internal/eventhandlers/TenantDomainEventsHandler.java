package com.andeva.atelier.platform.iam.application.internal.eventhandlers;

import com.andeva.atelier.platform.iam.application.internal.outbound.acl.ResendEmailService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.events.StaffInvitedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantRegisteredEvent;
import com.andeva.atelier.platform.iam.domain.repositories.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

/**
 * Application event listener orchestrating tenant communications and integration audit upon domain events.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class TenantDomainEventsHandler {

    private static final Logger log = LoggerFactory.getLogger(TenantDomainEventsHandler.class);

    private final ResendEmailService resendEmailService;
    private final TenantRepository tenantRepository;

    public TenantDomainEventsHandler(ResendEmailService resendEmailService, TenantRepository tenantRepository) {
        this.resendEmailService = Objects.requireNonNull(resendEmailService, "ResendEmailService cannot be null");
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository cannot be null");
    }

    @EventListener
    @Async
    public void on(StaffInvitedEvent event) {
        log.info("Processing StaffInvitedEvent for email: {}", event.email().value());
        String tenantName = tenantRepository.findById(event.tenantId())
                .map(Tenant::name)
                .orElse("Atelier Workshop");

        resendEmailService.sendStaffInvitationEmail(event.email(), tenantName, event.token());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TenantRegisteredEvent event) {
        log.info("Tenant successfully registered and committed to persistence: id={}, name={}",
                event.tenantId(), event.name());
    }
}
