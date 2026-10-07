package com.andeva.atelier.platform.invoicing.application;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CashFlowPdfGeneratorPort;
import com.andeva.atelier.platform.invoicing.application.internal.queryservices.CashFlowQueryServiceImpl;
import com.andeva.atelier.platform.invoicing.application.internal.queryservices.ElectronicVoucherQueryServiceImpl;
import com.andeva.atelier.platform.invoicing.application.internal.queryservices.VoucherPaymentQueryServiceImpl;
import com.andeva.atelier.platform.invoicing.application.queryservices.CashFlowQueryService;
import com.andeva.atelier.platform.invoicing.application.queryservices.ElectronicVoucherQueryService;
import com.andeva.atelier.platform.invoicing.application.queryservices.VoucherPaymentQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetCashFlowSummaryQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherByIdQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherBySerieAndNumberQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherPaymentsQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVouchersByTenantQuery;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.invoicing.domain.repositories.VoucherPaymentRepository;
import com.andeva.atelier.platform.invoicing.application.internal.queryservices.SeriesConfigurationQueryServiceImpl;
import com.andeva.atelier.platform.invoicing.application.queryservices.SeriesConfigurationQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetActiveSeriesQuery;
import com.andeva.atelier.platform.invoicing.domain.repositories.SeriesConfigurationRepository;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
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
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for all Invoicing Query Service implementations.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Invoicing Query Services Tests")
class InvoicingQueryServicesTest {

    private ElectronicVoucherRepository voucherRepository;
    private VoucherPaymentRepository paymentRepository;
    private SeriesConfigurationRepository seriesRepository;
    private CashFlowPdfGeneratorPort pdfGeneratorPort;

    private ElectronicVoucherQueryService voucherQueryService;
    private VoucherPaymentQueryService paymentQueryService;
    private SeriesConfigurationQueryService seriesQueryService;
    private CashFlowQueryService cashFlowQueryService;

    private final TenantId tenantId = TenantId.generate();
    private final BranchId branchId = BranchId.generate();
    private final CustomerId customerId = CustomerId.generate();
    private final WorkOrderId workOrderId = WorkOrderId.generate();
    private final PeruvianTaxCalculationEngine taxEngine = new PeruvianTaxCalculationEngine();

    private ElectronicVoucher voucher;
    private final VoucherId voucherId = VoucherId.generate();

    @BeforeEach
    void setUp() {
        voucherRepository = Mockito.mock(ElectronicVoucherRepository.class);
        paymentRepository = Mockito.mock(VoucherPaymentRepository.class);
        seriesRepository = Mockito.mock(SeriesConfigurationRepository.class);
        pdfGeneratorPort = Mockito.mock(CashFlowPdfGeneratorPort.class);

        voucherQueryService = new ElectronicVoucherQueryServiceImpl(voucherRepository);
        paymentQueryService = new VoucherPaymentQueryServiceImpl(paymentRepository);
        seriesQueryService = new SeriesConfigurationQueryServiceImpl(seriesRepository);
        cashFlowQueryService = new CashFlowQueryServiceImpl(paymentRepository, pdfGeneratorPort);

        voucher = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                workOrderId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                CustomerFiscalInfo.of(TaxId.ruc("20100070970"), "SUNAT", "LIMA", DocumentType.RUC),
                Currency.PEN,
                TaxCalculation.of(Money.of(new BigDecimal("100.00"), Currency.PEN), Money.of(new BigDecimal("18.00"), Currency.PEN), Money.of(new BigDecimal("118.00"), Currency.PEN)),
                List.of(taxEngine.calculateLine(null, voucherId, Optional.empty(), VoucherItemType.SERVICE, "Svc", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );
    }

    @Test
    @DisplayName("Should query voucher by ID, Serie/Number, and WorkOrderId")
    void shouldQueryVouchers() {
        when(voucherRepository.findById(voucher.getId())).thenReturn(Optional.of(voucher));
        when(voucherRepository.findByTenantIdAndSerieAndNumber(tenantId, VoucherSerie.of("F001"), VoucherNumber.of(1)))
                .thenReturn(Optional.of(voucher));
        when(voucherRepository.findAllByWorkOrderId(workOrderId)).thenReturn(List.of(voucher));

        // By ID
        Optional<ElectronicVoucher> byId = voucherQueryService.handle(new GetVoucherByIdQuery(voucher.getId()));
        assertThat(byId).contains(voucher);

        // By Serie and Number
        Optional<ElectronicVoucher> bySerieNum = voucherQueryService.handle(
                new GetVoucherBySerieAndNumberQuery(tenantId, VoucherSerie.of("F001"), VoucherNumber.of(1))
        );
        assertThat(bySerieNum).contains(voucher);

        // By WorkOrderId
        List<ElectronicVoucher> byWo = voucherQueryService.getVouchersByWorkOrderId(workOrderId);
        assertThat(byWo).containsExactly(voucher);
    }

    @Test
    @DisplayName("Should query vouchers by tenant with date range and voucher type filtering")
    void shouldQueryVouchersByTenant() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(voucherRepository.findAllByTenantIdAndDateRange(tenantId, from, to))
                .thenReturn(List.of(voucher));

        // Query with type filter matching
        GetVouchersByTenantQuery query = new GetVouchersByTenantQuery(tenantId, Optional.of(VoucherType.FACTURA), from, to);
        List<ElectronicVoucher> result = voucherQueryService.handle(query);
        assertThat(result).hasSize(1);

        // Query with type filter not matching
        GetVouchersByTenantQuery boletaQuery = new GetVouchersByTenantQuery(tenantId, Optional.of(VoucherType.BOLETA), from, to);
        List<ElectronicVoucher> boletaResult = voucherQueryService.handle(boletaQuery);
        assertThat(boletaResult).isEmpty();
    }

