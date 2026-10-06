package com.andeva.atelier.platform.hr.interfaces.rest.transform;

import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.commands.AddPayrollBonusCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.AddPayrollDeductionCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.DisbursePayrollPaymentCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.GeneratePayrollCommand;
import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.AddPayrollBonusRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.AddPayrollDeductionRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.DisbursePayrollRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.GeneratePayrollRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.PayrollItemResource;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.PayrollPaymentResource;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.PayrollPaymentSummaryResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;

public final class PayrollPaymentResourceAssembler {

    private PayrollPaymentResourceAssembler() {}

    public static GeneratePayrollCommand toCommand(TenantId tenantId, GeneratePayrollRequest request) {
        return new GeneratePayrollCommand(
                tenantId,
                TenantMembershipId.of(request.membershipId()),
                request.periodStart(),
                request.periodEnd()
        );
    }

    public static AddPayrollDeductionCommand toCommand(TenantId tenantId, PayrollPaymentId payrollId, AddPayrollDeductionRequest request) {
        Currency cur = request.currency() != null ? Currency.valueOf(request.currency().toUpperCase()) : Currency.PEN;
        return new AddPayrollDeductionCommand(
                tenantId,
                payrollId,
                request.concept(),
                Money.of(request.amount(), cur),
                request.deductionType(),
                request.date()
        );
    }

    public static AddPayrollBonusCommand toCommand(TenantId tenantId, PayrollPaymentId payrollId, AddPayrollBonusRequest request) {
        Currency cur = request.currency() != null ? Currency.valueOf(request.currency().toUpperCase()) : Currency.PEN;
        return new AddPayrollBonusCommand(
                tenantId,
                payrollId,
                request.concept(),
                Money.of(request.amount(), cur),
                request.bonusType(),
                request.date()
        );
    }

    public static DisbursePayrollPaymentCommand toCommand(TenantId tenantId, PayrollPaymentId payrollId, DisbursePayrollRequest request) {
        return new DisbursePayrollPaymentCommand(
                tenantId,
                payrollId,
                request.paymentReference(),
                request.paidAt()
        );
    }

    public static PayrollPaymentResource toResource(PayrollPayment domain) {
        if (domain == null) return null;

        List<PayrollItemResource> items = domain.getItems().stream()
                .map(i -> new PayrollItemResource(
                        i.getCategory().name(),
                        i.getConcept(),
                        i.getAmount().amount(),
                        i.getType(),
                        i.getDate()
                )).toList();

        return new PayrollPaymentResource(
                domain.getId().value(),
                domain.getMembershipId().value(),
                domain.getPeriod().startDate(),
                domain.getPeriod().endDate(),
                domain.getBaseAmount().amount(),
                domain.getDeductions().amount(),
                domain.getBonuses().amount(),
                domain.getTotalPaid().amount(),
                domain.getCurrency().name(),
                domain.getStatus().name(),
                domain.getPaidAt(),
                domain.getPaymentReference(),
                items
        );
    }

    public static PayrollPaymentSummaryResource toSummaryResource(PayrollPayment domain) {
        if (domain == null) return null;
        return new PayrollPaymentSummaryResource(
                domain.getId().value(),
                domain.getMembershipId().value(),
                domain.getPeriod().startDate(),
                domain.getPeriod().endDate(),
                domain.getBaseAmount().amount(),
                domain.getTotalPaid().amount(),
                domain.getCurrency().name(),
                domain.getStatus().name()
        );
    }

    public static List<PayrollPaymentResource> toResourceList(List<PayrollPayment> list) {
        if (list == null) return List.of();
        return list.stream().map(PayrollPaymentResourceAssembler::toResource).toList();
    }

    public static List<PayrollPaymentSummaryResource> toSummaryResourceList(List<PayrollPayment> list) {
        if (list == null) return List.of();
        return list.stream().map(PayrollPaymentResourceAssembler::toSummaryResource).toList();
    }
}
