package com.andeva.atelier.platform.invoicing.infrastructure.external.reporting.openpdf;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CashFlowPdfGeneratorPort;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.BranchCashFlowBreakdown;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowReportContext;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Secondary adapter implementing {@link CashFlowPdfGeneratorPort} using OpenPDF.
 * Renders an official corporate Cash Flow & Fiscal Audit Statement in vector PDF format
 * with institutional letterhead, labor/parts/IGV breakdown, and optional multi-branch consolidation.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class OpenPdfCashFlowReportAdapter implements CashFlowPdfGeneratorPort {

    private static final Logger log = LoggerFactory.getLogger(OpenPdfCashFlowReportAdapter.class);
    private static final Color NAVY = new Color(3, 26, 107);
    private static final Color ELECTRIC_BLUE = new Color(0, 113, 235);
    private static final Color SLATE = new Color(52, 73, 94);
    private static final Color TOTAL_BG = new Color(235, 243, 252);

    @Override
    public byte[] generateCashFlowPdf(UUID tenantId, CashFlowSummary summary, List<CashFlowMovement> movements) {
        return generateCashFlowPdf(tenantId, CashFlowReportContext.defaultFor(LocalDate.now().minusMonths(1), LocalDate.now(), true), summary, movements);
    }

    @Override
    public byte[] generateCashFlowPdf(
            UUID tenantId,
            CashFlowReportContext context,
            CashFlowSummary summary,
            List<CashFlowMovement> movements
    ) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(summary, "CashFlowSummary cannot be null");
        CashFlowReportContext ctx = context != null
                ? context
                : CashFlowReportContext.defaultFor(LocalDate.now().minusMonths(1), LocalDate.now(), true);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, NAVY);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, ELECTRIC_BLUE);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);
            Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, NAVY);

            // 1. Institutional Letterhead
            Paragraph title = new Paragraph(ctx.workshopName().toUpperCase(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph legalLine = new Paragraph("Razón Social: " + ctx.legalName() + "   |   RUC: " + ctx.ruc(), metaFont);
            legalLine.setAlignment(Element.ALIGN_CENTER);
            document.add(legalLine);

            Paragraph subtitle = new Paragraph("ESTADO DE FLUJO DE CAJA E INGRESOS FISCALES", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(6);
            document.add(subtitle);

            Paragraph scopeMeta = new Paragraph(
                    ctx.scopeLabel() + "   |   Período: " + ctx.from() + " al " + ctx.to(),
                    metaFont
            );
            scopeMeta.setAlignment(Element.ALIGN_CENTER);
            scopeMeta.setSpacingAfter(14);
            document.add(scopeMeta);

            String currency = summary.grossRevenue().currency().name();

            // 2. Executive Financial & Fiscal Summary Table
            Paragraph summaryTitle = new Paragraph("RESUMEN EJECUTIVO Y DESGLOSE FISCAL", sectionFont);
            summaryTitle.setSpacingAfter(6);
            document.add(summaryTitle);

            PdfPTable summaryTable = new PdfPTable(5);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(16);

            addHeaderCell(summaryTable, "Ingresos Mano de Obra", headerFont, NAVY);
            addHeaderCell(summaryTable, "Ingresos Repuestos", headerFont, NAVY);
            addHeaderCell(summaryTable, "IGV Retenido (18%)", headerFont, NAVY);
            addHeaderCell(summaryTable, "Total Recaudado", headerFont, NAVY);
            addHeaderCell(summaryTable, "Flujo Neto", headerFont, NAVY);

            addCell(summaryTable, currency + " " + summary.laborRevenue().amount(), bodyFont);
            addCell(summaryTable, currency + " " + summary.partsRevenue().amount(), bodyFont);
            addCell(summaryTable, currency + " " + summary.igvCollected().amount(), bodyFont);
            addCell(summaryTable, currency + " " + summary.grossRevenue().amount(), boldBodyFont);
            addCell(summaryTable, currency + " " + summary.netCashFlow().amount(), boldBodyFont);

            document.add(summaryTable);

            // 3. Multi-Branch Corporate Breakdown Table (Scenario 2)
            if (ctx.multiBranch() && summary.branchBreakdowns() != null && !summary.branchBreakdowns().isEmpty()) {
                Paragraph branchTitle = new Paragraph("RECAUDACIÓN CONSOLIDADA POR SUCURSAL", sectionFont);
                branchTitle.setSpacingAfter(6);
                document.add(branchTitle);

                PdfPTable branchTable = new PdfPTable(5);
                branchTable.setWidthPercentage(100);
                branchTable.setWidths(new float[]{3f, 2f, 2f, 2f, 2f});
                branchTable.setSpacingAfter(16);

                addHeaderCell(branchTable, "Sucursal", headerFont, SLATE);
                addHeaderCell(branchTable, "Mano de Obra", headerFont, SLATE);
                addHeaderCell(branchTable, "Repuestos", headerFont, SLATE);
                addHeaderCell(branchTable, "IGV Retenido", headerFont, SLATE);
                addHeaderCell(branchTable, "Total Sede", headerFont, SLATE);

                BigDecimal sumLabor = BigDecimal.ZERO;
                BigDecimal sumParts = BigDecimal.ZERO;
                BigDecimal sumIgv = BigDecimal.ZERO;
                BigDecimal sumTotal = BigDecimal.ZERO;

                for (BranchCashFlowBreakdown b : summary.branchBreakdowns()) {
                    addCell(branchTable, b.branchName(), bodyFont);
                    addCell(branchTable, currency + " " + b.laborRevenue().amount(), bodyFont);
                    addCell(branchTable, currency + " " + b.partsRevenue().amount(), bodyFont);
                    addCell(branchTable, currency + " " + b.igvCollected().amount(), bodyFont);
                    addCell(branchTable, currency + " " + b.totalRevenue().amount(), boldBodyFont);

                    sumLabor = sumLabor.add(b.laborRevenue().amount());
                    sumParts = sumParts.add(b.partsRevenue().amount());
                    sumIgv = sumIgv.add(b.igvCollected().amount());
                    sumTotal = sumTotal.add(b.totalRevenue().amount());
                }

                addHighlightedCell(branchTable, "TOTAL CONSOLIDADO", boldBodyFont, TOTAL_BG);
                addHighlightedCell(branchTable, currency + " " + sumLabor, boldBodyFont, TOTAL_BG);
                addHighlightedCell(branchTable, currency + " " + sumParts, boldBodyFont, TOTAL_BG);
                addHighlightedCell(branchTable, currency + " " + sumIgv, boldBodyFont, TOTAL_BG);
                addHighlightedCell(branchTable, currency + " " + sumTotal, boldBodyFont, TOTAL_BG);

                document.add(branchTable);
            }

            // 4. Itemized Cash Movements Table
            Paragraph movementsTitle = new Paragraph("DETALLE CRONOLÓGICO DE COBROS Y MOVIMIENTOS", sectionFont);
            movementsTitle.setSpacingAfter(6);
            document.add(movementsTitle);

            PdfPTable moveTable = new PdfPTable(5);
            moveTable.setWidthPercentage(100);
            moveTable.setWidths(new float[]{2f, 1.6f, 4.4f, 2f, 2f});
            moveTable.setSpacingAfter(14);

            addHeaderCell(moveTable, "Fecha", headerFont, SLATE);
            addHeaderCell(moveTable, "Tipo", headerFont, SLATE);
            addHeaderCell(moveTable, "Concepto", headerFont, SLATE);
            addHeaderCell(moveTable, "Importe", headerFont, SLATE);
            addHeaderCell(moveTable, "Saldo", headerFont, SLATE);

            if (movements != null && !movements.isEmpty()) {
                for (CashFlowMovement m : movements) {
                    addCell(moveTable, m.movementDate().toString(), bodyFont);
                    addCell(moveTable, m.type(), bodyFont);
                    addCell(moveTable, m.concept(), bodyFont);
                    addCell(moveTable, currency + " " + m.amount(), bodyFont);
                    addCell(moveTable, currency + " " + m.runningBalance(), bodyFont);
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Paragraph("No se registraron movimientos en el período seleccionado.", bodyFont));
                emptyCell.setColspan(5);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(10);
                moveTable.addCell(emptyCell);
            }

            document.add(moveTable);

            // 5. Audit Footer
            Paragraph footer = new Paragraph(
                    "Documento emitido el " + LocalDate.now() + " por Atelier Platform para fines de auditoría contable externa | Tenant: " + tenantId,
                    metaFont
            );
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

    private void addHighlightedCell(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }
}
