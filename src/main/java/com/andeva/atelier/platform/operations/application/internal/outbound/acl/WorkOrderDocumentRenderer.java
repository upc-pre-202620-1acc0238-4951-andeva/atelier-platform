package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Outbound port rendering customer-facing documents of a work order as PDF.
 */
public interface WorkOrderDocumentRenderer {

    byte[] renderQuotation(QuotationDocument quotation);

    byte[] renderExitPass(ExitPassDocument exitPass);

    record Line(String kind, String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount) {
    }

    record QuotationDocument(
            WorkshopProfileDto workshop,
            int workOrderNumber,
            String customerName,
            String customerTaxId,
            String vehicle,
            String plate,
            int mileage,
            String diagnosticSummary,
            List<Line> lines,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal total,
            String currency,
            LocalDate issuedOn,
            LocalDate validUntil
    ) {
    }

    record ExitPassDocument(
            WorkshopProfileDto workshop,
            int workOrderNumber,
            String customerName,
            String vehicle,
            String plate,
            BigDecimal totalPaid,
            String currency,
            Instant issuedAt
    ) {
    }
}
