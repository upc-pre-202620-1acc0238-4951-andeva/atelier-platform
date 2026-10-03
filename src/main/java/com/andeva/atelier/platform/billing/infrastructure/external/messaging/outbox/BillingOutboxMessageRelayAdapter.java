package com.andeva.atelier.platform.billing.infrastructure.external.messaging.outbox;

import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxStatus;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Outbound asynchronous relay adapter polling and streaming queued billing outbox messages
 * towards platform event brokers, ensuring At-Least-Once Delivery guarantees.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class BillingOutboxMessageRelayAdapter {

    private static final Logger log = LoggerFactory.getLogger(BillingOutboxMessageRelayAdapter.class);
    private static final String BILLING_AGGREGATE_TYPE = "TenantSubscription";

    private final OutboxMessageJpaRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BillingOutboxMessageRelayAdapter(
            OutboxMessageJpaRepository outboxRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "OutboxMessageJpaRepository cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    /**
     * Polls pending outbox records and relays billing domain events with retry resilience.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void relayPendingBillingMessages() {
        List<OutboxMessagePersistenceEntity> pendingMessages =
                outboxRepository.findTop50ByStatusAndAggregateTypeOrderByOccurredOnAsc(
                        OutboxStatus.PENDING, BILLING_AGGREGATE_TYPE);

        for (OutboxMessagePersistenceEntity message : pendingMessages) {
            try {
                log.debug("Relaying billing outbox message {} [type={}]", message.getId(), message.getEventType());
                eventPublisher.publishEvent(message);
                message.setStatus(OutboxStatus.PUBLISHED);
                message.setProcessedAt(Instant.now());
                outboxRepository.save(message);
            } catch (Exception ex) {
                log.error("Failed to relay billing outbox message {}: {}", message.getId(), ex.getMessage(), ex);
                message.setStatus(OutboxStatus.FAILED);
                message.setLastError(ex.getMessage());
                outboxRepository.save(message);
            }
        }
    }
}
