package com.andeva.atelier.platform.crm.application.internal.eventhandlers;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.DriverAppPushGateway;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentArrivedEvent;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentCanceledEvent;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentConfirmedEvent;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentScheduledEvent;
import com.andeva.atelier.platform.crm.domain.model.events.CustomerContactUpdatedEvent;
import com.andeva.atelier.platform.crm.domain.model.events.CustomerRegisteredEvent;
import com.andeva.atelier.platform.crm.domain.model.events.VehicleOwnershipTransferredEvent;
import com.andeva.atelier.platform.crm.domain.model.events.VehicleRegisteredEvent;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.domain.model.ids.VehicleOwnershipId;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.Vin;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.crm.interfaces.events.AppointmentArrivedIntegrationEvent;
import com.andeva.atelier.platform.crm.interfaces.events.AppointmentScheduledIntegrationEvent;
import com.andeva.atelier.platform.crm.interfaces.events.CustomerCreatedIntegrationEvent;
import com.andeva.atelier.platform.crm.interfaces.events.VehicleOwnershipTransferredIntegrationEvent;
import com.andeva.atelier.platform.crm.interfaces.events.VehicleRegisteredIntegrationEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxStatus;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for CRM event handlers and transactional outbox publisher.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CRM Event Handlers and Outbox Publisher Tests")
class CrmEventHandlersTest {

    @Nested
    @DisplayName("CrmTransactionalOutboxPublisher Tests")
    class OutboxPublisherTests {

        @Mock
        private OutboxMessageJpaRepository outboxRepository;

        private CrmTransactionalOutboxPublisher publisher;

        @BeforeEach
        void setUp() {
            publisher = new CrmTransactionalOutboxPublisher(outboxRepository, new ObjectMapper());
        }

        @Test
        @DisplayName("Should persist CustomerCreatedIntegrationEvent to outbox")
        void shouldPersistCustomerCreatedIntegrationEvent() {
            CustomerCreatedIntegrationEvent event = new CustomerCreatedIntegrationEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "INDIVIDUAL",
                    "Juan Perez",
                    "12345678",
                    "juan@example.com",
                    "+51987654321",
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor =
                    ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("Customer");
            assertThat(saved.getAggregateId()).isEqualTo(event.customerId().toString());
            assertThat(saved.getEventType()).isEqualTo("CustomerCreatedIntegrationEvent");
            assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(saved.getPayload()).contains("Juan Perez");
        }

        @Test
        @DisplayName("Should persist VehicleRegisteredIntegrationEvent to outbox")
        void shouldPersistVehicleRegisteredIntegrationEvent() {
            VehicleRegisteredIntegrationEvent event = new VehicleRegisteredIntegrationEvent(
                    UUID.randomUUID(),
                    "ABC-123",
                    "1HGCR2F83HA000000",
                    "Toyota",
                    "Corolla",
                    2022,
                    "GASOLINE",
                    UUID.randomUUID(),
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor =
                    ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("Vehicle");
            assertThat(saved.getAggregateId()).isEqualTo(event.vehicleId().toString());
            assertThat(saved.getPayload()).contains("ABC-123");
        }

        @Test
        @DisplayName("Should persist VehicleOwnershipTransferredIntegrationEvent to outbox")
        void shouldPersistVehicleOwnershipTransferredIntegrationEvent() {
            VehicleOwnershipTransferredIntegrationEvent event = new VehicleOwnershipTransferredIntegrationEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    LocalDate.now(),
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor =
                    ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("Vehicle");
            assertThat(saved.getEventType()).isEqualTo("VehicleOwnershipTransferredIntegrationEvent");
        }

        @Test
        @DisplayName("Should persist AppointmentScheduledIntegrationEvent to outbox")
        void shouldPersistAppointmentScheduledIntegrationEvent() {
            AppointmentScheduledIntegrationEvent event = new AppointmentScheduledIntegrationEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Instant.now().plusSeconds(3600),
                    60,
                    "Mantenimiento regular",
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor =
                    ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("Appointment");
            assertThat(saved.getEventType()).isEqualTo("AppointmentScheduledIntegrationEvent");
        }

        @Test
        @DisplayName("Should persist AppointmentArrivedIntegrationEvent to outbox")
        void shouldPersistAppointmentArrivedIntegrationEvent() {
            AppointmentArrivedIntegrationEvent event = new AppointmentArrivedIntegrationEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor =
                    ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("Appointment");
            assertThat(saved.getEventType()).isEqualTo("AppointmentArrivedIntegrationEvent");
        }
    }

