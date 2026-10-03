package com.andeva.atelier.platform.shared.domain.model.aggregates;

import com.andeva.atelier.platform.shared.domain.events.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for {@link AbstractDomainAggregateRoot} lifecycle and event accumulation.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("AbstractDomainAggregateRoot Tests")
class AbstractDomainAggregateRootTest {

    record SampleDomainEvent(UUID eventId, Instant occurredOn, String aggregateId, String eventType) implements DomainEvent {}

    static class TestAggregate extends AbstractDomainAggregateRoot<TestAggregate> {
        public void performBusinessAction() {
            registerDomainEvent(new SampleDomainEvent(
                    UUID.randomUUID(),
                    Instant.now(),
                    "AGG-123",
                    "SampleEventOccurred"
            ));
        }

        public void registerCustomEvent(Object event) {
            registerDomainEvent(event);
        }
    }

    @Nested
    @DisplayName("Domain Event Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("Should initialize with empty domain events collection")
        void shouldInitiallyHaveEmptyDomainEvents() {
            TestAggregate aggregate = new TestAggregate();
            assertThat(aggregate.domainEvents()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should register, preserve order, and retrieve domain events")
        void shouldRegisterAndAccumulateMultipleDomainEvents() {
            TestAggregate aggregate = new TestAggregate();
            SampleDomainEvent event1 = new SampleDomainEvent(UUID.randomUUID(), Instant.now(), "AGG-1", "Created");
            SampleDomainEvent event2 = new SampleDomainEvent(UUID.randomUUID(), Instant.now(), "AGG-1", "Updated");

            aggregate.registerCustomEvent(event1);
            aggregate.registerCustomEvent(event2);

            assertThat(aggregate.domainEvents())
                    .hasSize(2)
                    .containsExactly(event1, event2);
        }

        @Test
        @DisplayName("Should throw NullPointerException when registering null domain event")
        void shouldThrowWhenRegisteringNullEvent() {
            TestAggregate aggregate = new TestAggregate();
            assertThatThrownBy(() -> aggregate.registerCustomEvent(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Domain event to register cannot be null");
        }

        @Test
        @DisplayName("Should enforce unmodifiable collection for domainEvents")
        void shouldEnforceUnmodifiableDomainEventsCollection() {
            TestAggregate aggregate = new TestAggregate();
            aggregate.performBusinessAction();

            var events = aggregate.domainEvents();
            assertThat(events).hasSize(1);

            SampleDomainEvent externalEvent = new SampleDomainEvent(
                    UUID.randomUUID(), Instant.now(), "AGG-999", "External");

            assertThatThrownBy(() -> events.add(externalEvent))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> events.remove(events.iterator().next()))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(events::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should clear registered domain events and handle multiple clear calls idempotently")
        void shouldClearDomainEventsCorrectly() {
            TestAggregate aggregate = new TestAggregate();
            aggregate.performBusinessAction();
            assertThat(aggregate.domainEvents()).hasSize(1);

            aggregate.clearDomainEvents();
            assertThat(aggregate.domainEvents()).isEmpty();

            // Calling clear again on already empty collection should not throw
            aggregate.clearDomainEvents();
            assertThat(aggregate.domainEvents()).isEmpty();
        }
    }
}
