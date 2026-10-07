package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Presentation resource detailing a legally issued electronic voucher,
 * its lines, payment amortizations, and official SUNAT dispatch metadata.
 *
 * @author Joel Huamani Estefanero
 */
public record ElectronicVoucherResource(
        UUID id,
        UUID tenantId,
        UUID branchId,
        UUID customerId,
        UUID workOrderId,
        String voucherType,
        String serie,
        int number,
        BigDecimal subtotal,
        BigDecimal igvAmount,
        BigDecimal totalAmount,
        String currency,
        String status,
        CustomerFiscalInfoResponse customerInfo,
        DigitalReceiptUrlsResponse digitalReceipts,
        SunatResponseDto sunatResponse,
        List<VoucherLineResource> lines,
        List<VoucherPaymentResource> payments,
        Instant issuedAt
) implements Serializable {
}
