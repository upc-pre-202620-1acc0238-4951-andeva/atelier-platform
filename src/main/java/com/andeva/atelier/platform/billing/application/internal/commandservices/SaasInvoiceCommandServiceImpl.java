package com.andeva.atelier.platform.billing.application.internal.commandservices;

import com.andeva.atelier.platform.billing.application.commandservices.SaasInvoiceCommandService;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.TenantBillingNotificationGatewayPort;
import com.andeva.atelier.platform.billing.domain.exceptions.SaasInvoiceNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.commands.RecordSaasInvoicePaymentCommand;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.repositories.SaasInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Implementation of SaasInvoiceCommandService responsible for recording invoice receipts,
 * managing payment failure marks, and voiding invalid invoices.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class SaasInvoiceCommandServiceImpl implements SaasInvoiceCommandService {

    private final SaasInvoiceRepository invoiceRepository;
    private final TenantBillingNotificationGatewayPort notificationGatewayPort;

    public SaasInvoiceCommandServiceImpl(
            SaasInvoiceRepository invoiceRepository,
            TenantBillingNotificationGatewayPort notificationGatewayPort
    ) {
        this.invoiceRepository = Objects.requireNonNull(invoiceRepository, "SaasInvoiceRepository cannot be null");
        this.notificationGatewayPort = Objects.requireNonNull(notificationGatewayPort, "TenantBillingNotificationGatewayPort cannot be null");
    }

    @Override
    public SaasInvoiceId handle(RecordSaasInvoicePaymentCommand command) {
        Objects.requireNonNull(command, "RecordSaasInvoicePaymentCommand cannot be null");

        SaasInvoice invoice = SaasInvoice.recordPaid(
                command.subscriptionId(),
                command.tenantId(),
                command.stripeInvoiceId(),
                command.amount(),
                command.pdfUrl(),
                command.hostedUrl(),
                command.paidAt()
        );

        SaasInvoice saved = invoiceRepository.save(invoice);
        notificationGatewayPort.sendInvoiceReceipt(command.tenantId(), saved);
        return saved.id();
    }

    @Override
    public void handleMarkFailed(SaasInvoiceId invoiceId, String reason) {
        Objects.requireNonNull(invoiceId, "SaasInvoiceId cannot be null");

        SaasInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new SaasInvoiceNotFoundException(invoiceId));

        invoice.markPaymentFailed(reason != null ? reason : "Payment transaction rejected");
        invoiceRepository.save(invoice);
    }

    @Override
    public void handleMarkVoid(SaasInvoiceId invoiceId) {
        Objects.requireNonNull(invoiceId, "SaasInvoiceId cannot be null");

        SaasInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new SaasInvoiceNotFoundException(invoiceId));

        invoice.markVoid();
        invoiceRepository.save(invoice);
    }
}
