package com.andeva.atelier.platform.invoicing.application;

import com.andeva.atelier.platform.invoicing.application.commandservices.VoucherPaymentCommandService;
import com.andeva.atelier.platform.invoicing.application.internal.commandservices.VoucherPaymentCommandServiceImpl;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherAlreadyPaidException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.commands.RegisterVoucherPaymentCommand;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.invoicing.domain.repositories.VoucherPaymentRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for VoucherPaymentCommandServiceImpl.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("VoucherPaymentCommandServiceImpl Tests")
class VoucherPaymentCommandServiceTest {

    private ElectronicVoucherRepository voucherRepository;
    private VoucherPaymentRepository paymentRepository;
    private VoucherPaymentCommandService paymentCommandService;

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

        paymentCommandService = new VoucherPaymentCommandServiceImpl(voucherRepository, paymentRepository);

        voucher = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                workOrderId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                CustomerFiscalInfo.of(TaxId.ruc("20100070970"), "CLIENTE SAC", "LIMA", DocumentType.RUC),
                Currency.PEN,
                TaxCalculation.of(Money.of(new BigDecimal("100.00"), Currency.PEN), Money.of(new BigDecimal("18.00"), Currency.PEN), Money.of(new BigDecimal("118.00"), Currency.PEN)),
                List.of(taxEngine.calculateLine(null, voucherId, Optional.empty(), VoucherItemType.SERVICE, "Servicio", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );

        when(voucherRepository.findById(voucher.getId())).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(ElectronicVoucher.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentRepository.save(any(VoucherPayment.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("Should successfully register partial and final payments on voucher")
    void shouldRegisterPayments() {
        // 1. Partial payment S/ 50.00
        RegisterVoucherPaymentCommand partialCmd = new RegisterVoucherPaymentCommand(
                voucher.getId(),
                Money.of(new BigDecimal("50.00"), Currency.PEN),
                PaymentMethod.CASH,
                null
        );

        VoucherPayment payment1 = paymentCommandService.handle(partialCmd);
        assertThat(payment1).isNotNull();
        assertThat(payment1.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment1.getAmount().amount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(voucher.isFullyPaid()).isFalse();
        assertThat(voucher.getPendingBalance().amount()).isEqualByComparingTo(new BigDecimal("68.00"));

        // 2. Final payment S/ 68.00
        RegisterVoucherPaymentCommand finalCmd = new RegisterVoucherPaymentCommand(
                voucher.getId(),
                Money.of(new BigDecimal("68.00"), Currency.PEN),
                PaymentMethod.DIGITAL_WALLET_YAPE,
                "OPER-123456"
        );

        VoucherPayment payment2 = paymentCommandService.handle(finalCmd);
        assertThat(payment2).isNotNull();
        assertThat(voucher.isFullyPaid()).isTrue();
        assertThat(voucher.getPendingBalance().amount()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(paymentRepository, Mockito.times(2)).save(any(VoucherPayment.class));
    }

    @Test
    @DisplayName("Should reject overpayment when amount exceeds pending balance")
    void shouldRejectOverpayment() {
        RegisterVoucherPaymentCommand overCmd = new RegisterVoucherPaymentCommand(
                voucher.getId(),
                Money.of(new BigDecimal("120.00"), Currency.PEN), // Total is 118.00!
                PaymentMethod.CASH,
                null
        );

        assertThatThrownBy(() -> paymentCommandService.handle(overCmd))
                .isInstanceOf(VoucherAlreadyPaidException.class);
    }

    @Test
    @DisplayName("Should throw VoucherNotFoundException when voucher does not exist")
    void shouldThrowWhenVoucherNotFound() {
        VoucherId missingId = VoucherId.generate();
        when(voucherRepository.findById(missingId)).thenReturn(Optional.empty());

        RegisterVoucherPaymentCommand cmd = new RegisterVoucherPaymentCommand(
                missingId,
                Money.of(new BigDecimal("50.00"), Currency.PEN),
                PaymentMethod.CASH,
                null
        );

        assertThatThrownBy(() -> paymentCommandService.handle(cmd))
                .isInstanceOf(VoucherNotFoundException.class);
    }
}
