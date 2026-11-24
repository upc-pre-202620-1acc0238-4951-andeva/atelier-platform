package com.andeva.atelier.platform.invoicing.application;

import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.BranchAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.TenantAclDto;
import com.andeva.atelier.platform.invoicing.application.internal.queryservices.CashFlowQueryServiceImpl;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetCashFlowSummaryQuery;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.invoicing.domain.repositories.VoucherPaymentRepository;
import com.andeva.atelier.platform.invoicing.infrastructure.external.reporting.openpdf.OpenPdfCashFlowReportAdapter;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.MeasurementUnit;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxIdType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AT-28 / US41 - Cash Flow PDF Export for External Accounting Audit")
class CashFlowReportExportTest {

    @Mock private VoucherPaymentRepository paymentRepository;
    @Mock private ElectronicVoucherRepository voucherRepository;
    @Mock private TenancyContextFacade tenancyContextFacade;

    private CashFlowQueryServiceImpl queryService;
    private TenantId tenantId;
    private BranchId surquilloBranch;
    private BranchId mirafloresBranch;

    @BeforeEach
    void setUp() {
        queryService = new CashFlowQueryServiceImpl(
                paymentRepository,
                new OpenPdfCashFlowReportAdapter(),
                voucherRepository,
                tenancyContextFacade
        );
        tenantId = TenantId.generate();
        surquilloBranch = BranchId.generate();
        mirafloresBranch = BranchId.generate();
    }

