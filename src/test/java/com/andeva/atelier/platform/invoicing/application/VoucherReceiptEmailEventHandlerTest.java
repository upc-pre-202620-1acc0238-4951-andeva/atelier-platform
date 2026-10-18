package com.andeva.atelier.platform.invoicing.application;

import com.andeva.atelier.platform.invoicing.application.internal.eventhandlers.VoucherReceiptEmailEventHandler;
import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CustomerFiscalValidationAclService;
import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.VoucherReceiptEmailGateway;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherAcceptedBySunatEvent;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link VoucherReceiptEmailEventHandler}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Voucher Receipt Email Event Handler Tests")
class VoucherReceiptEmailEventHandlerTest {

    private ElectronicVoucherRepository voucherRepository;
    private CustomerFiscalValidationAclService customerAclService;
    private VoucherReceiptEmailGateway emailGateway;
    private VoucherReceiptEmailEventHandler handler;
    private ElectronicVoucher voucher;
    private VoucherAcceptedBySunatEvent event;

    @BeforeEach
    void setUp() {
        voucherRepository = mock(ElectronicVoucherRepository.class);
        customerAclService = mock(CustomerFiscalValidationAclService.class);
        emailGateway = mock(VoucherReceiptEmailGateway.class);
        handler = new VoucherReceiptEmailEventHandler(voucherRepository, customerAclService, emailGateway);

        voucher = ElectronicVoucher.issue(
                TenantId.generate(), BranchId.generate(), CustomerId.generate(), null,
                VoucherType.FACTURA, VoucherSerie.of("F001"), VoucherNumber.of(12),
                CustomerFiscalInfo.of(TaxId.ruc("20100070970"), "SUNAT SAC", "LIMA", DocumentType.RUC),
                Currency.PEN,
                TaxCalculation.of(
                        Money.of(new BigDecimal("100.00"), Currency.PEN),
                        Money.of(new BigDecimal("18.00"), Currency.PEN),
                        Money.of(new BigDecimal("118.00"), Currency.PEN)
                ),
                List.of(new PeruvianTaxCalculationEngine().calculateLine(null, VoucherId.generate(), Optional.empty(),
                        VoucherItemType.SERVICE, "Mantenimiento", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );
        event = VoucherAcceptedBySunatEvent.of(voucher.getId(), voucher.getTenantId(), "hash",
                DigitalReceiptUrls.of("https://nubefact/x.pdf", "https://nubefact/x.xml", "https://nubefact/x.cdr"));
        when(voucherRepository.findById(voucher.getId())).thenReturn(Optional.of(voucher));
    }

    @Test
    @DisplayName("Should email the receipt links to the customer")
    void shouldSendReceiptToCustomerEmail() {
        when(customerAclService.getCustomerEmail(voucher.getCustomerId().value())).thenReturn(Optional.of("cliente@correo.pe"));

        handler.on(event);

        verify(emailGateway).sendVoucherReceiptEmail(
                voucher.getId().value(), "cliente@correo.pe", "SUNAT SAC", "F001-00000012",
                "https://nubefact/x.pdf", "https://nubefact/x.xml");
    }

    @Test
    @DisplayName("Should skip the email when the customer has no address")
    void shouldSkipWhenCustomerHasNoEmail() {
        when(customerAclService.getCustomerEmail(voucher.getCustomerId().value())).thenReturn(Optional.empty());

        handler.on(event);

        verify(emailGateway, never()).sendVoucherReceiptEmail(any(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
}
