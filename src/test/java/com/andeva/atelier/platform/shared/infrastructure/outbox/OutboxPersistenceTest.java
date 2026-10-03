package com.andeva.atelier.platform.shared.infrastructure.outbox;

import com.andeva.atelier.platform.shared.domain.events.DomainEvent;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxStatus;
import com.andeva.atelier.platform.shared.infrastructure.outbox.publisher.JpaDomainEventPublisher;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for Transactional Outbox persistence entity and JPA event publisher.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Transactional Outbox Persistence and Publisher Unit Tests")
class OutboxPersistenceTest {

    record SampleDomainEvent(
            UUID eventId,
            Instant occurredOn,
            String aggregateId,
            String eventType,
            String description
    ) implements DomainEvent {}

    @Nested
    @DisplayName("OutboxMessagePersistenceEntity Tests")
    class EntityTests {

        @Test
        @DisplayName("Should create pending outbox entity with pendingOf factory")
        void shouldCreatePendingEntity() {
            Instant now = Instant.now();
            OutboxMessagePersistenceEntity entity = OutboxMessagePersistenceEntity.pendingOf(
                    "com.andeva.atelier.workshop",
                    "ORD-999",
                    "WorkOrderCreated",
                    "{\"orderId\":\"ORD-999\"}",
                    now
            );

            assertThat(entity.getAggregateType()).isEqualTo("com.andeva.atelier.workshop");
            assertThat(entity.getAggregateId()).isEqualTo("ORD-999");
            assertThat(entity.getEventType()).isEqualTo("WorkOrderCreated");
            assertThat(entity.getPayload()).isEqualTo("{\"orderId\":\"ORD-999\"}");
            assertThat(entity.getOccurredOn()).isEqualTo(now);
            assertThat(entity.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(entity.getRetryCount()).isEqualTo(0);
            assertThat(entity.getLastError()).isNull();
            assertThat(entity.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("Should reject null fields in pendingOf factory")
        void shouldRejectNullFields() {
            Instant now = Instant.now();
            assertThatThrownBy(() -> OutboxMessagePersistenceEntity.pendingOf(null, "ID", "Type", "{}", now))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> OutboxMessagePersistenceEntity.pendingOf("Type", null, "Type", "{}", now))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> OutboxMessagePersistenceEntity.pendingOf("Type", "ID", null, "{}", now))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> OutboxMessagePersistenceEntity.pendingOf("Type", "ID", "Type", null, now))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> OutboxMessagePersistenceEntity.pendingOf("Type", "ID", "Type", "{}", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("JpaDomainEventPublisher Tests")
    class PublisherTests {

        @Mock
        private OutboxMessageJpaRepository outboxRepository;

        @Mock
        private ObjectMapper objectMapper;

        @Mock
        private ApplicationEventPublisher applicationEventPublisher;

        @InjectMocks
        private JpaDomainEventPublisher domainEventPublisher;

        @Test
        @DisplayName("Should serialize and persist domain event to outbox repository and publish locally")
        void shouldPublishDomainEvent() throws Exception {
            UUID eventId = UUID.randomUUID();
            Instant now = Instant.now();
            SampleDomainEvent event = new SampleDomainEvent(eventId, now, "TEN-123", "TenantCreated", "Workshop Onboarded");

            when(objectMapper.writeValueAsString(event)).thenReturn("{\"eventId\":\"" + eventId + "\"}");

            domainEventPublisher.publish(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor =
                    ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateId()).isEqualTo("TEN-123");
            assertThat(saved.getEventType()).isEqualTo("TenantCreated");
            assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(saved.getPayload()).isEqualTo("{\"eventId\":\"" + eventId + "\"}");
            assertThat(saved.getOccurredOn()).isEqualTo(now);

            verify(applicationEventPublisher).publishEvent(event);
        }

        @Test
        @DisplayName("Should publish collection of events via publishAll")
        void shouldPublishAllEvents() throws Exception {
            SampleDomainEvent event1 = new SampleDomainEvent(UUID.randomUUID(), Instant.now(), "1", "E1", "Desc1");
            SampleDomainEvent event2 = new SampleDomainEvent(UUID.randomUUID(), Instant.now(), "2", "E2", "Desc2");
            String plainMessage = "Non-domain notification";

            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            domainEventPublisher.publishAll(List.of(event1, event2, plainMessage));

            verify(outboxRepository, org.mockito.Mockito.times(2)).save(any());
            verify(applicationEventPublisher).publishEvent(event1);
            verify(applicationEventPublisher).publishEvent(event2);
            verify(applicationEventPublisher).publishEvent(plainMessage);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when ObjectMapper serialization fails")
        void shouldThrowWhenSerializationFails() throws Exception {
            SampleDomainEvent event = new SampleDomainEvent(UUID.randomUUID(), Instant.now(), "1", "E1", "Desc");
            when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("Serialization error") {});

            assertThatThrownBy(() -> domainEventPublisher.publish(event))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Failed to serialize domain event for outbox");
        }

        @Test
        @DisplayName("Should reject null event in publish")
        void shouldRejectNullEvent() {
            assertThatThrownBy(() -> domainEventPublisher.publish(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Domain event cannot be null");
        }
    }
}
