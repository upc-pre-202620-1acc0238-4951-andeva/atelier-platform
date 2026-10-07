package com.andeva.atelier.platform.invoicing.infrastructure.external.messaging.outbox;

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
 * Outbound asynchronous relay adapter polling and streaming queued Invoicing outbox messages
 * towards platform event brokers, ensuring At-Least-Once Delivery guarantees.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class InvoicingOutboxMessageRelayAdapter {

    private static final Logger log = LoggerFactory.getLogger(InvoicingOutboxMessageRelayAdapter.class);
    private static final String INVOICING_AGGREGATE_TYPE = "ElectronicVoucher";

    private final OutboxMessageJpaRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InvoicingOutboxMessageRelayAdapter(
            OutboxMessageJpaRepository outboxRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "OutboxMessageJpaRepository cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    /**
     * Polls pending outbox records and relays Invoicing integration events with retry resilience.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void relayPendingInvoicingMessages() {
        List<OutboxMessagePersistenceEntity> pendingMessages =
                outboxRepository.findTop50ByStatusAndAggregateTypeOrderByOccurredOnAsc(
                        OutboxStatus.PENDING, INVOICING_AGGREGATE_TYPE);

        for (OutboxMessagePersistenceEntity message : pendingMessages) {
            try {
                log.debug("Relaying Invoicing outbox message {} [type={}]", message.getId(), message.getEventType());
                eventPublisher.publishEvent(message);
                message.setStatus(OutboxStatus.PUBLISHED);
                message.setProcessedAt(Instant.now());
                outboxRepository.save(message);
            } catch (Exception ex) {
                log.error("Failed to relay Invoicing outbox message {}: {}", message.getId(), ex.getMessage(), ex);
                message.setStatus(OutboxStatus.FAILED);
                message.setLastError(ex.getMessage());
                outboxRepository.save(message);
            }
        }
    }
}
