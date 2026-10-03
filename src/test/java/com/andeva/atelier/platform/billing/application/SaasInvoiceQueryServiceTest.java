package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.internal.queryservices.SaasInvoiceQueryServiceImpl;
import com.andeva.atelier.platform.billing.application.queryservices.SaasInvoiceQueryService;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.queries.ListTenantInvoicesQuery;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for {@link SaasInvoiceQueryServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SaasInvoice Query Service Tests")
class SaasInvoiceQueryServiceTest {

    @Mock
    private SaasInvoiceRepository invoiceRepository;

    private SaasInvoiceQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new SaasInvoiceQueryServiceImpl(invoiceRepository);
    }

    @Test
    @DisplayName("Should list all invoices for a tenant")
    void shouldListTenantInvoices() {
        TenantId tenantId = TenantId.generate();
        SaasInvoice invoice = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                tenantId,
                new StripeInvoiceId("in_123"),
                Money.of(new BigDecimal("99.00"), Currency.USD),
                "https://pdf",
                "https://hosted",
                Instant.now()
        );

        when(invoiceRepository.findAllByTenantId(tenantId)).thenReturn(List.of(invoice));

        List<SaasInvoice> results = queryService.handle(new ListTenantInvoicesQuery(tenantId));

        assertThat(results).hasSize(1);
        verify(invoiceRepository).findAllByTenantId(tenantId);
    }

    @Test
    @DisplayName("Should find invoice by internal ID")
    void shouldFindById() {
        SaasInvoiceId invoiceId = SaasInvoiceId.generate();
        SaasInvoice invoice = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                TenantId.generate(),
                new StripeInvoiceId("in_123"),
                Money.of(new BigDecimal("99.00"), Currency.USD),
                "https://pdf",
                "https://hosted",
                Instant.now()
        );

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        Optional<SaasInvoice> result = queryService.findById(invoiceId);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Should find invoice by Stripe invoice ID")
    void shouldFindByStripeInvoiceId() {
        StripeInvoiceId stripeInvoiceId = new StripeInvoiceId("in_stripe_xyz");
        SaasInvoice invoice = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                TenantId.generate(),
                stripeInvoiceId,
                Money.of(new BigDecimal("99.00"), Currency.USD),
                "https://pdf",
                "https://hosted",
                Instant.now()
        );

        when(invoiceRepository.findByStripeInvoiceId(stripeInvoiceId)).thenReturn(Optional.of(invoice));

        Optional<SaasInvoice> result = queryService.findByStripeInvoiceId(stripeInvoiceId);

        assertThat(result).isPresent();
    }
}
