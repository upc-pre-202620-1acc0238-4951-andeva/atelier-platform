package com.andeva.atelier.platform.invoicing.infrastructure;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.adapters.ElectronicVoucherRepositoryImpl;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.adapters.SeriesConfigurationRepositoryImpl;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.adapters.VoucherPaymentRepositoryImpl;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers.ElectronicVoucherPersistenceAssembler;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers.SeriesConfigurationPersistenceAssembler;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers.VoucherPaymentPersistenceAssembler;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.ElectronicVoucherPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.SeriesConfigurationPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.VoucherPaymentPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.repositories.ElectronicVoucherPersistenceRepository;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.repositories.SeriesConfigurationPersistenceRepository;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.repositories.VoucherPaymentPersistenceRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests verifying repository adapters for Invoicing domain repositories.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Invoicing Repository Adapters Unit Tests")
class InvoicingRepositoryAdaptersTest {

    @Mock
    private ElectronicVoucherPersistenceRepository voucherJpaRepo;

    @Mock
    private VoucherPaymentPersistenceRepository paymentJpaRepo;

    @Mock
    private SeriesConfigurationPersistenceRepository seriesJpaRepo;

    private VoucherPaymentPersistenceAssembler paymentAssembler;
    private SeriesConfigurationPersistenceAssembler seriesAssembler;
    private ElectronicVoucherPersistenceAssembler voucherAssembler;

    private ElectronicVoucherRepositoryImpl voucherRepo;
    private VoucherPaymentRepositoryImpl paymentRepo;
    private SeriesConfigurationRepositoryImpl seriesRepo;

    private PeruvianTaxCalculationEngine taxEngine;

    @BeforeEach
    void setUp() {
        paymentAssembler = new VoucherPaymentPersistenceAssembler();
        seriesAssembler = new SeriesConfigurationPersistenceAssembler();
        voucherAssembler = new ElectronicVoucherPersistenceAssembler(paymentAssembler);
        taxEngine = new PeruvianTaxCalculationEngine();

        voucherRepo = new ElectronicVoucherRepositoryImpl(voucherJpaRepo, voucherAssembler);
        paymentRepo = new VoucherPaymentRepositoryImpl(paymentJpaRepo, voucherJpaRepo, paymentAssembler);
        seriesRepo = new SeriesConfigurationRepositoryImpl(seriesJpaRepo, seriesAssembler);
    }

    @Test
    @DisplayName("Should save and find SeriesConfiguration via adapter")
    void testSeriesConfigurationRepositoryAdapter() {
        TenantId tenantId = TenantId.generate();
        BranchId branchId = BranchId.generate();
        SeriesConfiguration config = SeriesConfiguration.create(
                tenantId, branchId, VoucherType.FACTURA, VoucherSerie.of("F001"), 0
        );

        SeriesConfigurationPersistenceEntity entity = seriesAssembler.toEntity(config);
        when(seriesJpaRepo.save(any(SeriesConfigurationPersistenceEntity.class))).thenReturn(entity);
        when(seriesJpaRepo.findById(config.getId().value())).thenReturn(Optional.of(entity));
        when(seriesJpaRepo.findActiveForUpdate(tenantId.value(), branchId.value(), "FACTURA"))
                .thenReturn(Optional.of(entity));

        SeriesConfiguration saved = seriesRepo.save(config);
        assertThat(saved).isNotNull();

        Optional<SeriesConfiguration> found = seriesRepo.findById(config.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(config.getId());

        Optional<SeriesConfiguration> active = seriesRepo.findByTenantIdAndBranchIdAndVoucherTypeAndActive(
                tenantId, branchId, VoucherType.FACTURA
        );
        assertThat(active).isPresent();

        verify(seriesJpaRepo).save(any());
        verify(seriesJpaRepo).findById(config.getId().value());
        verify(seriesJpaRepo).findActiveForUpdate(tenantId.value(), branchId.value(), "FACTURA");
    }

    @Test
    @DisplayName("Should save and find ElectronicVoucher via adapter")
    void testElectronicVoucherRepositoryAdapter() {
        TenantId tenantId = TenantId.generate();
        BranchId branchId = BranchId.generate();
        CustomerId customerId = CustomerId.generate();
        WorkOrderId workOrderId = WorkOrderId.generate();
        VoucherId voucherId = VoucherId.generate();

        CustomerFiscalInfo fiscal = CustomerFiscalInfo.of(
                TaxId.ruc("20100070970"), "CLIENTE SAC", "LIMA", DocumentType.RUC
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
                List.of(taxEngine.calculateLine(null, voucherId, Optional.empty(), VoucherItemType.SERVICE, "Servicio", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );

        ElectronicVoucherPersistenceEntity entity = voucherAssembler.toEntity(voucher);
        when(voucherJpaRepo.save(any())).thenReturn(entity);
        when(voucherJpaRepo.findById(voucher.getId().value())).thenReturn(Optional.of(entity));
        when(voucherJpaRepo.findByTenantIdAndSerieAndNumber(tenantId.value(), "F001", 1))
                .thenReturn(Optional.of(entity));
        when(voucherJpaRepo.findAllByWorkOrderId(workOrderId.value())).thenReturn(List.of(entity));
        when(voucherJpaRepo.existsByTenantIdAndSerieAndNumber(tenantId.value(), "F001", 1)).thenReturn(true);

        ElectronicVoucher saved = voucherRepo.save(voucher);
        assertThat(saved).isNotNull();

        Optional<ElectronicVoucher> byId = voucherRepo.findById(voucher.getId());
        assertThat(byId).isPresent();

        Optional<ElectronicVoucher> bySerieNum = voucherRepo.findByTenantIdAndSerieAndNumber(tenantId, VoucherSerie.of("F001"), VoucherNumber.of(1));
        assertThat(bySerieNum).isPresent();

        List<ElectronicVoucher> byWorkOrder = voucherRepo.findAllByWorkOrderId(workOrderId);
        assertThat(byWorkOrder).hasSize(1);

        boolean exists = voucherRepo.existsByTenantIdAndSerieAndNumber(tenantId, VoucherSerie.of("F001"), VoucherNumber.of(1));
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should save and find VoucherPayment via adapter")
    void testVoucherPaymentRepositoryAdapter() {
        VoucherId voucherId = VoucherId.generate();
        TenantId tenantId = TenantId.generate();
        BranchId branchId = BranchId.generate();

        VoucherPayment payment = VoucherPayment.record(
                PaymentId.generate(),
                voucherId,
                tenantId,
                branchId,
                Money.of(new BigDecimal("118.00"), Currency.PEN),
                PaymentMethod.CASH,
                null
        );

        ElectronicVoucherPersistenceEntity vEntity = new ElectronicVoucherPersistenceEntity();
        vEntity.setId(voucherId.value());
        VoucherPaymentPersistenceEntity entity = paymentAssembler.toEntity(payment, vEntity);
        when(voucherJpaRepo.findById(voucherId.value())).thenReturn(Optional.of(vEntity));
        when(paymentJpaRepo.save(any())).thenReturn(entity);
        when(paymentJpaRepo.findById(payment.getId().value())).thenReturn(Optional.of(entity));
        when(paymentJpaRepo.findAllByVoucherId(voucherId.value())).thenReturn(List.of(entity));

        VoucherPayment saved = paymentRepo.save(payment);
        assertThat(saved).isNotNull();

        Optional<VoucherPayment> byId = paymentRepo.findById(payment.getId());
        assertThat(byId).isPresent();

        List<VoucherPayment> byVoucher = paymentRepo.findAllByVoucherId(voucherId);
        assertThat(byVoucher).hasSize(1);
    }
}