    private ElectronicVoucher sampleAcceptedVoucher(BranchId branchId, int number, BigDecimal laborWithIgv, BigDecimal partsWithIgv) {
        VoucherId voucherId = VoucherId.generate();
        VoucherLine laborLine = VoucherLine.create(null, voucherId, UUID.randomUUID(), VoucherItemType.SERVICE, "Mano de obra", Quantity.of(BigDecimal.ONE, MeasurementUnit.UNIT), Money.of(laborWithIgv, Currency.PEN));
        VoucherLine partLine = VoucherLine.create(null, voucherId, UUID.randomUUID(), VoucherItemType.PRODUCT, "Repuesto OEM", Quantity.of(BigDecimal.ONE, MeasurementUnit.UNIT), Money.of(partsWithIgv, Currency.PEN));

        BigDecimal total = laborWithIgv.add(partsWithIgv);
        BigDecimal igv = laborLine.getIgvAmount().amount().add(partLine.getIgvAmount().amount());
        BigDecimal subtotal = total.subtract(igv);

        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId,
                branchId,
                CustomerId.generate(),
                null,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(number),
                CustomerFiscalInfo.of(new TaxId("20612345679", TaxIdType.RUC), "CLIENTE S.A.C.", "Av. Lima 100", DocumentType.RUC),
                Currency.PEN,
                TaxCalculation.of(Money.of(subtotal, Currency.PEN), Money.of(igv, Currency.PEN), Money.of(total, Currency.PEN)),
                List.of(laborLine, partLine)
        );
        voucher.markAcceptedBySunat("hash-" + number, "Aceptada por SUNAT", DigitalReceiptUrls.empty());
        return voucher;
    }

    @Test
    @DisplayName("Scenario 1: Monthly single-branch cash flow PDF includes labor, parts, IGV breakdown and letterhead")
    void exportsSingleBranchMonthlyPdfWithFiscalBreakdown() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        ElectronicVoucher voucher = sampleAcceptedVoucher(surquilloBranch, 1, new BigDecimal("236.00"), new BigDecimal("118.00"));
        VoucherPayment payment = VoucherPayment.record(
                null, voucher.getId(), tenantId, surquilloBranch,
                Money.of(new BigDecimal("354.00"), Currency.PEN),
                PaymentMethod.DIGITAL_WALLET_YAPE, "OP-99100"
        );

        when(paymentRepository.findByBranchIdAndDateRange(eq(surquilloBranch), any(), any())).thenReturn(List.of(payment));
        when(voucherRepository.findAllByTenantIdAndBranchIdAndDateRange(tenantId, surquilloBranch, from, to)).thenReturn(List.of(voucher));
        when(tenancyContextFacade.fetchTenantById(tenantId.value()))
                .thenReturn(Optional.of(new TenantAclDto(tenantId.value(), "Taller Suarez", "Suarez Automotriz S.A.C.", "20612345679", "ACTIVE")));
        when(tenancyContextFacade.fetchBranchById(surquilloBranch.value()))
                .thenReturn(Optional.of(new BranchAclDto(surquilloBranch.value(), tenantId.value(), "Sede Surquillo", "0001", true)));

        CashFlowSummary summary = queryService.handle(new GetCashFlowSummaryQuery(tenantId, surquilloBranch, from, to));
        assertThat(summary.grossRevenue().amount()).isEqualByComparingTo("354.00");
        assertThat(summary.laborRevenue().amount()).isEqualByComparingTo("236.00");
        assertThat(summary.partsRevenue().amount()).isEqualByComparingTo("118.00");
        assertThat(summary.igvCollected().amount()).isEqualByComparingTo("54.00");
        assertThat(summary.branchBreakdowns()).isEmpty();

        byte[] pdfBytes = queryService.exportCashFlowPdf(tenantId, surquilloBranch, from, to);
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(500);
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("Scenario 2: Multi-branch corporate annual balance aggregates and discriminates revenue by branch")
    void exportsMultiBranchConsolidatedAnnualPdfDiscriminatingByBranch() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);

        ElectronicVoucher vSurquillo = sampleAcceptedVoucher(surquilloBranch, 1, new BigDecimal("236.00"), new BigDecimal("118.00"));
        ElectronicVoucher vMiraflores = sampleAcceptedVoucher(mirafloresBranch, 2, new BigDecimal("590.00"), new BigDecimal("118.00"));

        VoucherPayment p1 = VoucherPayment.record(null, vSurquillo.getId(), tenantId, surquilloBranch,
                Money.of(new BigDecimal("354.00"), Currency.PEN), PaymentMethod.CREDIT_CARD, "POS-1");
        VoucherPayment p2 = VoucherPayment.record(null, vMiraflores.getId(), tenantId, mirafloresBranch,
                Money.of(new BigDecimal("708.00"), Currency.PEN), PaymentMethod.BANK_TRANSFER, "TRF-2");

        when(paymentRepository.findByTenantIdAndDateRange(eq(tenantId), any(), any())).thenReturn(List.of(p1, p2));
        when(voucherRepository.findAllByTenantIdAndDateRange(tenantId, from, to)).thenReturn(List.of(vSurquillo, vMiraflores));
        when(tenancyContextFacade.fetchTenantById(tenantId.value()))
                .thenReturn(Optional.of(new TenantAclDto(tenantId.value(), "Taller Suarez", "Suarez Automotriz S.A.C.", "20612345679", "ACTIVE")));
        when(tenancyContextFacade.fetchBranchById(surquilloBranch.value()))
                .thenReturn(Optional.of(new BranchAclDto(surquilloBranch.value(), tenantId.value(), "Sede Surquillo", "0001", true)));
        when(tenancyContextFacade.fetchBranchById(mirafloresBranch.value()))
                .thenReturn(Optional.of(new BranchAclDto(mirafloresBranch.value(), tenantId.value(), "Sede Miraflores", "0002", true)));

        CashFlowSummary summary = queryService.handle(new GetCashFlowSummaryQuery(tenantId, null, from, to));
        assertThat(summary.grossRevenue().amount()).isEqualByComparingTo("1062.00");
        assertThat(summary.laborRevenue().amount()).isEqualByComparingTo("826.00");
        assertThat(summary.partsRevenue().amount()).isEqualByComparingTo("236.00");
        assertThat(summary.igvCollected().amount()).isEqualByComparingTo("162.00");
        assertThat(summary.branchBreakdowns()).hasSize(2);
        assertThat(summary.branchBreakdowns())
                .extracting(b -> b.branchName() + ":" + b.totalRevenue().amount().toPlainString())
                .containsExactly("Sede Surquillo:354.00", "Sede Miraflores:708.00");

        byte[] pdfBytes = queryService.exportCashFlowPdf(tenantId, null, from, to);
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(500);
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
    }
}
