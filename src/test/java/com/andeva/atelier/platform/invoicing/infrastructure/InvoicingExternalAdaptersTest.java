package com.andeva.atelier.platform.invoicing.infrastructure;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.NubefactPseFiscalGateway;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CreditNoteReference;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.invoicing.infrastructure.external.acl.crm.CustomerFiscalValidationAclAdapter;
import com.andeva.atelier.platform.invoicing.infrastructure.external.cloud.firebase.FirebaseSunatCdrStorageAdapter;
import com.andeva.atelier.platform.invoicing.infrastructure.external.mail.resend.ResendVoucherReceiptEmailAdapter;
import com.andeva.atelier.platform.invoicing.infrastructure.external.messaging.outbox.InvoicingOutboxMessageRelayAdapter;
import com.andeva.atelier.platform.invoicing.infrastructure.external.reporting.openpdf.OpenPdfCashFlowReportAdapter;
import com.andeva.atelier.platform.invoicing.infrastructure.external.tax.nubefact.NubefactPseFiscalAdapter;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxStatus;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Invoicing External Adapters (Nubefact, Firebase, Resend, CRM ACL, OpenPDF, Outbox Relay).
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Invoicing External Adapters Unit Tests")
class InvoicingExternalAdaptersTest {

    @Mock
    private OutboxMessageJpaRepository outboxRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private NubefactPseFiscalAdapter nubefactAdapter;
    private FirebaseSunatCdrStorageAdapter firebaseAdapter;
    private ResendVoucherReceiptEmailAdapter resendAdapter;
    private CustomerFiscalValidationAclAdapter crmAdapter;
    private OpenPdfCashFlowReportAdapter openPdfAdapter;
    private InvoicingOutboxMessageRelayAdapter outboxRelay;

    private PeruvianTaxCalculationEngine taxEngine;

    @BeforeEach
    void setUp() {
        nubefactAdapter = new NubefactPseFiscalAdapter("https://api.nubefact.com/api/v1", "test-token");
        firebaseAdapter = new FirebaseSunatCdrStorageAdapter("atelier-platform-cdr.appspot.com");
        resendAdapter = new ResendVoucherReceiptEmailAdapter("re_123456", "billing@atelier.com");
        crmAdapter = new CustomerFiscalValidationAclAdapter();
        openPdfAdapter = new OpenPdfCashFlowReportAdapter();
        outboxRelay = new InvoicingOutboxMessageRelayAdapter(outboxRepository, eventPublisher);
        taxEngine = new PeruvianTaxCalculationEngine();
    }

