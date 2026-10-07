package com.andeva.atelier.platform.invoicing.infrastructure;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers.ElectronicVoucherPersistenceAssembler;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers.SeriesConfigurationPersistenceAssembler;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers.VoucherPaymentPersistenceAssembler;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.ElectronicVoucherPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.SeriesConfigurationPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.VoucherPaymentPersistenceEntity;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests verifying lossless bidirectional conversion for Invoicing Persistence Assemblers.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Invoicing Persistence Assemblers Unit Tests")
class InvoicingPersistenceAssemblersTest {

    private VoucherPaymentPersistenceAssembler paymentAssembler;
    private SeriesConfigurationPersistenceAssembler seriesAssembler;
    private ElectronicVoucherPersistenceAssembler voucherAssembler;
    private PeruvianTaxCalculationEngine taxEngine;

    @BeforeEach
    void setUp() {
        paymentAssembler = new VoucherPaymentPersistenceAssembler();
        seriesAssembler = new SeriesConfigurationPersistenceAssembler();
        voucherAssembler = new ElectronicVoucherPersistenceAssembler(paymentAssembler);
        taxEngine = new PeruvianTaxCalculationEngine();
    }

    @Test
    @DisplayName("Should convert SeriesConfiguration bidirectionally")
    void testSeriesConfigurationAssembler() {
        SeriesConfiguration domain = SeriesConfiguration.create(
                TenantId.generate(),
                BranchId.generate(),
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                100
        );

        SeriesConfigurationPersistenceEntity entity = seriesAssembler.toEntity(domain);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId().value());
        assertThat(entity.getSerie()).isEqualTo("F001");
        assertThat(entity.getCurrentCorrelative()).isEqualTo(100);
        assertThat(entity.isActive()).isTrue();

        SeriesConfiguration reconstituted = seriesAssembler.toDomain(entity);
        assertThat(reconstituted).isNotNull();
        assertThat(reconstituted.getId()).isEqualTo(domain.getId());
        assertThat(reconstituted.getSerie()).isEqualTo(domain.getSerie());
        assertThat(reconstituted.getCurrentCorrelative()).isEqualTo(100);

        assertThat(seriesAssembler.toEntity(null)).isNull();
        assertThat(seriesAssembler.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should convert VoucherPayment bidirectionally")
    void testVoucherPaymentAssembler() {
        VoucherId voucherId = VoucherId.generate();
        TenantId tenantId = TenantId.generate();
        BranchId branchId = BranchId.generate();

        VoucherPayment payment = VoucherPayment.record(
                PaymentId.generate(),
                voucherId,
                tenantId,
                branchId,
                Money.of(new BigDecimal("150.00"), Currency.PEN),
                PaymentMethod.DIGITAL_WALLET_YAPE,
                "TRX-777888"
        );

        ElectronicVoucherPersistenceEntity voucherEntity = new ElectronicVoucherPersistenceEntity();
        voucherEntity.setId(voucherId.value());

        VoucherPaymentPersistenceEntity entity = paymentAssembler.toEntity(payment, voucherEntity);
        assertThat(entity).isNotNull();
        assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(entity.getPaymentMethod()).isEqualTo("DIGITAL_WALLET_YAPE");
        assertThat(entity.getTransactionReference()).isEqualTo("TRX-777888");

        VoucherPayment reconstituted = paymentAssembler.toDomain(entity);
        assertThat(reconstituted).isNotNull();
        assertThat(reconstituted.getId()).isEqualTo(payment.getId());
        assertThat(reconstituted.getAmount()).isEqualTo(payment.getAmount());
        assertThat(reconstituted.getPaymentMethod()).isEqualTo(PaymentMethod.DIGITAL_WALLET_YAPE);

        assertThat(paymentAssembler.toEntity(null, voucherEntity)).isNull();
        assertThat(paymentAssembler.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should convert ElectronicVoucher aggregate with lines and payments bidirectionally")
    void testElectronicVoucherAssembler() {
        TenantId tenantId = TenantId.generate();
        BranchId branchId = BranchId.generate();
        CustomerId customerId = CustomerId.generate();
        WorkOrderId workOrderId = WorkOrderId.generate();
        VoucherId voucherId = VoucherId.generate();

        CustomerFiscalInfo fiscal = CustomerFiscalInfo.of(
                TaxId.ruc("20100070970"),
                "SUPERMARKET S.A.",
                "AV LARCO 123",
                DocumentType.RUC
        );

        TaxCalculation taxCalc = TaxCalculation.of(
                Money.of(new BigDecimal("100.00"), Currency.PEN),
                Money.of(new BigDecimal("18.00"), Currency.PEN),
                Money.of(new BigDecimal("118.00"), Currency.PEN)
        );

        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                workOrderId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                fiscal,
                Currency.PEN,
                taxCalc,
                List.of(taxEngine.calculateLine(null, voucherId, Optional.empty(), VoucherItemType.SERVICE, "Mano de obra", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );

        voucher.recordPayment(PaymentId.generate(), Money.of(new BigDecimal("118.00"), Currency.PEN), PaymentMethod.CASH, null);

        ElectronicVoucherPersistenceEntity entity = voucherAssembler.toEntity(voucher);
        assertThat(entity).isNotNull();
        assertThat(entity.getSerie()).isEqualTo("F001");
        assertThat(entity.getNumber()).isEqualTo(1);
        assertThat(entity.getTotalAmount()).isEqualByComparingTo(new BigDecimal("118.00"));
        assertThat(entity.getLines()).hasSize(1);
        assertThat(entity.getPayments()).hasSize(1);

        ElectronicVoucher reconstituted = voucherAssembler.toDomain(entity);
        assertThat(reconstituted).isNotNull();
        assertThat(reconstituted.getId()).isEqualTo(voucher.getId());
        assertThat(reconstituted.getSerie()).isEqualTo(voucher.getSerie());
        assertThat(reconstituted.getNumber()).isEqualTo(voucher.getNumber());
        assertThat(reconstituted.getLines()).hasSize(1);
        assertThat(reconstituted.getPayments()).hasSize(1);
        assertThat(reconstituted.isFullyPaid()).isTrue();

        assertThat(voucherAssembler.toEntity(null)).isNull();
        assertThat(voucherAssembler.toDomain(null)).isNull();
    }
}
