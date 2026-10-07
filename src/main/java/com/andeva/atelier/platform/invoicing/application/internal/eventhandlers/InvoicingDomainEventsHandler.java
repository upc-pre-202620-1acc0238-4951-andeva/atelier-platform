package com.andeva.atelier.platform.invoicing.application.internal.eventhandlers;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.VoucherReceiptEmailGateway;
import com.andeva.atelier.platform.invoicing.domain.model.events.CreditNoteIssuedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.ElectronicVoucherIssuedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.SeriesConfigurationCreatedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.SeriesCorrelativeIncrementedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherAcceptedBySunatEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherPaymentRegisteredEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherRejectedBySunatEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherVoidedEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.ElectronicVoucherIssuedIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherAcceptedBySunatIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherPaymentRegisteredIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherRejectedBySunatIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherVoidedIntegrationEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Domain Event Handler listening to fiscal and payment domain events,
 * translating them into integration events, and orchestrating transactional outbox persistence.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class InvoicingDomainEventsHandler {

    private static final Logger log = LoggerFactory.getLogger(InvoicingDomainEventsHandler.class);

    private final InvoicingTransactionalOutboxPublisher outboxPublisher;
    private final VoucherReceiptEmailGateway emailGateway;

    public InvoicingDomainEventsHandler(
            InvoicingTransactionalOutboxPublisher outboxPublisher,
            VoucherReceiptEmailGateway emailGateway
    ) {
        this.outboxPublisher = Objects.requireNonNull(outboxPublisher, "Outbox publisher cannot be null");
        this.emailGateway = Objects.requireNonNull(emailGateway, "Email gateway cannot be null");
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(ElectronicVoucherIssuedEvent event) {
        log.info("Handling ElectronicVoucherIssuedEvent for voucher: {}", event.voucherId().value());

        ElectronicVoucherIssuedIntegrationEvent integrationEvent = new ElectronicVoucherIssuedIntegrationEvent(
                event.voucherId().value(),
                event.tenantId().value(),
                event.branchId().value(),
                event.customerId().value(),
                event.workOrderId().map(WorkOrderId::value).orElse(null),
                event.voucherType().name(),
                event.serie().value(),
                event.number().value(),
                event.totalAmount().amount(),
                event.currency().name(),
                event.occurredOn()
        );

        outboxPublisher.on(integrationEvent);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VoucherAcceptedBySunatEvent event) {
        log.info("Handling VoucherAcceptedBySunatEvent for voucher: {}", event.voucherId().value());

        VoucherAcceptedBySunatIntegrationEvent integrationEvent = new VoucherAcceptedBySunatIntegrationEvent(
                event.voucherId().value(),
                event.tenantId().value(),
                event.digitalSignatureHash(),
                event.urls() != null ? event.urls().pdfUrl() : "",
                event.urls() != null ? event.urls().xmlUrl() : "",
                event.urls() != null ? event.urls().cdrUrl() : "",
                event.occurredOn()
        );

        outboxPublisher.on(integrationEvent);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VoucherRejectedBySunatEvent event) {
        log.warn("Handling VoucherRejectedBySunatEvent for voucher: {} [code={}]",
                event.voucherId().value(), event.errorCode());

        VoucherRejectedBySunatIntegrationEvent integrationEvent = new VoucherRejectedBySunatIntegrationEvent(
                event.voucherId().value(),
                event.tenantId().value(),
                event.errorCode(),
                event.errorMessage(),
                event.occurredOn()
        );

        outboxPublisher.on(integrationEvent);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VoucherVoidedEvent event) {
        log.info("Handling VoucherVoidedEvent for voucher: {}", event.voucherId().value());

        VoucherVoidedIntegrationEvent integrationEvent = new VoucherVoidedIntegrationEvent(
                event.voucherId().value(),
                event.tenantId().value(),
                event.reason(),
                event.occurredOn()
        );

        outboxPublisher.on(integrationEvent);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VoucherPaymentRegisteredEvent event) {
        log.info("Handling VoucherPaymentRegisteredEvent for payment: {} on voucher: {}",
                event.paymentId().value(), event.voucherId().value());

        VoucherPaymentRegisteredIntegrationEvent integrationEvent = new VoucherPaymentRegisteredIntegrationEvent(
                event.paymentId().value(),
                event.voucherId().value(),
                event.tenantId().value(),
                event.branchId().value(),
                event.amount().amount(),
                event.amount().currency().name(),
                event.paymentMethod().name(),
                event.isFullyPaid(),
                event.occurredOn()
        );

        outboxPublisher.on(integrationEvent);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(CreditNoteIssuedEvent event) {
        log.info("Handling CreditNoteIssuedEvent for credit note: {} referencing: {}",
                event.creditNoteId().value(), event.referenceVoucherId().value());
        outboxPublisher.publish(
                event.creditNoteId().value().toString(),
                event.getClass().getSimpleName(),
                event,
                event.occurredOn()
        );
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(SeriesConfigurationCreatedEvent event) {
        log.info("Handling SeriesConfigurationCreatedEvent for series: {}", event.serie().value());
        outboxPublisher.publish(
                event.seriesId().value().toString(),
                event.getClass().getSimpleName(),
                event,
                event.occurredOn()
        );
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(SeriesCorrelativeIncrementedEvent event) {
        log.debug("Series correlative incremented for series: {} to {}",
                event.serie().value(), event.newCorrelative());
    }
}
