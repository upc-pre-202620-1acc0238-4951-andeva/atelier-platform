package com.andeva.atelier.platform.hr.domain.model.aggregates;

import com.andeva.atelier.platform.hr.domain.exceptions.InvalidPayrollModificationException;
import com.andeva.atelier.platform.hr.domain.model.entities.PayrollItem;
import com.andeva.atelier.platform.hr.domain.model.enums.PayrollItemCategory;
import com.andeva.atelier.platform.hr.domain.model.enums.PayrollStatus;
import com.andeva.atelier.platform.hr.domain.model.events.PayrollApprovedEvent;
import com.andeva.atelier.platform.hr.domain.model.events.PayrollCalculatedEvent;
import com.andeva.atelier.platform.hr.domain.model.events.PayrollDisbursedEvent;
import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.PayPeriod;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PayrollPayment extends AbstractDomainAggregateRoot<PayrollPayment> {

    private final PayrollPaymentId id;
    private final TenantId tenantId;
    private final TenantMembershipId membershipId;
    private final PayPeriod period;
    private Money baseAmount;
    private Money deductions;
    private Money bonuses;
    private Money totalPaid;
    private PayrollStatus status;
    private Instant paidAt;
    private String paymentReference;
    private final List<PayrollItem> items;

    public PayrollPayment(
            PayrollPaymentId id,
            TenantId tenantId,
            TenantMembershipId membershipId,
            PayPeriod period,
            Money baseAmount,
            Money deductions,
            Money bonuses,
            Money totalPaid,
            PayrollStatus status,
            Instant paidAt,
            String paymentReference,
            List<PayrollItem> items
    ) {
        this.id = Objects.requireNonNull(id, "PayrollPaymentId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.membershipId = Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
        this.period = Objects.requireNonNull(period, "PayPeriod cannot be null");
        this.baseAmount = Objects.requireNonNull(baseAmount, "baseAmount cannot be null");
        this.deductions = deductions != null ? deductions : Money.of(BigDecimal.ZERO, baseAmount.currency());
        this.bonuses = bonuses != null ? bonuses : Money.of(BigDecimal.ZERO, baseAmount.currency());
        this.totalPaid = totalPaid != null ? totalPaid : calculateNetTotal(baseAmount, this.deductions, this.bonuses);
        this.status = status != null ? status : PayrollStatus.DRAFT;
        this.paidAt = paidAt;
        this.paymentReference = paymentReference;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public static PayrollPayment calculate(
            TenantId tenantId,
            TenantMembershipId membershipId,
            PayPeriod period,
            Money baseSalary
    ) {
        PayrollPaymentId paymentId = PayrollPaymentId.generate();
        Money zero = Money.of(BigDecimal.ZERO, baseSalary.currency());

        PayrollPayment payment = new PayrollPayment(
                paymentId, tenantId, membershipId, period,
                baseSalary, zero, zero, baseSalary,
                PayrollStatus.DRAFT, null, null, new ArrayList<>()
        );

        payment.addItem(PayrollItem.create(
                paymentId, PayrollItemCategory.BASE_SALARY, "Haber Básico Contractual",
                baseSalary, "BASE", period.startDate()
        ));

        payment.registerEvent(PayrollCalculatedEvent.now(paymentId, tenantId, membershipId, payment.totalPaid));
        return payment;
    }

    public void addDeduction(String concept, Money amount, String type, LocalDate date) {
        assertModifiable();
        Objects.requireNonNull(amount, "Deduction amount cannot be null");
        this.items.add(PayrollItem.create(this.id, PayrollItemCategory.DEDUCTION, concept, amount, type, date));
        this.deductions = this.deductions.add(amount);
        recalculateNet();
    }

    public void addBonus(String concept, Money amount, String type, LocalDate date) {
        assertModifiable();
        Objects.requireNonNull(amount, "Bonus amount cannot be null");
        this.items.add(PayrollItem.create(this.id, PayrollItemCategory.BONUS, concept, amount, type, date));
        this.bonuses = this.bonuses.add(amount);
        recalculateNet();
    }

    public void approve(TenantMembershipId approverMembershipId) {
        assertModifiable();
        this.status = PayrollStatus.APPROVED;
        registerEvent(PayrollApprovedEvent.now(this.id, this.tenantId, this.membershipId, approverMembershipId, this.totalPaid));
    }

    public void disburse(String paymentReference, Instant paidAt) {
        if (this.status != PayrollStatus.APPROVED) {
            throw new InvalidPayrollModificationException("Únicamente las liquidaciones aprobadas pueden ser desembolsadas");
        }
        this.paymentReference = Objects.requireNonNull(paymentReference, "paymentReference cannot be null");
        this.paidAt = paidAt != null ? paidAt : Instant.now();
        this.status = PayrollStatus.PAID;

        registerEvent(PayrollDisbursedEvent.now(
                this.id, this.tenantId, this.membershipId, this.paymentReference, this.totalPaid, this.paidAt
        ));
    }

    public void cancel() {
        if (this.status == PayrollStatus.PAID) {
            throw new InvalidPayrollModificationException("No se puede cancelar una liquidación de nómina ya pagada");
        }
        this.status = PayrollStatus.CANCELLED;
    }

    private void recalculateNet() {
        this.totalPaid = calculateNetTotal(this.baseAmount, this.deductions, this.bonuses);
    }

    private static Money calculateNetTotal(Money base, Money ded, Money bon) {
        BigDecimal net = base.amount().subtract(ded.amount()).add(bon.amount());
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            net = BigDecimal.ZERO;
        }
        return Money.of(net, base.currency());
    }

    private void assertModifiable() {
        if (this.status == PayrollStatus.APPROVED || this.status == PayrollStatus.PAID) {
            throw new InvalidPayrollModificationException("La liquidación de nómina se encuentra en estado " + this.status + " y no admite modificaciones");
        }
    }

    public void addItem(PayrollItem item) {
        if (item != null) {
            this.items.add(item);
        }
    }

    public PayrollPaymentId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public TenantMembershipId getMembershipId() {
        return membershipId;
    }

    public PayPeriod getPeriod() {
        return period;
    }

    public Money getBaseAmount() {
        return baseAmount;
    }

    public Money getDeductions() {
        return deductions;
    }

    public Money getBonuses() {
        return bonuses;
    }

    public Money getTotalPaid() {
        return totalPaid;
    }

    public Currency getCurrency() {
        return baseAmount.currency();
    }

    public PayrollStatus getStatus() {
        return status;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public List<PayrollItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
