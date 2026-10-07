package com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.*;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.*;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.*;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for Invoicing Resource Assemblers:
 * {@link ElectronicVoucherResourceAssembler},
 * {@link VoucherPaymentResourceAssembler},
 * {@link SeriesConfigurationResourceAssembler},
 * {@link CashFlowResourceAssembler}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("InvoicingResourceAssemblers Unit Tests")
class InvoicingResourceAssemblersTest {

    private ElectronicVoucherResourceAssembler voucherAssembler;
    private VoucherPaymentResourceAssembler paymentAssembler;
    private SeriesConfigurationResourceAssembler seriesAssembler;
    private CashFlowResourceAssembler cashFlowAssembler;

    private TenantId tenantId;
    private BranchId branchId;
    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        voucherAssembler = new ElectronicVoucherResourceAssembler();
        paymentAssembler = new VoucherPaymentResourceAssembler();
        seriesAssembler = new SeriesConfigurationResourceAssembler();
        cashFlowAssembler = new CashFlowResourceAssembler();

        tenantId = TenantId.generate();
        branchId = BranchId.generate();
        customerId = CustomerId.generate();
    }

    @Test
    @DisplayName("ElectronicVoucherResourceAssembler maps aggregate to ElectronicVoucherResource")
    void shouldAssembleElectronicVoucher() {
        TaxCalculation taxCalc = TaxCalculation.of(
                Money.soles(200.00),
                Money.soles(36.00),
                Money.soles(236.00)
        );

        CustomerFiscalInfo customer = CustomerFiscalInfo.of(
                TaxId.ruc("20100070970"),
                "Corporacion Los Andes S.A.C.",
                "Av. Primavera 123, Lima",
                DocumentType.RUC
        );

        VoucherId voucherId = VoucherId.generate();
        VoucherLine line = VoucherLine.create(
                null,
                voucherId,
                UUID.randomUUID(),
                VoucherItemType.SERVICE,
                "Mantenimiento General",
                Quantity.ofUnits(2),
                Money.soles(118.00)
        );

        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                null,
                VoucherType.FACTURA,
                new VoucherSerie("F001"),
                new VoucherNumber(42),
                customer,
                Currency.PEN,
                taxCalc,
                List.of(line)
        );

        ElectronicVoucherResource resource = voucherAssembler.toResource(voucher);

        assertThat(resource).isNotNull();
        assertThat(resource.id()).isEqualTo(voucher.getId().value());
        assertThat(resource.tenantId()).isEqualTo(tenantId.value());
        assertThat(resource.branchId()).isEqualTo(branchId.value());
        assertThat(resource.customerId()).isEqualTo(customerId.value());
        assertThat(resource.serie()).isEqualTo("F001");
        assertThat(resource.number()).isEqualTo(42);
        assertThat(resource.voucherType()).isEqualTo("01");
        assertThat(resource.status()).isEqualTo(VoucherStatus.ISSUED.name());
        assertThat(resource.customerInfo()).isNotNull();
        assertThat(resource.customerInfo().taxId()).isEqualTo("20100070970");
        assertThat(resource.customerInfo().legalName()).isEqualTo("Corporacion Los Andes S.A.C.");
        assertThat(resource.lines()).hasSize(1);
        assertThat(resource.lines().get(0).description()).isEqualTo("Mantenimiento General");
        assertThat(resource.totalAmount()).isEqualByComparingTo(new BigDecimal("236.00"));

        List<ElectronicVoucherResource> resources = voucherAssembler.toResourceList(List.of(voucher));
        assertThat(resources).hasSize(1);
        assertThat(voucherAssembler.toResource(null)).isNull();
    }

    @Test
    @DisplayName("VoucherPaymentResourceAssembler maps VoucherPayment to VoucherPaymentResource")
    void shouldAssembleVoucherPayment() {
        VoucherId voucherId = VoucherId.generate();
        VoucherPayment payment = VoucherPayment.record(
                PaymentId.generate(),
                voucherId,
                tenantId,
                branchId,
                Money.soles(100.00),
                PaymentMethod.DIGITAL_WALLET_YAPE,
                "YAPE-TRANS-987654"
        );

        VoucherPaymentResource resource = paymentAssembler.toResource(payment);

        assertThat(resource).isNotNull();
        assertThat(resource.id()).isEqualTo(payment.getId().value());
        assertThat(resource.voucherId()).isEqualTo(voucherId.value());
        assertThat(resource.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(resource.currency()).isEqualTo("PEN");
        assertThat(resource.paymentMethod()).isEqualTo("DIGITAL_WALLET_YAPE");
        assertThat(resource.status()).isEqualTo(PaymentStatus.COMPLETED.name());
        assertThat(resource.transactionReference()).isEqualTo("YAPE-TRANS-987654");

        List<VoucherPaymentResource> resources = paymentAssembler.toResourceList(List.of(payment));
        assertThat(resources).hasSize(1);
        assertThat(paymentAssembler.toResource(null)).isNull();
    }

    @Test
    @DisplayName("SeriesConfigurationResourceAssembler maps SeriesConfiguration to SeriesConfigurationResource")
    void shouldAssembleSeriesConfiguration() {
        SeriesConfiguration series = SeriesConfiguration.create(
                tenantId,
                branchId,
                VoucherType.BOLETA,
                new VoucherSerie("B001"),
                10
        );

        SeriesConfigurationResource resource = seriesAssembler.toResource(series);

        assertThat(resource).isNotNull();
        assertThat(resource.id()).isEqualTo(series.getId().value());
        assertThat(resource.tenantId()).isEqualTo(tenantId.value());
        assertThat(resource.branchId()).isEqualTo(branchId.value());
        assertThat(resource.serie()).isEqualTo("B001");
        assertThat(resource.voucherType()).isEqualTo("03");
        assertThat(resource.currentCorrelative()).isEqualTo(10);
        assertThat(resource.active()).isTrue();

        List<SeriesConfigurationResource> resources = seriesAssembler.toResourceList(List.of(series));
        assertThat(resources).hasSize(1);
        assertThat(seriesAssembler.toResource(null)).isNull();
    }

    @Test
    @DisplayName("CashFlowResourceAssembler maps CashFlowMovement and CashFlowSummary")
    void shouldAssembleCashFlow() {
        CashFlowMovement movement = new CashFlowMovement(
                UUID.randomUUID(),
                Instant.now(),
                "INCOME",
                "BILLING",
                "Cobro de Factura F001-00000042",
                "F001-00000042",
                new BigDecimal("236.00"),
                new BigDecimal("1236.00")
        );

        CashFlowMovementResource movementResource = cashFlowAssembler.toMovementResource(movement);
        assertThat(movementResource).isNotNull();
        assertThat(movementResource.type()).isEqualTo("INCOME");
        assertThat(movementResource.category()).isEqualTo("BILLING");
        assertThat(movementResource.amount()).isEqualByComparingTo(new BigDecimal("236.00"));
        assertThat(movementResource.runningBalance()).isEqualByComparingTo(new BigDecimal("1236.00"));

        CashFlowSummary summary = new CashFlowSummary(
                Money.soles(5000.00),
                Money.soles(1500.00),
                Money.soles(500.00),
                Money.soles(3000.00)
        );

        CashFlowSummaryResource summaryResource = cashFlowAssembler.toResource(summary, List.of(movement));
        assertThat(summaryResource).isNotNull();
        assertThat(summaryResource.totalIncome()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(summaryResource.totalExpenses()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(summaryResource.netCashFlow()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(summaryResource.movements()).hasSize(1);

        assertThat(cashFlowAssembler.toMovementResource(null)).isNull();
        assertThat(cashFlowAssembler.toResource(null, null)).isNull();
    }
}
