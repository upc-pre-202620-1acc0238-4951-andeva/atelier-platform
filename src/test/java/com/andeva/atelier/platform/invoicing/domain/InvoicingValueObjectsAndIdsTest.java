package com.andeva.atelier.platform.invoicing.domain;

import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidVoucherAmountException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;
import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherLineId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CreditNoteReference;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.SunatResponse;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoidedInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive unit test suite for Value Objects, Strongly Typed IDs,
 * and Enums in the Invoicing & Compliance Bounded Context.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Invoicing Value Objects and IDs Unit Tests")
class InvoicingValueObjectsAndIdsTest {

    @Test
    @DisplayName("Should create and validate strongly-typed IDs")
    void shouldCreateAndValidateStronglyTypedIds() {
        UUID rawUuid = UUID.randomUUID();

        VoucherId voucherId = VoucherId.of(rawUuid);
        assertThat(voucherId.value()).isEqualTo(rawUuid);
        assertThat(VoucherId.of(rawUuid.toString())).isEqualTo(voucherId);
        assertThat(VoucherId.generate()).isNotNull();

        SeriesConfigurationId seriesId = SeriesConfigurationId.of(rawUuid);
        assertThat(seriesId.value()).isEqualTo(rawUuid);
        assertThat(SeriesConfigurationId.of(rawUuid.toString())).isEqualTo(seriesId);
        assertThat(SeriesConfigurationId.generate()).isNotNull();

        PaymentId paymentId = PaymentId.of(rawUuid);
        assertThat(paymentId.value()).isEqualTo(rawUuid);
        assertThat(PaymentId.of(rawUuid.toString())).isEqualTo(paymentId);
        assertThat(PaymentId.generate()).isNotNull();

        VoucherLineId lineId = VoucherLineId.of(rawUuid);
        assertThat(lineId.value()).isEqualTo(rawUuid);
        assertThat(VoucherLineId.of(rawUuid.toString())).isEqualTo(lineId);
        assertThat(VoucherLineId.generate()).isNotNull();

        assertThatThrownBy(() -> new VoucherId(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SeriesConfigurationId(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PaymentId(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new VoucherLineId(null)).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"F001", "B001", "FC01", "BC01", "T001", "F123", "B999"})
    @DisplayName("Should accept valid 4-character fiscal series")
    void shouldAcceptValidFiscalSeries(String validSeries) {
        VoucherSerie serie = VoucherSerie.of(validSeries);
        assertThat(serie.value()).isEqualTo(validSeries);
    }

    @Test
    @DisplayName("Should normalize lowercase fiscal series to uppercase")
    void shouldNormalizeLowercaseSeries() {
        VoucherSerie serie = VoucherSerie.of("f001");
        assertThat(serie.value()).isEqualTo("F001");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "F", "F01", "F0001", "Z001", "X001", "1001", "F-01", "F@01"})
    @DisplayName("Should reject invalid fiscal series formats")
    void shouldRejectInvalidFiscalSeries(String invalidSeries) {
        assertThatThrownBy(() -> VoucherSerie.of(invalidSeries))
                .isInstanceOf(InvoicingDomainException.class);
    }

    @Test
    @DisplayName("Should format VoucherNumber to 8 digits with leading zeros")
    void shouldFormatVoucherNumberWithLeadingZeros() {
        VoucherNumber num1 = VoucherNumber.of(1);
        assertThat(num1.format()).isEqualTo("00000001");

        VoucherNumber num123 = VoucherNumber.of(123);
        assertThat(num123.format()).isEqualTo("00000123");

        VoucherNumber numMax = VoucherNumber.of(99_999_999);
        assertThat(numMax.format()).isEqualTo("99999999");

        VoucherNumber fromString = VoucherNumber.of(" 00000456 ");
        assertThat(fromString.value()).isEqualTo(456);
        assertThat(fromString.format()).isEqualTo("00000456");

        assertThat(num1.compareTo(num123)).isLessThan(0);
    }

    @Test
    @DisplayName("Should reject out-of-range VoucherNumbers")
    void shouldRejectOutOfRangeVoucherNumbers() {
        assertThatThrownBy(() -> VoucherNumber.of(0))
                .isInstanceOf(InvoicingDomainException.class);

        assertThatThrownBy(() -> VoucherNumber.of(-5))
                .isInstanceOf(InvoicingDomainException.class);

        assertThatThrownBy(() -> VoucherNumber.of(100_000_000))
                .isInstanceOf(InvoicingDomainException.class);

        assertThatThrownBy(() -> VoucherNumber.of("not-a-number"))
                .isInstanceOf(InvoicingDomainException.class);
    }

    @Test
    @DisplayName("Should create TaxCalculation and enforce arithmetic invariants")
    void shouldCreateTaxCalculationAndEnforceInvariants() {
        Money subtotal = Money.soles(100.00);
        Money igv = Money.soles(18.00);
        Money total = Money.soles(118.00);

        TaxCalculation calc = TaxCalculation.of(subtotal, igv, total);
        assertThat(calc.subtotal()).isEqualTo(subtotal);
        assertThat(calc.igvAmount()).isEqualTo(igv);
        assertThat(calc.totalAmount()).isEqualTo(total);
        assertThat(calc.igvRate()).isEqualByComparingTo(BigDecimal.valueOf(0.18));

        // Arithmetic mismatch
        Money wrongTotal = Money.soles(120.00);
        assertThatThrownBy(() -> TaxCalculation.of(subtotal, igv, wrongTotal))
                .isInstanceOf(InvalidVoucherAmountException.class);

        // Negative values
        Money negativeSubtotal = Money.soles(-10.00);
        assertThatThrownBy(() -> TaxCalculation.of(negativeSubtotal, igv, total))
                .isInstanceOf(InvalidVoucherAmountException.class);
    }

    @Test
    @DisplayName("Should create CustomerFiscalInfo and handle anonymous profile")
    void shouldCreateCustomerFiscalInfo() {
        TaxId ruc = TaxId.ruc("20100070970");
        CustomerFiscalInfo info = CustomerFiscalInfo.of(ruc, "TALLERES UNIDOS S.A.C.", "AV. INDUSTRIAL 123", DocumentType.RUC);

        assertThat(info.taxId()).isEqualTo(ruc);
        assertThat(info.legalName()).isEqualTo("TALLERES UNIDOS S.A.C.");
        assertThat(info.fiscalAddress()).isEqualTo("AV. INDUSTRIAL 123");
        assertThat(info.documentType()).isEqualTo(DocumentType.RUC);

        CustomerFiscalInfo anon = CustomerFiscalInfo.anonymous();
        assertThat(anon.taxId()).isNull();
        assertThat(anon.legalName()).isEqualTo("CLIENTES VARIOS");
        assertThat(anon.documentType()).isEqualTo(DocumentType.DNI);
    }

    @Test
    @DisplayName("Should create DigitalReceiptUrls and SunatResponse")
    void shouldCreateDigitalReceiptUrlsAndSunatResponse() {
        DigitalReceiptUrls urls = DigitalReceiptUrls.of(
                "https://api.atelier.pe/receipts/F001-1.pdf",
                "https://api.atelier.pe/receipts/F001-1.xml",
                "https://api.atelier.pe/receipts/R-F001-1.zip"
        );
        assertThat(urls.pdfUrl()).contains(".pdf");
        assertThat(urls.xmlUrl()).contains(".xml");
        assertThat(urls.cdrUrl()).contains(".zip");

        DigitalReceiptUrls empty = DigitalReceiptUrls.empty();
        assertThat(empty.pdfUrl()).isNull();

        SunatResponse accepted = SunatResponse.of("0", "La Factura F001-00000001 ha sido aceptada", "hash12345");
        assertThat(accepted.isAccepted()).isTrue();
        assertThat(accepted.responseCode()).isEqualTo("0");

        SunatResponse rejected = SunatResponse.of("2012", "RUC emisor no habilitado", "");
        assertThat(rejected.isAccepted()).isFalse();
    }

    @Test
    @DisplayName("Should create VoidedInfo and CreditNoteReference")
    void shouldCreateVoidedInfoAndCreditNoteReference() {
        VoidedInfo voided = VoidedInfo.of("Error en digitación de RUC");
        assertThat(voided.reason()).isEqualTo("Error en digitación de RUC");
        assertThat(voided.voidedAt()).isNotNull();

        assertThatThrownBy(() -> VoidedInfo.of("   "))
                .isInstanceOf(IllegalArgumentException.class);

        VoucherId origId = VoucherId.generate();
        VoucherSerie serie = VoucherSerie.of("F001");
        VoucherNumber num = VoucherNumber.of(5);

        CreditNoteReference ref = CreditNoteReference.of(
                origId,
                serie,
                num,
                CreditNoteReason.ANULACION_DE_LA_OPERACION,
                "Cancelación total de orden de trabajo"
        );
        assertThat(ref.referenceVoucherId()).isEqualTo(origId);
        assertThat(ref.reason()).isEqualTo(CreditNoteReason.ANULACION_DE_LA_OPERACION);
    }

    @Test
    @DisplayName("Should create CashFlowMovement and CashFlowSummary")
    void shouldCreateCashFlowMovementAndSummary() {
        UUID txId = UUID.randomUUID();
        Instant now = Instant.now();
        CashFlowMovement movement = CashFlowMovement.of(
                txId,
                now,
                "INCOME",
                "WORKSHOP_SERVICES",
                "Payment for Work Order #1002",
                "F001-00000012",
                BigDecimal.valueOf(350.00),
                BigDecimal.valueOf(1500.00)
        );
        assertThat(movement.transactionId()).isEqualTo(txId);
        assertThat(movement.amount()).isEqualByComparingTo(BigDecimal.valueOf(350.00));

        CashFlowSummary summary = CashFlowSummary.of(
                Money.soles(5000.00),
                Money.soles(1500.00),
                Money.soles(1200.00),
                Money.soles(2300.00)
        );
        assertThat(summary.grossRevenue().amount()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(summary.netCashFlow().amount()).isEqualByComparingTo(BigDecimal.valueOf(2300.00));
    }

    @Test
    @DisplayName("Should verify Domain Enums codes and descriptions")
    void shouldVerifyDomainEnums() {
        assertThat(VoucherType.FACTURA.getSunatCode()).isEqualTo("01");
        assertThat(VoucherType.BOLETA.getSunatCode()).isEqualTo("03");
        assertThat(VoucherType.NOTA_CREDITO.getSunatCode()).isEqualTo("07");
        assertThat(VoucherType.fromSunatCode("01")).isEqualTo(VoucherType.FACTURA);

        assertThat(CreditNoteReason.ANULACION_DE_LA_OPERACION.getSunatCode()).isEqualTo("01");
        assertThat(CreditNoteReason.fromSunatCode("01")).isEqualTo(CreditNoteReason.ANULACION_DE_LA_OPERACION);

        assertThat(DocumentType.RUC.getSunatCode()).isEqualTo("6");
        assertThat(DocumentType.fromSunatCode("6")).isEqualTo(DocumentType.RUC);

        assertThat(PaymentMethod.CASH.requiresTransactionReference()).isFalse();
        assertThat(PaymentMethod.DIGITAL_WALLET_YAPE.requiresTransactionReference()).isTrue();
        assertThat(PaymentMethod.BANK_TRANSFER.requiresTransactionReference()).isTrue();

        assertThat(PaymentStatus.values()).contains(PaymentStatus.PENDING, PaymentStatus.COMPLETED, PaymentStatus.REFUNDED);
        assertThat(VoucherStatus.values()).contains(VoucherStatus.DRAFT, VoucherStatus.ISSUED, VoucherStatus.ACCEPTED_SUNAT, VoucherStatus.REJECTED_SUNAT, VoucherStatus.VOIDED);
        assertThat(VoucherItemType.values()).contains(VoucherItemType.PRODUCT, VoucherItemType.SERVICE);
    }
}
