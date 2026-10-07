package com.andeva.atelier.platform.invoicing.infrastructure.external.reporting.openpdf;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CashFlowPdfGeneratorPort;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Secondary adapter implementing {@link CashFlowPdfGeneratorPort} using OpenPDF.
 * Renders an official corporate Cash Flow Statement in vector PDF format.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class OpenPdfCashFlowReportAdapter implements CashFlowPdfGeneratorPort {

    private static final Logger log = LoggerFactory.getLogger(OpenPdfCashFlowReportAdapter.class);

    @Override
    public byte[] generateCashFlowPdf(UUID tenantId, CashFlowSummary summary, List<CashFlowMovement> movements) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(summary, "CashFlowSummary cannot be null");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(44, 62, 80));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(127, 140, 141));
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(41, 128, 185));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(39, 174, 96));

            // Header
            Paragraph title = new Paragraph("ATELIER AUTOMOTIVE PLATFORM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("OFFICIAL OPERATIONAL CASH FLOW STATEMENT", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Summary Table
            Paragraph summaryTitle = new Paragraph("EXECUTIVE FINANCIAL SUMMARY", sectionFont);
            summaryTitle.setSpacingAfter(8);
            document.add(summaryTitle);

            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);

            addHeaderCell(summaryTable, "Gross Revenue", headerFont, new Color(44, 62, 80));
            addHeaderCell(summaryTable, "Purchase Expenses", headerFont, new Color(44, 62, 80));
            addHeaderCell(summaryTable, "Payroll Expenses", headerFont, new Color(44, 62, 80));
            addHeaderCell(summaryTable, "Net Cash Flow", headerFont, new Color(44, 62, 80));

            addCell(summaryTable, summary.grossRevenue().currency().name() + " " + summary.grossRevenue().amount(), bodyFont);
            addCell(summaryTable, summary.purchaseExpenses().currency().name() + " " + summary.purchaseExpenses().amount(), bodyFont);
            addCell(summaryTable, summary.payrollExpenses().currency().name() + " " + summary.payrollExpenses().amount(), bodyFont);
            addCell(summaryTable, summary.netCashFlow().currency().name() + " " + summary.netCashFlow().amount(), summaryFont);

            document.add(summaryTable);

            // Itemized Movements Table
            Paragraph movementsTitle = new Paragraph("CHRONOLOGICAL CASH MOVEMENTS", sectionFont);
            movementsTitle.setSpacingAfter(8);
            document.add(movementsTitle);

            PdfPTable moveTable = new PdfPTable(5);
            moveTable.setWidthPercentage(100);
            moveTable.setWidths(new float[]{2f, 2f, 4f, 2f, 2f});
            moveTable.setSpacingAfter(15);

            addHeaderCell(moveTable, "Date", headerFont, new Color(52, 73, 94));
            addHeaderCell(moveTable, "Type", headerFont, new Color(52, 73, 94));
            addHeaderCell(moveTable, "Concept", headerFont, new Color(52, 73, 94));
            addHeaderCell(moveTable, "Amount", headerFont, new Color(52, 73, 94));
            addHeaderCell(moveTable, "Balance", headerFont, new Color(52, 73, 94));

            if (movements != null && !movements.isEmpty()) {
                for (CashFlowMovement m : movements) {
                    addCell(moveTable, m.movementDate().toString(), bodyFont);
                    addCell(moveTable, m.type(), bodyFont);
                    addCell(moveTable, m.concept(), bodyFont);
                    addCell(moveTable, m.amount().toString(), bodyFont);
                    addCell(moveTable, m.runningBalance().toString(), bodyFont);
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Paragraph("No transactions recorded in the reporting period", bodyFont));
                emptyCell.setColspan(5);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(10);
                moveTable.addCell(emptyCell);
            }

            document.add(moveTable);

            // Footer
            Paragraph footer = new Paragraph("Report generated on " + LocalDate.now() + " | Tenant: " + tenantId, subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            log.info("Rendered Cash Flow Statement PDF for tenant: {} ({} bytes)", tenantId, out.size());
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to render Cash Flow Statement PDF for tenant: {}", tenantId, e);
            throw new IllegalStateException("Failed to generate Cash Flow PDF report", e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
