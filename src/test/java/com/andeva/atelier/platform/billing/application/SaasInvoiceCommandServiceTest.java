package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.commandservices.SaasInvoiceCommandService;
import com.andeva.atelier.platform.billing.application.internal.commandservices.SaasInvoiceCommandServiceImpl;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.TenantBillingNotificationGatewayPort;
import com.andeva.atelier.platform.billing.domain.exceptions.SaasInvoiceNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.commands.RecordSaasInvoicePaymentCommand;
import com.andeva.atelier.platform.billing.domain.model.enums.InvoiceStatus;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.billing.domain.repositories.SaasInvoiceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link SaasInvoiceCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SaasInvoice Command Service Tests")
class SaasInvoiceCommandServiceTest {

    @Mock
    private SaasInvoiceRepository invoiceRepository;
    @Mock
    private TenantBillingNotificationGatewayPort notificationGatewayPort;

    private SaasInvoiceCommandService invoiceCommandService;

    @BeforeEach
    void setUp() {
        invoiceCommandService = new SaasInvoiceCommandServiceImpl(invoiceRepository, notificationGatewayPort);
    }

    @Test
    @DisplayName("Should successfully record a settled SaaS invoice receipt")
    void shouldRecordPaidInvoice() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        StripeInvoiceId stripeInvoiceId = new StripeInvoiceId("in_test_123");
        Money amount = Money.of(new BigDecimal("99.00"), Currency.USD);

        when(invoiceRepository.save(any(SaasInvoice.class))).thenAnswer(inv -> inv.getArgument(0));

        RecordSaasInvoicePaymentCommand command = new RecordSaasInvoicePaymentCommand(
                subId,
                tenantId,
                stripeInvoiceId,
                amount,
                "https://pdf",
                "https://hosted",
                Instant.now()
        );

        SaasInvoiceId invoiceId = invoiceCommandService.handle(command);

        assertThat(invoiceId).isNotNull();
        verify(invoiceRepository).save(any(SaasInvoice.class));
        verify(notificationGatewayPort).sendInvoiceReceipt(eq(tenantId), any(SaasInvoice.class));
    }

    @Test
    @DisplayName("Should mark existing invoice payment attempt as failed")
    void shouldMarkInvoicePaymentFailed() {
        SaasInvoice invoice = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                TenantId.generate(),
                new StripeInvoiceId("in_test_1"),
                Money.of(new BigDecimal("49.00"), Currency.USD),
                "https://pdf",
                "https://hosted",
                Instant.now()
        );

        when(invoiceRepository.findById(invoice.id())).thenReturn(Optional.of(invoice));

        invoiceCommandService.handleMarkFailed(invoice.id(), "Insufficient funds");

        assertThat(invoice.status()).isEqualTo(InvoiceStatus.UNCOLLECTIBLE);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Should mark invoice as void")
    void shouldMarkInvoiceVoid() {
        SaasInvoice invoice = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                TenantId.generate(),
                new StripeInvoiceId("in_test_2"),
                Money.of(new BigDecimal("49.00"), Currency.USD),
                "https://pdf",
                "https://hosted",
                Instant.now()
        );

        when(invoiceRepository.findById(invoice.id())).thenReturn(Optional.of(invoice));

        invoiceCommandService.handleMarkVoid(invoice.id());

        assertThat(invoice.status()).isEqualTo(InvoiceStatus.VOID);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Should throw SaasInvoiceNotFoundException if invoice does not exist")
    void shouldThrowIfInvoiceNotFound() {
        SaasInvoiceId invoiceId = SaasInvoiceId.generate();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceCommandService.handleMarkVoid(invoiceId))
                .isInstanceOf(SaasInvoiceNotFoundException.class);
    }
}
