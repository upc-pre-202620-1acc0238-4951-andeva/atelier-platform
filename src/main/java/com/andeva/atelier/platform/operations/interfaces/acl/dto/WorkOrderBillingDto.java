package com.andeva.atelier.platform.operations.interfaces.acl.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WorkOrderBillingDto(
        UUID id,
        UUID tenantId,
        Integer internalNumber,
        UUID customerId,
        UUID vehicleId,
        BigDecimal laborSubtotal,
        BigDecimal productsSubtotal,
        BigDecimal totalAmount,
        String currency
) {
    public BigDecimal laborAmount() {
        return laborSubtotal;
    }

    public BigDecimal productsAmount() {
        return productsSubtotal;
    }
}
