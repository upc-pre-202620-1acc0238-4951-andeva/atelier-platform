package com.andeva.atelier.platform.operations.application.eventhandlers;

import com.andeva.atelier.platform.operations.application.internal.eventhandlers.OperationsTransactionalOutboxPublisher;
import com.andeva.atelier.platform.operations.application.internal.eventhandlers.WorkOrderDomainEventsHandler;
import com.andeva.atelier.platform.operations.application.internal.eventhandlers.WorkOrderTaskDomainEventsHandler;
import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.domain.model.events.*;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.interfaces.events.*;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive Unit Tests for Workshop Operations Event Handlers and Transactional Outbox Publisher.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Workshop Operations Event Handlers & Outbox Publisher Unit Tests")
class OperationsEventHandlersTest {

    @Nested
    @DisplayName("OperationsTransactionalOutboxPublisher Tests")
    class OutboxPublisherTests {

        @Mock
        private OutboxMessageJpaRepository outboxRepository;

        private OperationsTransactionalOutboxPublisher publisher;

        @BeforeEach
        void setUp() {
            publisher = new OperationsTransactionalOutboxPublisher(outboxRepository, new ObjectMapper());
        }

        @Test
        @DisplayName("Should persist WorkOrderCreatedIntegrationEvent into outbox")
        void shouldPersistWorkOrderCreatedIntegrationEvent() {
            UUID orderId = UUID.randomUUID();
            WorkOrderCreatedIntegrationEvent event = new WorkOrderCreatedIntegrationEvent(
                    orderId,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    101,
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("WorkOrder");
            assertThat(saved.getAggregateId()).isEqualTo(orderId.toString());
            assertThat(saved.getEventType()).isEqualTo("WorkOrderCreatedIntegrationEvent");
        }

        @Test
        @DisplayName("Should persist WorkOrderBayAssignedIntegrationEvent into outbox")
        void shouldPersistWorkOrderBayAssignedIntegrationEvent() {
            UUID orderId = UUID.randomUUID();
            WorkOrderBayAssignedIntegrationEvent event = new WorkOrderBayAssignedIntegrationEvent(
                    orderId,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Bay 1",
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("WorkOrder");
            assertThat(saved.getAggregateId()).isEqualTo(orderId.toString());
            assertThat(saved.getEventType()).isEqualTo("WorkOrderBayAssignedIntegrationEvent");
        }

        @Test
        @DisplayName("Should persist WorkOrderStartedIntegrationEvent into outbox")
        void shouldPersistWorkOrderStartedIntegrationEvent() {
            UUID orderId = UUID.randomUUID();
            WorkOrderStartedIntegrationEvent event = new WorkOrderStartedIntegrationEvent(
                    orderId,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("WorkOrder");
            assertThat(saved.getAggregateId()).isEqualTo(orderId.toString());
            assertThat(saved.getEventType()).isEqualTo("WorkOrderStartedIntegrationEvent");
        }

        @Test
        @DisplayName("Should persist ProductStockReservationRequestedIntegrationEvent into outbox")
        void shouldPersistProductStockReservationRequestedIntegrationEvent() {
            UUID taskId = UUID.randomUUID();
            ProductStockReservationRequestedIntegrationEvent event = new ProductStockReservationRequestedIntegrationEvent(
                    UUID.randomUUID(),
                    taskId,
                    UUID.randomUUID(),
                    new BigDecimal("3.50"),
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("WorkOrderTask");
            assertThat(saved.getAggregateId()).isEqualTo(taskId.toString());
            assertThat(saved.getEventType()).isEqualTo("ProductStockReservationRequestedIntegrationEvent");
        }

        @Test
        @DisplayName("Should persist ProductStockReservationCancelledIntegrationEvent into outbox")
        void shouldPersistProductStockReservationCancelledIntegrationEvent() {
            UUID taskId = UUID.randomUUID();
            ProductStockReservationCancelledIntegrationEvent event = new ProductStockReservationCancelledIntegrationEvent(
                    UUID.randomUUID(),
                    taskId,
                    UUID.randomUUID(),
                    new BigDecimal("1.00"),
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("WorkOrderTask");
            assertThat(saved.getAggregateId()).isEqualTo(taskId.toString());
            assertThat(saved.getEventType()).isEqualTo("ProductStockReservationCancelledIntegrationEvent");
        }

        @Test
        @DisplayName("Should persist WorkOrderCompletedIntegrationEvent into outbox")
        void shouldPersistWorkOrderCompletedIntegrationEvent() {
            UUID orderId = UUID.randomUUID();
            WorkOrderCompletedIntegrationEvent event = new WorkOrderCompletedIntegrationEvent(
                    orderId,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new BigDecimal("350.00"),
                    "PEN",
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("WorkOrder");
            assertThat(saved.getAggregateId()).isEqualTo(orderId.toString());
            assertThat(saved.getEventType()).isEqualTo("WorkOrderCompletedIntegrationEvent");
        }

        @Test
        @DisplayName("Should persist WorkOrderPaidIntegrationEvent into outbox")
        void shouldPersistWorkOrderPaidIntegrationEvent() {
            UUID orderId = UUID.randomUUID();
            WorkOrderPaidIntegrationEvent event = new WorkOrderPaidIntegrationEvent(
                    orderId,
                    UUID.randomUUID(),
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("WorkOrder");
            assertThat(saved.getAggregateId()).isEqualTo(orderId.toString());
            assertThat(saved.getEventType()).isEqualTo("WorkOrderPaidIntegrationEvent");
        }

        @Test
        @DisplayName("Should persist WorkOrderDeliveredIntegrationEvent into outbox")
        void shouldPersistWorkOrderDeliveredIntegrationEvent() {
            UUID orderId = UUID.randomUUID();
            WorkOrderDeliveredIntegrationEvent event = new WorkOrderDeliveredIntegrationEvent(
                    orderId,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("WorkOrder");
            assertThat(saved.getAggregateId()).isEqualTo(orderId.toString());
            assertThat(saved.getEventType()).isEqualTo("WorkOrderDeliveredIntegrationEvent");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when ObjectMapper serialization fails")
        void shouldThrowExceptionOnSerializationFailure() throws JsonProcessingException {
            ObjectMapper brokenMapper = mock(ObjectMapper.class);
            when(brokenMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization failed") {});
            OperationsTransactionalOutboxPublisher brokenPublisher = new OperationsTransactionalOutboxPublisher(outboxRepository, brokenMapper);

            WorkOrderPaidIntegrationEvent event = new WorkOrderPaidIntegrationEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Instant.now()
            );

            assertThatThrownBy(() -> brokenPublisher.on(event))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Could not serialize event to JSON");
        }
    }

    @Nested
    @DisplayName("WorkOrderTaskDomainEventsHandler Tests")
    class TaskDomainEventsHandlerTests {

        @Mock
        private ApplicationEventPublisher eventPublisher;

        private WorkOrderTaskDomainEventsHandler handler;

        @BeforeEach
        void setUp() {
            handler = new WorkOrderTaskDomainEventsHandler(eventPublisher);
        }

        @Test
        @DisplayName("Should translate ProductStockReservationRequestedEvent to integration event")
        void shouldTranslateProductStockReservationRequestedEvent() {
            WorkOrderId orderId = WorkOrderId.generate();
            WorkOrderTaskId taskId = WorkOrderTaskId.generate();
            UUID productId = UUID.randomUUID();
            Quantity qty = Quantity.of(new BigDecimal("3.0"));

            ProductStockReservationRequestedEvent event = ProductStockReservationRequestedEvent.of(orderId, taskId, productId, qty);
            handler.on(event);

            ArgumentCaptor<ProductStockReservationRequestedIntegrationEvent> captor =
                    ArgumentCaptor.forClass(ProductStockReservationRequestedIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            ProductStockReservationRequestedIntegrationEvent published = captor.getValue();
            assertThat(published.workOrderId()).isEqualTo(orderId.value());
            assertThat(published.taskId()).isEqualTo(taskId.value());
            assertThat(published.productId()).isEqualTo(productId);
            assertThat(published.quantity()).isEqualByComparingTo(new BigDecimal("3.0"));
        }

        @Test
        @DisplayName("Should translate ProductStockReservationCancelledEvent to integration event")
        void shouldTranslateProductStockReservationCancelledEvent() {
            WorkOrderId orderId = WorkOrderId.generate();
            WorkOrderTaskId taskId = WorkOrderTaskId.generate();
            UUID productId = UUID.randomUUID();
            Quantity qty = Quantity.of(new BigDecimal("2.0"));

            ProductStockReservationCancelledEvent event = ProductStockReservationCancelledEvent.of(orderId, taskId, productId, qty);
            handler.on(event);

            ArgumentCaptor<ProductStockReservationCancelledIntegrationEvent> captor =
                    ArgumentCaptor.forClass(ProductStockReservationCancelledIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            ProductStockReservationCancelledIntegrationEvent published = captor.getValue();
            assertThat(published.workOrderId()).isEqualTo(orderId.value());
            assertThat(published.taskId()).isEqualTo(taskId.value());
            assertThat(published.productId()).isEqualTo(productId);
            assertThat(published.quantity()).isEqualByComparingTo(new BigDecimal("2.0"));
        }

        @Test
        @DisplayName("Should handle informational task lifecycle events without error")
        void shouldHandleInformationalTaskEvents() {
            WorkOrderId orderId = WorkOrderId.generate();
            WorkOrderTaskId taskId = WorkOrderTaskId.generate();
            UUID mechanicId = UUID.randomUUID();

            handler.on(WorkOrderTaskAssignedEvent.of(orderId, taskId, mechanicId));
            handler.on(WorkOrderTaskHoldEvent.of(orderId, taskId, mechanicId, "Filtro"));
            handler.on(WorkOrderTaskResumedEvent.of(orderId, taskId));
            handler.on(WorkOrderTaskCompletedEvent.of(orderId, taskId, 2.5));

            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("WorkOrderDomainEventsHandler Tests")
    class WorkOrderDomainEventsHandlerTests {

        @Mock
        private ApplicationEventPublisher eventPublisher;

        private WorkOrderDomainEventsHandler handler;

        @BeforeEach
        void setUp() {
            handler = new WorkOrderDomainEventsHandler(eventPublisher);
        }

        @Test
        @DisplayName("Should publish WorkOrderCreatedIntegrationEvent on WorkOrderCreatedEvent")
        void shouldHandleWorkOrderCreatedEvent() {
            WorkOrderId orderId = WorkOrderId.generate();
            TenantId tenantId = TenantId.generate();
            VehicleId vehicleId = VehicleId.generate();
            WorkOrderNumber number = WorkOrderNumber.of(201);

            handler.on(WorkOrderCreatedEvent.of(orderId, tenantId, BranchId.generate(), vehicleId, CustomerId.generate(), number));

            ArgumentCaptor<WorkOrderCreatedIntegrationEvent> captor =
                    ArgumentCaptor.forClass(WorkOrderCreatedIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            WorkOrderCreatedIntegrationEvent published = captor.getValue();
            assertThat(published.workOrderId()).isEqualTo(orderId.value());
            assertThat(published.tenantId()).isEqualTo(tenantId.value());
            assertThat(published.vehicleId()).isEqualTo(vehicleId.value());
            assertThat(published.internalNumber()).isEqualTo(201);
        }

        @Test
        @DisplayName("Should publish WorkOrderBayAssignedIntegrationEvent on WorkBayAssignedEvent")
        void shouldHandleWorkBayAssignedEvent() {
            WorkOrderId orderId = WorkOrderId.generate();
            WorkBayId bayId = WorkBayId.generate();

            handler.on(WorkBayAssignedEvent.of(orderId, bayId));

            ArgumentCaptor<WorkOrderBayAssignedIntegrationEvent> captor =
                    ArgumentCaptor.forClass(WorkOrderBayAssignedIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            WorkOrderBayAssignedIntegrationEvent published = captor.getValue();
            assertThat(published.workOrderId()).isEqualTo(orderId.value());
            assertThat(published.bayId()).isEqualTo(bayId.value());
        }

        @Test
        @DisplayName("Should publish WorkOrderCompletedIntegrationEvent on WorkOrderCompletedEvent")
        void shouldHandleWorkOrderCompletedEvent() {
            WorkOrderId orderId = WorkOrderId.generate();
            TenantId tenantId = TenantId.generate();
            VehicleId vehicleId = VehicleId.generate();
            Money total = Money.soles(new BigDecimal("450.00"));

            handler.on(WorkOrderCompletedEvent.of(orderId, tenantId, vehicleId, total));

            ArgumentCaptor<WorkOrderCompletedIntegrationEvent> captor =
                    ArgumentCaptor.forClass(WorkOrderCompletedIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            WorkOrderCompletedIntegrationEvent published = captor.getValue();
            assertThat(published.workOrderId()).isEqualTo(orderId.value());
            assertThat(published.tenantId()).isEqualTo(tenantId.value());
            assertThat(published.totalAmount()).isEqualByComparingTo(new BigDecimal("450.00"));
            assertThat(published.currency()).isEqualTo("PEN");
        }

        @Test
        @DisplayName("Should publish WorkOrderPaidIntegrationEvent on WorkOrderPaidEvent")
        void shouldHandleWorkOrderPaidEvent() {
            WorkOrderId orderId = WorkOrderId.generate();
            TenantId tenantId = TenantId.generate();

            handler.on(WorkOrderPaidEvent.of(orderId, tenantId, Money.soles(new BigDecimal("100.00"))));

            ArgumentCaptor<WorkOrderPaidIntegrationEvent> captor =
                    ArgumentCaptor.forClass(WorkOrderPaidIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            WorkOrderPaidIntegrationEvent published = captor.getValue();
            assertThat(published.workOrderId()).isEqualTo(orderId.value());
            assertThat(published.tenantId()).isEqualTo(tenantId.value());
        }

        @Test
        @DisplayName("Should publish WorkOrderDeliveredIntegrationEvent on WorkOrderDeliveredEvent")
        void shouldHandleWorkOrderDeliveredEvent() {
            WorkOrderId orderId = WorkOrderId.generate();
            TenantId tenantId = TenantId.generate();
            VehicleId vehicleId = VehicleId.generate();

            handler.on(WorkOrderDeliveredEvent.of(orderId, tenantId, vehicleId));

            ArgumentCaptor<WorkOrderDeliveredIntegrationEvent> captor =
                    ArgumentCaptor.forClass(WorkOrderDeliveredIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            WorkOrderDeliveredIntegrationEvent published = captor.getValue();
            assertThat(published.workOrderId()).isEqualTo(orderId.value());
            assertThat(published.tenantId()).isEqualTo(tenantId.value());
            assertThat(published.vehicleId()).isEqualTo(vehicleId.value());
        }
    }
}
