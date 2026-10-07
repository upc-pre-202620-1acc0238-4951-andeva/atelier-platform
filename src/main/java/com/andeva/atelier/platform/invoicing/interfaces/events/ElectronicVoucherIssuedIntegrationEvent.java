package com.andeva.atelier.platform.invoicing.interfaces.events;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when an electronic voucher (Factura or Boleta)
 * is issued and registered in the invoicing catalog.
 *
 * @author Joel Huamani Estefanero
 */
public record ElectronicVoucherIssuedIntegrationEvent(
        UUID voucherId,
        UUID tenantId,
        UUID branchId,
        UUID customerId,
        UUID workOrderId,
        String voucherType,
        String serie,
        int number,
        BigDecimal totalAmount,
        String currency,
        Instant occurredOn
) implements Serializable {}
