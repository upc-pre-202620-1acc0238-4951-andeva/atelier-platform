package com.andeva.atelier.platform.invoicing.domain;

import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidVoucherAmountException;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link VoucherPayment} child entity.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("VoucherPayment Entity Unit Tests")
class VoucherPaymentTest {

    private final VoucherId voucherId = VoucherId.generate();
    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final BranchId branchId = BranchId.of(UUID.randomUUID());

    @Test
    @DisplayName("Should record payment in COMPLETED status")
    void shouldRecordPaymentSuccessfully() {
        Money amount = Money.soles(150.00);

        VoucherPayment payment = VoucherPayment.record(
                null,
                voucherId,
                tenantId,
                branchId,
                amount,
                PaymentMethod.CASH,
                null
        );

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getVoucherId()).isEqualTo(voucherId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getPaidAt()).isNotNull();

        payment.refund();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("Should require transaction reference for electronic and wallet payments")
    void shouldRequireTransactionReferenceForElectronicPayments() {
        Money amount = Money.soles(80.00);

        assertThatThrownBy(() -> VoucherPayment.record(
                null, voucherId, tenantId, branchId, amount, PaymentMethod.DIGITAL_WALLET_YAPE, null
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> VoucherPayment.record(
                null, voucherId, tenantId, branchId, amount, PaymentMethod.BANK_TRANSFER, "   "
        )).isInstanceOf(IllegalArgumentException.class);

        VoucherPayment validWallet = VoucherPayment.record(
                null, voucherId, tenantId, branchId, amount, PaymentMethod.DIGITAL_WALLET_YAPE, "OPER-998822"
        );
        assertThat(validWallet.getTransactionReference()).isEqualTo("OPER-998822");
    }

    @Test
    @DisplayName("Should reject payment with zero or negative amount")
    void shouldRejectZeroOrNegativePaymentAmount() {
        assertThatThrownBy(() -> VoucherPayment.record(
                null, voucherId, tenantId, branchId, Money.soles(0.00), PaymentMethod.CASH, null
        )).isInstanceOf(InvalidVoucherAmountException.class);

        assertThatThrownBy(() -> VoucherPayment.record(
                null, voucherId, tenantId, branchId, Money.soles(-20.00), PaymentMethod.CASH, null
        )).isInstanceOf(InvalidVoucherAmountException.class);
    }
}
