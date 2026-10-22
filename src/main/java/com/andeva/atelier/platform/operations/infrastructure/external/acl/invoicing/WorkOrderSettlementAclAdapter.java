package com.andeva.atelier.platform.operations.infrastructure.external.acl.invoicing;

import com.andeva.atelier.platform.invoicing.interfaces.acl.InvoicingContextFacade;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderSettlementAclService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Anti-Corruption Layer adapter asking Invoicing whether a work order's vouchers are paid.
 * The facade is injected lazily: Invoicing itself reads work orders when it bills them.
 */
@Component
public class WorkOrderSettlementAclAdapter implements WorkOrderSettlementAclService {

    private final InvoicingContextFacade invoicingContextFacade;

    public WorkOrderSettlementAclAdapter(@Lazy InvoicingContextFacade invoicingContextFacade) {
        this.invoicingContextFacade = Objects.requireNonNull(invoicingContextFacade, "invoicingContextFacade cannot be null");
    }

    @Override
    public boolean isSettled(UUID workOrderId) {
        return workOrderId != null && invoicingContextFacade.isWorkOrderFullySettled(workOrderId);
    }
}
