package com.andeva.atelier.platform.operations.domain.services;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskProduct;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class WorkOrderCostCalculator {

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    public record WorkOrderBillingSummary(
            Money subtotal,
            Money tax,
            Money totalAmount
    ) {
    }

    public WorkOrderBillingSummary calculateTotal(WorkOrder workOrder) {
        if (workOrder == null) {
            Money zero = Money.ZERO_PEN;
            return new WorkOrderBillingSummary(zero, zero, zero);
        }

        Currency currency = workOrder.getTotalAmount() != null ? workOrder.getTotalAmount().currency() : Currency.PEN;
        BigDecimal subtotalLabor = BigDecimal.ZERO;
        BigDecimal subtotalParts = BigDecimal.ZERO;

        for (WorkOrderTask task : workOrder.getTasks()) {
            if (task.getStatus() != WorkOrderTaskStatus.CANCELLED) {
                if (task.getPrice() != null && task.getPrice().amount() != null) {
                    subtotalLabor = subtotalLabor.add(task.getPrice().amount());
                }

                for (WorkOrderTaskProduct product : task.getConsumedProducts()) {
                    if (product.getTotalAmount() != null && product.getTotalAmount().amount() != null) {
                        subtotalParts = subtotalParts.add(product.getTotalAmount().amount());
                    }
                }
            }
        }

        BigDecimal subtotalAmount = subtotalLabor.add(subtotalParts).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal taxAmount = subtotalAmount.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal totalAmount = subtotalAmount.add(taxAmount).setScale(2, RoundingMode.HALF_EVEN);

        return new WorkOrderBillingSummary(
                Money.of(subtotalAmount, currency),
                Money.of(taxAmount, currency),
                Money.of(totalAmount, currency)
        );
    }
}
