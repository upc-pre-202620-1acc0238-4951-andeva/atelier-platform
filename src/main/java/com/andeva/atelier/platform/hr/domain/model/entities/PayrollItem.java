package com.andeva.atelier.platform.hr.domain.model.entities;

import com.andeva.atelier.platform.hr.domain.model.enums.PayrollItemCategory;
import com.andeva.atelier.platform.hr.domain.model.ids.PayrollItemId;
import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.time.LocalDate;
import java.util.Objects;

public class PayrollItem {

    private final PayrollItemId id;
    private final PayrollPaymentId payrollPaymentId;
    private final PayrollItemCategory category;
    private final String concept;
    private final Money amount;
    private final String type;
    private final LocalDate date;

    public PayrollItem(
            PayrollItemId id,
            PayrollPaymentId payrollPaymentId,
            PayrollItemCategory category,
            String concept,
            Money amount,
            String type,
            LocalDate date
    ) {
        this.id = Objects.requireNonNull(id, "PayrollItemId cannot be null");
        this.payrollPaymentId = Objects.requireNonNull(payrollPaymentId, "PayrollPaymentId cannot be null");
        this.category = Objects.requireNonNull(category, "category cannot be null");
        this.concept = Objects.requireNonNull(concept, "concept cannot be null");
        this.amount = Objects.requireNonNull(amount, "amount cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.date = Objects.requireNonNull(date, "date cannot be null");
    }

    public static PayrollItem create(
            PayrollPaymentId payrollPaymentId,
            PayrollItemCategory category,
            String concept,
            Money amount,
            String type,
            LocalDate date
    ) {
        return new PayrollItem(PayrollItemId.generate(), payrollPaymentId, category, concept, amount, type, date);
    }

    public PayrollItemId getId() {
        return id;
    }

    public PayrollPaymentId getPayrollPaymentId() {
        return payrollPaymentId;
    }

    public PayrollItemCategory getCategory() {
        return category;
    }

    public String getConcept() {
        return concept;
    }

    public Money getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }
}