    @Test
    @DisplayName("Should query voucher payments by voucherId and daily branch reconciliation")
    void shouldQueryVoucherPayments() {
        VoucherPayment payment = VoucherPayment.record(
                PaymentId.generate(),
                voucher.getId(),
                tenantId,
                branchId,
                Money.of(new BigDecimal("118.00"), Currency.PEN),
                PaymentMethod.CASH,
                null
        );

        when(paymentRepository.findAllByVoucherId(voucher.getId())).thenReturn(List.of(payment));
        when(paymentRepository.findAllByBranchIdAndDate(eq(branchId), any(LocalDate.class)))
                .thenReturn(List.of(payment));

        List<VoucherPayment> byVoucher = paymentQueryService.handle(new GetVoucherPaymentsQuery(voucher.getId()));
        assertThat(byVoucher).containsExactly(payment);

        List<VoucherPayment> daily = paymentQueryService.getDailyPaymentsByBranch(branchId, LocalDate.now());
        assertThat(daily).containsExactly(payment);
    }

    @Test
    @DisplayName("Should calculate cash flow summary and export PDF")
    void shouldCalculateCashFlowAndExportPdf() {
        LocalDate from = LocalDate.now().minusMonths(1);
        LocalDate to = LocalDate.now();

        byte[] samplePdf = "%PDF-1.4 sample content".getBytes();
        when(pdfGeneratorPort.generateCashFlowPdf(eq(tenantId.value()), any(), any()))
                .thenReturn(samplePdf);

        CashFlowSummary summary = cashFlowQueryService.handle(new GetCashFlowSummaryQuery(tenantId, from, to));
        assertThat(summary).isNotNull();
        assertThat(summary.grossRevenue().currency()).isEqualTo(Currency.PEN);

        byte[] pdf = cashFlowQueryService.exportCashFlowPdf(tenantId, from, to);
        assertThat(pdf).isEqualTo(samplePdf);
    }

    @Test
    @DisplayName("Should query series configurations by branch, id, and active status")
    void shouldQuerySeriesConfigurations() {
        SeriesConfiguration series = SeriesConfiguration.create(
                tenantId,
                branchId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                0
        );

        when(seriesRepository.findAllByTenantIdAndBranchId(tenantId, branchId))
                .thenReturn(List.of(series));
        when(seriesRepository.findById(series.getId()))
                .thenReturn(Optional.of(series));
        when(seriesRepository.findByTenantIdAndBranchIdAndVoucherTypeAndActive(tenantId, branchId, VoucherType.FACTURA))
                .thenReturn(Optional.of(series));

        List<SeriesConfiguration> byBranch = seriesQueryService.getSeriesByBranch(tenantId, branchId);
        assertThat(byBranch).containsExactly(series);

        Optional<SeriesConfiguration> byId = seriesQueryService.getSeriesById(series.getId());
        assertThat(byId).contains(series);

        Optional<SeriesConfiguration> active = seriesQueryService.getActiveSeriesByBranchAndType(tenantId, branchId, VoucherType.FACTURA);
        assertThat(active).contains(series);

        Optional<SeriesConfiguration> handled = seriesQueryService.handle(new GetActiveSeriesQuery(tenantId, branchId, VoucherType.FACTURA));
        assertThat(handled).contains(series);
    }
}
