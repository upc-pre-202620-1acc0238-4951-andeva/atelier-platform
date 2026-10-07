package com.andeva.atelier.platform.invoicing.domain;

import com.andeva.atelier.platform.invoicing.domain.exceptions.CustomerFiscalDataMissingException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidVoucherAmountException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherAlreadyPaidException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherImmutableException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.events.CreditNoteIssuedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.ElectronicVoucherIssuedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherAcceptedBySunatEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherPaymentRegisteredEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherRejectedBySunatEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherVoidedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CreditNoteReference;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ElectronicVoucher} aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("ElectronicVoucher Aggregate Unit Tests")
class ElectronicVoucherTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final BranchId branchId = BranchId.of(UUID.randomUUID());
    private final CustomerId customerId = CustomerId.of(UUID.randomUUID());
    private final WorkOrderId workOrderId = WorkOrderId.of(UUID.randomUUID());

    private TaxId validRuc;
    private CustomerFiscalInfo corporateCustomer;
    private VoucherLine sampleLine;
    private TaxCalculation sampleTaxCalculation;

    @BeforeEach
    void setUp() {
        validRuc = TaxId.ruc("20100070970");
        corporateCustomer = CustomerFiscalInfo.of(
                validRuc,
                "AUTOMOTRIZ CENTRAL S.A.C.",
                "AV. JAVIER PRADO ESTE 4500",
                DocumentType.RUC
        );

        // Line: qty 1, unitPrice 118.00 (subtotal 100.00, igv 18.00, total 118.00)
        sampleLine = VoucherLine.create(
                null,
                VoucherId.generate(),
                UUID.randomUUID(),
                VoucherItemType.SERVICE,
                "Alineamiento y balanceo computarizado",
                Quantity.ofUnits(1),
                Money.soles(118.00)
        );

        sampleTaxCalculation = TaxCalculation.of(
                Money.soles(100.00),
                Money.soles(18.00),
                Money.soles(118.00)
        );
    }

    @Test
    @DisplayName("Should successfully issue Factura with valid RUC and register ElectronicVoucherIssuedEvent")
    void shouldIssueFacturaSuccessfully() {
        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                workOrderId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                corporateCustomer,
                Currency.PEN,
                sampleTaxCalculation,
                List.of(sampleLine)
        );

        assertThat(voucher.getId()).isNotNull();
        assertThat(voucher.getVoucherType()).isEqualTo(VoucherType.FACTURA);
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.ISSUED);
        assertThat(voucher.getWorkOrderId()).contains(workOrderId);
        assertThat(voucher.getPendingBalance()).isEqualTo(Money.soles(118.00));
        assertThat(voucher.isFullyPaid()).isFalse();

        assertThat(voucher.domainEvents()).hasSize(1);
        assertThat(voucher.domainEvents()).first().isInstanceOf(ElectronicVoucherIssuedEvent.class);
    }

    @Test
    @DisplayName("Should reject Factura if customer tax document is not RUC or missing")
    void shouldRejectFacturaWithoutRuc() {
        CustomerFiscalInfo dniCustomer = CustomerFiscalInfo.of(
                TaxId.dni("12345678"),
                "JUAN PEREZ",
                "CALLE LOS PINOS 123",
                DocumentType.DNI
        );

        assertThatThrownBy(() -> ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                null,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                dniCustomer,
                Currency.PEN,
                sampleTaxCalculation,
                List.of(sampleLine)
        )).isInstanceOf(CustomerFiscalDataMissingException.class);
    }

    @Test
    @DisplayName("Should permit anonymous Boleta below S/ 700 threshold")
    void shouldPermitAnonymousBoletaBelowThreshold() {
        CustomerFiscalInfo anon = CustomerFiscalInfo.anonymous();

        ElectronicVoucher boleta = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                null,
                VoucherType.BOLETA,
                VoucherSerie.of("B001"),
                VoucherNumber.of(1),
                anon,
                Currency.PEN,
                sampleTaxCalculation, // S/ 118.00 < S/ 700.00
                List.of(sampleLine)
        );

        assertThat(boleta.getStatus()).isEqualTo(VoucherStatus.ISSUED);
    }

    @Test
    @DisplayName("Should reject anonymous Boleta exceeding S/ 700 threshold")
    void shouldRejectAnonymousBoletaExceedingThreshold() {
        // High amount line: S/ 850.00
        VoucherLine bigLine = VoucherLine.create(
                null,
                VoucherId.generate(),
                null,
                VoucherItemType.PRODUCT,
                "Kit de embrague reforzado",
                Quantity.ofUnits(1),
                Money.soles(850.00)
        );
        TaxCalculation bigTax = TaxCalculation.of(
                Money.soles(720.34),
                Money.soles(129.66),
                Money.soles(850.00)
        );

        CustomerFiscalInfo anon = CustomerFiscalInfo.anonymous();

        assertThatThrownBy(() -> ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                null,
                VoucherType.BOLETA,
                VoucherSerie.of("B001"),
                VoucherNumber.of(2),
                anon,
                Currency.PEN,
                bigTax,
                List.of(bigLine)
        )).isInstanceOf(CustomerFiscalDataMissingException.class);
    }

    @Test
    @DisplayName("Should successfully issue Credit Note and register CreditNoteIssuedEvent")
    void shouldIssueCreditNoteSuccessfully() {
        VoucherId origId = VoucherId.generate();
        CreditNoteReference ref = CreditNoteReference.of(
                origId,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                CreditNoteReason.ANULACION_DE_LA_OPERACION,
                "Cancelación de servicio"
        );

        ElectronicVoucher creditNote = ElectronicVoucher.issueCreditNote(
                tenantId,
                branchId,
                customerId,
                null,
                VoucherSerie.of("FC01"),
                VoucherNumber.of(1),
                corporateCustomer,
                Currency.PEN,
                sampleTaxCalculation,
                ref,
                List.of(sampleLine)
        );

        assertThat(creditNote.getVoucherType()).isEqualTo(VoucherType.NOTA_CREDITO);
        assertThat(creditNote.getCreditNoteReference()).contains(ref);
        assertThat(creditNote.domainEvents()).hasSize(1);
        assertThat(creditNote.domainEvents()).first().isInstanceOf(CreditNoteIssuedEvent.class);
    }

    @Test
    @DisplayName("Should reject voucher with line amount mismatch against header total")
    void shouldRejectVoucherWithArithmeticMismatch() {
        TaxCalculation mismatchedTax = TaxCalculation.of(
                Money.soles(200.00),
                Money.soles(36.00),
                Money.soles(236.00) // Line is 118.00!
        );

        assertThatThrownBy(() -> ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                null,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                corporateCustomer,
                Currency.PEN,
                mismatchedTax,
                List.of(sampleLine)
        )).isInstanceOf(InvalidVoucherAmountException.class);
    }

    @Test
    @DisplayName("Should reject voucher with empty lines")
    void shouldRejectVoucherWithEmptyLines() {
        assertThatThrownBy(() -> ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                null,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                corporateCustomer,
                Currency.PEN,
                sampleTaxCalculation,
                Collections.emptyList()
        )).isInstanceOf(InvalidVoucherAmountException.class);
    }

    @Test
    @DisplayName("Should transition voucher to ACCEPTED_SUNAT and emit event")
    void shouldTransitionToAcceptedBySunat() {
        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId, branchId, customerId, null, VoucherType.FACTURA,
                VoucherSerie.of("F001"), VoucherNumber.of(1), corporateCustomer,
                Currency.PEN, sampleTaxCalculation, List.of(sampleLine)
        );
        voucher.clearDomainEvents();

        DigitalReceiptUrls urls = DigitalReceiptUrls.of("pdfUrl", "xmlUrl", "cdrUrl");
        voucher.markAcceptedBySunat("hash123", "Aceptada", urls);

        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.ACCEPTED_SUNAT);
        assertThat(voucher.getSunatResponse()).isPresent();
        assertThat(voucher.getDigitalReceiptUrls().pdfUrl()).isEqualTo("pdfUrl");

        assertThat(voucher.domainEvents()).hasSize(1);
        assertThat(voucher.domainEvents()).first().isInstanceOf(VoucherAcceptedBySunatEvent.class);

        // Cannot reject once accepted
        assertThatThrownBy(() -> voucher.markRejectedBySunat("ERR", "Msg"))
                .isInstanceOf(VoucherImmutableException.class);
    }

    @Test
    @DisplayName("Should transition voucher to REJECTED_SUNAT and emit event")
    void shouldTransitionToRejectedBySunat() {
        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId, branchId, customerId, null, VoucherType.FACTURA,
                VoucherSerie.of("F001"), VoucherNumber.of(1), corporateCustomer,
                Currency.PEN, sampleTaxCalculation, List.of(sampleLine)
        );
        voucher.clearDomainEvents();

        voucher.markRejectedBySunat("2012", "RUC no activo");

        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.REJECTED_SUNAT);
        assertThat(voucher.domainEvents()).hasSize(1);
        assertThat(voucher.domainEvents()).first().isInstanceOf(VoucherRejectedBySunatEvent.class);
    }

    @Test
    @DisplayName("Should void voucher and emit VoucherVoidedEvent")
    void shouldVoidVoucher() {
        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId, branchId, customerId, null, VoucherType.FACTURA,
                VoucherSerie.of("F001"), VoucherNumber.of(1), corporateCustomer,
                Currency.PEN, sampleTaxCalculation, List.of(sampleLine)
        );
        voucher.clearDomainEvents();

        voucher.voidVoucher("Anulación por cancelación de trabajo", Instant.now());
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.VOIDED);
        assertThat(voucher.getVoidedInfo()).isPresent();

        assertThat(voucher.domainEvents()).hasSize(1);
        assertThat(voucher.domainEvents()).first().isInstanceOf(VoucherVoidedEvent.class);

        // Cannot accept once voided
        assertThatThrownBy(() -> voucher.markAcceptedBySunat("h", "d", null))
                .isInstanceOf(VoucherImmutableException.class);
    }

    @Test
    @DisplayName("Should record partial and full payments, and reject overpayment")
    void shouldRecordPaymentsAndTrackBalance() {
        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId, branchId, customerId, null, VoucherType.FACTURA,
                VoucherSerie.of("F001"), VoucherNumber.of(1), corporateCustomer,
                Currency.PEN, sampleTaxCalculation, List.of(sampleLine)
        );
        voucher.clearDomainEvents();

        // 1. Partial payment: S/ 50.00
        VoucherPayment payment1 = voucher.recordPayment(
                PaymentId.generate(),
                Money.soles(50.00),
                PaymentMethod.CASH,
                null
        );
        assertThat(payment1).isNotNull();
        assertThat(voucher.isFullyPaid()).isFalse();
        assertThat(voucher.getPendingBalance()).isEqualTo(Money.soles(68.00));
        assertThat(voucher.domainEvents()).hasSize(1);
        assertThat(voucher.domainEvents()).first().isInstanceOf(VoucherPaymentRegisteredEvent.class);

        // 2. Overpayment attempt: S/ 70.00 > remaining 68.00
        assertThatThrownBy(() -> voucher.recordPayment(
                PaymentId.generate(),
                Money.soles(70.00),
                PaymentMethod.CASH,
                null
        )).isInstanceOf(VoucherAlreadyPaidException.class);

        // 3. Final payment: S/ 68.00
        voucher.recordPayment(
                PaymentId.generate(),
                Money.soles(68.00),
                PaymentMethod.DIGITAL_WALLET_YAPE,
                "OPER-12345"
        );
        assertThat(voucher.isFullyPaid()).isTrue();
        assertThat(voucher.getPendingBalance()).isEqualTo(Money.soles(0.00));

        // 4. Payment on fully paid voucher throws exception
        assertThatThrownBy(() -> voucher.recordPayment(
                PaymentId.generate(),
                Money.soles(10.00),
                PaymentMethod.CASH,
                null
        )).isInstanceOf(VoucherAlreadyPaidException.class);
    }
}
