package com.andeva.atelier.platform.shared.application.handlers;

import com.andeva.atelier.platform.shared.application.events.DomainEventPublisher;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.events.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for CQRS functional contracts and event dispatcher port.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("CQRS Handlers and Event Publisher Unit Tests")
class CqrsHandlersTest {

    record SampleCommand(String name) {}
    record SampleQuery(UUID id) {}
    record SampleDto(UUID id, String name) {}

    record TestDomainEvent(
            UUID eventId,
            Instant occurredOn,
            String aggregateId,
            String eventType,
            String payload
    ) implements DomainEvent {
        public static TestDomainEvent create(String aggregateId, String payload) {
            return new TestDomainEvent(UUID.randomUUID(), Instant.now(), aggregateId, "TestDomainEvent", payload);
        }
    }

    @Test
    @DisplayName("CommandHandler should process command and return Success")
    void commandHandlerShouldReturnSuccess() {
        CommandHandler<SampleCommand, UUID> handler = cmd -> {
            if (cmd.name() == null || cmd.name().isBlank()) {
                return Result.failure(ApplicationError.badRequest("Name cannot be empty"));
            }
            return Result.success(UUID.randomUUID());
        };

        Result<UUID, ApplicationError> result = handler.handle(new SampleCommand("New Workshop"));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional()).isPresent();
    }

    @Test
    @DisplayName("CommandHandler should return Failure when business validation fails")
    void commandHandlerShouldReturnFailure() {
        CommandHandler<SampleCommand, UUID> handler = cmd -> {
            if (cmd.name() == null || cmd.name().isBlank()) {
                return Result.failure(ApplicationError.badRequest("Name cannot be empty"));
            }
            return Result.success(UUID.randomUUID());
        };

        Result<UUID, ApplicationError> result = handler.handle(new SampleCommand(""));
        assertThat(result.isFailure()).isTrue();
        assertThat(result.toErrorOptional()).isPresent();
        assertThat(result.toErrorOptional().get().code()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("VoidCommandHandler should process command and return Void Success")
    void voidCommandHandlerShouldReturnSuccess() {
        AtomicBoolean deleted = new AtomicBoolean(false);
        VoidCommandHandler<UUID> handler = id -> {
            deleted.set(true);
            return Result.empty();
        };

        Result<Void, ApplicationError> result = handler.handle(UUID.randomUUID());
        assertThat(result.isSuccess()).isTrue();
        assertThat(deleted.get()).isTrue();
    }

    @Test
    @DisplayName("VoidCommandHandler should return Failure when operation cannot proceed")
    void voidCommandHandlerShouldReturnFailure() {
        VoidCommandHandler<UUID> handler = id -> Result.failure(ApplicationError.notFound("Resource", id));

        UUID id = UUID.randomUUID();
        Result<Void, ApplicationError> result = handler.handle(id);
        assertThat(result.isFailure()).isTrue();
        assertThat(result.toErrorOptional().get().code()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("QueryHandler should process query and return DTO projection")
    void queryHandlerShouldReturnProjection() {
        UUID targetId = UUID.randomUUID();
        QueryHandler<SampleQuery, SampleDto> handler = qry -> {
            if (qry.id().equals(targetId)) {
                return Result.success(new SampleDto(targetId, "Active Workshop"));
            }
            return Result.failure(ApplicationError.notFound("Workshop", qry.id()));
        };

        Result<SampleDto, ApplicationError> successResult = handler.handle(new SampleQuery(targetId));
        assertThat(successResult.isSuccess()).isTrue();
        assertThat(successResult.toOptional().get().name()).isEqualTo("Active Workshop");

        Result<SampleDto, ApplicationError> failureResult = handler.handle(new SampleQuery(UUID.randomUUID()));
        assertThat(failureResult.isFailure()).isTrue();
        assertThat(failureResult.toErrorOptional().get().code()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("DomainEventHandler should receive and handle domain event")
    void domainEventHandlerShouldHandleEvent() {
        AtomicReference<TestDomainEvent> received = new AtomicReference<>();
        DomainEventHandler<TestDomainEvent> eventHandler = received::set;

        TestDomainEvent event = TestDomainEvent.create("AGG-101", "OrderPlaced");
        eventHandler.handle(event);

        assertThat(received.get()).isNotNull();
        assertThat(received.get().aggregateId()).isEqualTo("AGG-101");
        assertThat(received.get().payload()).isEqualTo("OrderPlaced");
    }

    @Test
    @DisplayName("DomainEventPublisher port contract can publish single and multiple events")
    void domainEventPublisherContractShouldDispatchEvents() {
        List<Object> publishedEvents = new ArrayList<>();

        DomainEventPublisher publisher = new DomainEventPublisher() {
            @Override
            public void publish(DomainEvent event) {
                publishedEvents.add(event);
            }

            @Override
            public void publishAll(Collection<Object> events) {
                publishedEvents.addAll(events);
            }
        };

        TestDomainEvent event1 = TestDomainEvent.create("AGG-1", "Created");
        TestDomainEvent event2 = TestDomainEvent.create("AGG-2", "Updated");

        publisher.publish(event1);
        publisher.publishAll(List.of(event2));

        assertThat(publishedEvents).containsExactly(event1, event2);
    }
}
