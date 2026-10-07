package com.andeva.atelier.platform.invoicing.application;

import com.andeva.atelier.platform.invoicing.application.internal.eventhandlers.InvoicingDomainEventsHandler;
import com.andeva.atelier.platform.invoicing.application.internal.eventhandlers.InvoicingTransactionalOutboxPublisher;
import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.VoucherReceiptEmailGateway;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.interfaces.events.ElectronicVoucherIssuedIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherAcceptedBySunatIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherPaymentRegisteredIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherRejectedBySunatIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherVoidedIntegrationEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for Invoicing Event Handlers and Transactional Outbox Publisher.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Invoicing Event Handlers & Outbox Tests")
class InvoicingEventHandlersTest {

    private OutboxMessageJpaRepository outboxRepository;
    private ObjectMapper objectMapper;
    private VoucherReceiptEmailGateway emailGateway;

    private InvoicingTransactionalOutboxPublisher outboxPublisher;
    private InvoicingDomainEventsHandler domainEventsHandler;

    private final TenantId tenantId = TenantId.generate();
    private final BranchId branchId = BranchId.generate();
    private final CustomerId customerId = CustomerId.generate();
    private final VoucherId voucherId = VoucherId.generate();

    @BeforeEach
    void setUp() {
        outboxRepository = Mockito.mock(OutboxMessageJpaRepository.class);
        objectMapper = new ObjectMapper();
        emailGateway = Mockito.mock(VoucherReceiptEmailGateway.class);

        outboxPublisher = new InvoicingTransactionalOutboxPublisher(outboxRepository, objectMapper);
        domainEventsHandler = new InvoicingDomainEventsHandler(outboxPublisher, emailGateway);
    }

    @Test
    @DisplayName("Should persist ElectronicVoucherIssuedIntegrationEvent into outbox")
    void shouldPersistVoucherIssuedToOutbox() {
        ElectronicVoucherIssuedIntegrationEvent event = new ElectronicVoucherIssuedIntegrationEvent(
                voucherId.value(),
                tenantId.value(),
                branchId.value(),
                customerId.value(),
                UUID.randomUUID(),
                "FACTURA",
                "F001",
                1,
                new BigDecimal("118.00"),
                "PEN",
                Instant.now()
        );

        outboxPublisher.on(event);

        ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
        verify(outboxRepository).save(captor.capture());

        OutboxMessagePersistenceEntity saved = captor.getValue();
        assertThat(saved.getAggregateType()).isEqualTo("ElectronicVoucher");
        assertThat(saved.getAggregateId()).isEqualTo(voucherId.value().toString());
        assertThat(saved.getEventType()).isEqualTo("ElectronicVoucherIssuedIntegrationEvent");
        assertThat(saved.getPayload()).contains("F001");
    }

    @Test
    @DisplayName("Should translate domain events into integration events and outbox messages")
    void shouldHandleDomainEvents() {
        // 1. ElectronicVoucherIssuedEvent
        domainEventsHandler.on(com.andeva.atelier.platform.invoicing.domain.model.events.ElectronicVoucherIssuedEvent.of(
                voucherId,
                tenantId,
                branchId,
                customerId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                Money.of(new BigDecimal("118.00"), Currency.PEN)
        ));

        // 2. VoucherAcceptedBySunatEvent
        domainEventsHandler.on(com.andeva.atelier.platform.invoicing.domain.model.events.VoucherAcceptedBySunatEvent.of(
                voucherId,
                tenantId,
                "hash123",
                DigitalReceiptUrls.of("pdf", "xml", "cdr")
        ));

        // 3. VoucherRejectedBySunatEvent
        domainEventsHandler.on(com.andeva.atelier.platform.invoicing.domain.model.events.VoucherRejectedBySunatEvent.of(
                voucherId,
                tenantId,
                "2012",
                "Error RUC"
        ));

        // 4. VoucherVoidedEvent
        domainEventsHandler.on(com.andeva.atelier.platform.invoicing.domain.model.events.VoucherVoidedEvent.of(
                voucherId,
                tenantId,
                "Cancelación de servicio"
        ));

        // 5. VoucherPaymentRegisteredEvent
        domainEventsHandler.on(com.andeva.atelier.platform.invoicing.domain.model.events.VoucherPaymentRegisteredEvent.of(
                PaymentId.generate(),
                voucherId,
                tenantId,
                branchId,
                Money.of(new BigDecimal("50.00"), Currency.PEN),
                PaymentMethod.CASH,
                false
        ));

        // Verify outbox repository received 5 saves
        verify(outboxRepository, Mockito.times(5)).save(any(OutboxMessagePersistenceEntity.class));
    }
}