    @Nested
    @DisplayName("CustomerDomainEventsHandler Tests")
    class CustomerDomainEventsHandlerTests {

        @Mock
        private ApplicationEventPublisher eventPublisher;

        private CustomerDomainEventsHandler handler;

        @BeforeEach
        void setUp() {
            handler = new CustomerDomainEventsHandler(eventPublisher);
        }

        @Test
        @DisplayName("Should publish CustomerCreatedIntegrationEvent when CustomerRegisteredEvent received")
        void shouldPublishIntegrationEventOnCustomerRegistered() {
            CustomerRegisteredEvent event = new CustomerRegisteredEvent(
                    CustomerId.generate(),
                    TenantId.generate(),
                    CustomerType.INDIVIDUAL,
                    "Ana Torres",
                    TaxId.of("45871234"),
                    Instant.now()
            );

            handler.on(event);

            ArgumentCaptor<CustomerCreatedIntegrationEvent> captor =
                    ArgumentCaptor.forClass(CustomerCreatedIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            CustomerCreatedIntegrationEvent published = captor.getValue();
            assertThat(published.customerId()).isEqualTo(event.customerId().value());
            assertThat(published.displayName()).isEqualTo("Ana Torres");
            assertThat(published.taxId()).isEqualTo("45871234");
        }

        @Test
        @DisplayName("Should handle CustomerContactUpdatedEvent without exception")
        void shouldHandleCustomerContactUpdatedEvent() {
            CustomerContactUpdatedEvent event = new CustomerContactUpdatedEvent(
                    CustomerId.generate(),
                    EmailAddress.of("new@email.com"),
                    PhoneNumber.of("+51999888777"),
                    Instant.now()
            );

            handler.on(event);
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("VehicleDomainEventsHandler Tests")
    class VehicleDomainEventsHandlerTests {

        @Mock
        private VehicleRepository vehicleRepository;

        @Mock
        private ApplicationEventPublisher eventPublisher;

        private VehicleDomainEventsHandler handler;

        @BeforeEach
        void setUp() {
            handler = new VehicleDomainEventsHandler(vehicleRepository, eventPublisher);
        }

        @Test
        @DisplayName("Should publish VehicleRegisteredIntegrationEvent when VehicleRegisteredEvent received")
        void shouldPublishIntegrationEventOnVehicleRegistered() {
            VehicleId vehicleId = VehicleId.generate();
            LicensePlate plate = LicensePlate.of("ABC123");
            CustomerId ownerId = CustomerId.generate();

            Vehicle vehicle = Vehicle.register(
                    vehicleId,
                    plate,
                    Vin.of("1HGCR2F83HA000000"),
                    "Toyota",
                    "Corolla",
                    2022,
                    EngineType.GASOLINE,
                    Optional.of(ownerId),
                    Optional.empty()
            );
            when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

            VehicleRegisteredEvent event = new VehicleRegisteredEvent(
                    vehicleId,
                    plate,
                    ownerId,
                    Instant.now()
            );

            handler.on(event);

            ArgumentCaptor<VehicleRegisteredIntegrationEvent> captor =
                    ArgumentCaptor.forClass(VehicleRegisteredIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            VehicleRegisteredIntegrationEvent published = captor.getValue();
            assertThat(published.vehicleId()).isEqualTo(vehicleId.value());
            assertThat(published.brand()).isEqualTo("Toyota");
            assertThat(published.model()).isEqualTo("Corolla");
        }

        @Test
        @DisplayName("Should publish VehicleOwnershipTransferredIntegrationEvent when transferred")
        void shouldPublishIntegrationEventOnOwnershipTransferred() {
            VehicleId vehicleId = VehicleId.generate();
            CustomerId prevOwner = CustomerId.generate();
            CustomerId newOwner = CustomerId.generate();

            VehicleOwnershipTransferredEvent event = new VehicleOwnershipTransferredEvent(
                    vehicleId,
                    prevOwner,
                    newOwner,
                    LocalDate.now(),
                    Instant.now()
            );

            handler.on(event);

            ArgumentCaptor<VehicleOwnershipTransferredIntegrationEvent> captor =
                    ArgumentCaptor.forClass(VehicleOwnershipTransferredIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            VehicleOwnershipTransferredIntegrationEvent published = captor.getValue();
            assertThat(published.vehicleId()).isEqualTo(vehicleId.value());
            assertThat(published.previousOwnerId()).isEqualTo(prevOwner.value());
            assertThat(published.newOwnerId()).isEqualTo(newOwner.value());
        }
    }

    @Nested
    @DisplayName("AppointmentDomainEventsHandler Tests")
    class AppointmentDomainEventsHandlerTests {

        @Mock
        private DriverAppPushGateway driverAppPushGateway;

        @Mock
        private ApplicationEventPublisher eventPublisher;

        private AppointmentDomainEventsHandler handler;

        @BeforeEach
        void setUp() {
            handler = new AppointmentDomainEventsHandler(driverAppPushGateway, eventPublisher);
        }

        @Test
        @DisplayName("Should publish AppointmentScheduledIntegrationEvent when scheduled")
        void shouldPublishIntegrationEventOnAppointmentScheduled() {
            AppointmentId apptId = AppointmentId.generate();
            TenantId tenantId = TenantId.generate();
            BranchId branchId = BranchId.generate();
            CustomerId customerId = CustomerId.generate();
            VehicleId vehicleId = VehicleId.generate();
            Instant scheduledAt = Instant.now().plusSeconds(3600);

            AppointmentScheduledEvent event = new AppointmentScheduledEvent(
                    apptId,
                    tenantId,
                    branchId,
                    customerId,
                    vehicleId,
                    scheduledAt,
                    Instant.now()
            );

            handler.on(event);

            ArgumentCaptor<AppointmentScheduledIntegrationEvent> captor =
                    ArgumentCaptor.forClass(AppointmentScheduledIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            AppointmentScheduledIntegrationEvent published = captor.getValue();
            assertThat(published.appointmentId()).isEqualTo(apptId.value());
            assertThat(published.tenantId()).isEqualTo(tenantId.value());
        }

        @Test
        @DisplayName("Should send push notification on AppointmentConfirmedEvent")
        void shouldSendPushOnAppointmentConfirmed() {
            AppointmentId apptId = AppointmentId.generate();
            CustomerId customerId = CustomerId.generate();

            AppointmentConfirmedEvent event = new AppointmentConfirmedEvent(
                    apptId,
                    customerId,
                    Instant.now().plusSeconds(3600),
                    Instant.now()
            );

            handler.on(event);

            verify(driverAppPushGateway).sendPushNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should publish AppointmentArrivedIntegrationEvent when arrived")
        void shouldPublishIntegrationEventOnAppointmentArrived() {
            AppointmentId apptId = AppointmentId.generate();
            TenantId tenantId = TenantId.generate();
            BranchId branchId = BranchId.generate();
            CustomerId customerId = CustomerId.generate();
            VehicleId vehicleId = VehicleId.generate();

            AppointmentArrivedEvent event = new AppointmentArrivedEvent(
                    apptId,
                    tenantId,
                    branchId,
                    customerId,
                    vehicleId,
                    Instant.now()
            );

            handler.on(event);

            ArgumentCaptor<AppointmentArrivedIntegrationEvent> captor =
                    ArgumentCaptor.forClass(AppointmentArrivedIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            AppointmentArrivedIntegrationEvent published = captor.getValue();
            assertThat(published.appointmentId()).isEqualTo(apptId.value());
        }

        @Test
        @DisplayName("Should handle AppointmentCanceledEvent gracefully")
        void shouldHandleAppointmentCanceledEvent() {
            AppointmentCanceledEvent event = new AppointmentCanceledEvent(
                    AppointmentId.generate(),
                    "Cliente canceló por viaje",
                    Instant.now()
            );

            handler.on(event);
            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}