    @Test
    @DisplayName("Should dispatch voucher, credit note, and voiding via NubefactPseFiscalAdapter")
    void testNubefactAdapter() {
        TenantId tenantId = TenantId.generate();
        BranchId branchId = BranchId.generate();
        CustomerId customerId = CustomerId.generate();
        WorkOrderId workOrderId = WorkOrderId.generate();
        VoucherId voucherId = VoucherId.generate();

        CustomerFiscalInfo fiscal = CustomerFiscalInfo.of(
                TaxId.ruc("20100070970"), "SUNAT SAC", "LIMA", DocumentType.RUC
        );
        TaxCalculation taxCalc = TaxCalculation.of(
                Money.of(new BigDecimal("100.00"), Currency.PEN),
                Money.of(new BigDecimal("18.00"), Currency.PEN),
                Money.of(new BigDecimal("118.00"), Currency.PEN)
        );

        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId, branchId, customerId, workOrderId,
                VoucherType.FACTURA, VoucherSerie.of("F001"), VoucherNumber.of(1),
                fiscal, Currency.PEN, taxCalc,
                List.of(taxEngine.calculateLine(null, voucherId, Optional.empty(), VoucherItemType.PRODUCT, "Aceite sintético", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );

        NubefactPseFiscalGateway.NubefactDispatchResult result = nubefactAdapter.dispatchVoucher(voucher);
        assertThat(result.isAccepted()).isTrue();
        assertThat(result.responseCode()).isEqualTo("0");
        assertThat(result.urls().pdfUrl()).contains(".pdf");
        assertThat(result.digitalSignatureHash()).isNotBlank();

        ElectronicVoucher creditNote = ElectronicVoucher.issueCreditNote(
                tenantId, branchId, customerId, workOrderId,
                VoucherSerie.of("FC01"), VoucherNumber.of(1),
                fiscal, Currency.PEN, taxCalc,
                CreditNoteReference.of(voucher.getId(), voucher.getSerie(), voucher.getNumber(), CreditNoteReason.ANULACION_DE_LA_OPERACION, "Devolución"),
                List.of(taxEngine.calculateLine(null, VoucherId.generate(), Optional.empty(), VoucherItemType.PRODUCT, "Devolución Aceite", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );

        NubefactPseFiscalGateway.NubefactDispatchResult cnResult = nubefactAdapter.dispatchCreditNote(creditNote);
        assertThat(cnResult.isAccepted()).isTrue();

        NubefactPseFiscalGateway.NubefactVoidResult voidResult = nubefactAdapter.voidVoucher(voucher, "Error en facturación");
        assertThat(voidResult.isAccepted()).isTrue();
        assertThat(voidResult.ticketNumber()).startsWith("TICKET-");
    }

    @Test
    @DisplayName("Should store and retrieve XML artifacts via FirebaseSunatCdrStorageAdapter")
    void testFirebaseStorageAdapter() {
        UUID voucherId = UUID.randomUUID();
        byte[] cdrBytes = "<CDR status='OK'/>".getBytes();
        byte[] xmlBytes = "<Invoice>UBL2.1</Invoice>".getBytes();

        String cdrUrl = firebaseAdapter.storeCdrXml(voucherId, cdrBytes);
        assertThat(cdrUrl).contains("cdr%2F" + voucherId);

        String xmlUrl = firebaseAdapter.storeSignedXml(voucherId, xmlBytes);
        assertThat(xmlUrl).contains("xml%2F" + voucherId);

        byte[] retrieved = firebaseAdapter.retrieveCdrXml(voucherId);
        assertThat(retrieved).isEqualTo(cdrBytes);
    }

    @Test
    @DisplayName("Should send email without errors via ResendVoucherReceiptEmailAdapter")
    void testResendAdapter() {
        UUID voucherId = UUID.randomUUID();
        resendAdapter.sendVoucherReceiptEmail(
                voucherId,
                "cliente@gmail.com",
                "Juan Perez",
                "F001-00000001",
                "%PDF-1.4 sample".getBytes(),
                "<Invoice/>".getBytes()
        );
        // Verified by non-exceptional completion
    }

    @Test
    @DisplayName("Should validate RUC and DNI syntax via CustomerFiscalValidationAclAdapter")
    void testCustomerFiscalValidationAclAdapter() {
        assertThat(crmAdapter.validateTaxIdStatus("20100070970")).isTrue();
        assertThat(crmAdapter.validateTaxIdStatus("10456789012")).isTrue();
        assertThat(crmAdapter.validateTaxIdStatus("72345678")).isTrue(); // DNI 8 digits
        assertThat(crmAdapter.validateTaxIdStatus("123")).isFalse();
        assertThat(crmAdapter.validateTaxIdStatus(null)).isFalse();

        CustomerFiscalInfo info = crmAdapter.getCustomerFiscalData(UUID.randomUUID()).orElse(null);
        assertThat(info).isNotNull();
        assertThat(info.legalName()).isEqualTo("CLIENTES VARIOS");
    }

    @Test
    @DisplayName("Should render Cash Flow Statement PDF via OpenPdfCashFlowReportAdapter")
    void testOpenPdfCashFlowReportAdapter() {
        UUID tenantId = UUID.randomUUID();
        CashFlowSummary summary = CashFlowSummary.of(
                Money.of(new BigDecimal("15000.00"), Currency.PEN),
                Money.of(new BigDecimal("5000.00"), Currency.PEN),
                Money.of(new BigDecimal("3000.00"), Currency.PEN),
                Money.of(new BigDecimal("7000.00"), Currency.PEN)
        );

        CashFlowMovement movement = CashFlowMovement.of(
                UUID.randomUUID(),
                Instant.now(),
                "INFLOW",
                "OPERATING",
                "Cobro OT-101 Factura F001-1",
                "F001-00000001",
                new BigDecimal("1500.00"),
                new BigDecimal("1500.00")
        );

        byte[] pdf = openPdfAdapter.generateCashFlowPdf(tenantId, summary, List.of(movement));
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(100);
        // Valid PDF magic header
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("Should relay pending outbox messages via InvoicingOutboxMessageRelayAdapter")
    void testOutboxRelayAdapter() {
        OutboxMessagePersistenceEntity pendingMsg = OutboxMessagePersistenceEntity.pendingOf(
                "ElectronicVoucher",
                UUID.randomUUID().toString(),
                "ElectronicVoucherIssuedIntegrationEvent",
                "{\"voucherId\":\"" + UUID.randomUUID() + "\"}",
                Instant.now()
        );

        when(outboxRepository.findTop50ByStatusAndAggregateTypeOrderByOccurredOnAsc(
                eq(OutboxStatus.PENDING), eq("ElectronicVoucher")
        )).thenReturn(List.of(pendingMsg));

        outboxRelay.relayPendingInvoicingMessages();

        verify(eventPublisher).publishEvent(pendingMsg);
        assertThat(pendingMsg.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        verify(outboxRepository).save(pendingMsg);
    }
}
