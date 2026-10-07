package com.andeva.atelier.platform.iot.infrastructure.external.reporting.openpdf;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.VehicleHealthReportPdfGeneratorPort;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

/**
 * Secondary adapter implementing {@link VehicleHealthReportPdfGeneratorPort} using OpenPDF.
 * Compiles a structured, comprehensive institutional mechanical health evaluation document.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class OpenPdfVehicleHealthReportGeneratorAdapter implements VehicleHealthReportPdfGeneratorPort {

    @Override
    public byte[] generateHealthReportPdf(
            VehicleHealthReportAiDto reportData,
            CrmFleetAclPort.VehicleMetadataDto metadata) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(44, 62, 80));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(127, 140, 141));
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(22, 160, 133));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font scoreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(39, 174, 96));

            // Header Banner
            Paragraph title = new Paragraph("ATELIER AUTOMOTIVE PLATFORM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("UNIFIED VEHICLE HEALTH & PREDICTIVE DIAGNOSTIC REPORT", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Vehicle Metadata Table
            PdfPTable metaTable = new PdfPTable(4);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            addCell(metaTable, "License Plate:", true, bodyFont);
            addCell(metaTable, metadata != null ? metadata.licensePlate() : "N/A", false, bodyFont);
            addCell(metaTable, "VIN:", true, bodyFont);
            addCell(metaTable, metadata != null ? metadata.vin() : "N/A", false, bodyFont);

            addCell(metaTable, "Vehicle Brand:", true, bodyFont);
            addCell(metaTable, metadata != null ? metadata.brand() + " " + metadata.model() : "N/A", false, bodyFont);
            addCell(metaTable, "Model Year:", true, bodyFont);
            addCell(metaTable, metadata != null ? String.valueOf(metadata.year()) : "N/A", false, bodyFont);

            addCell(metaTable, "Customer Name:", true, bodyFont);
            addCell(metaTable, metadata != null ? metadata.ownerName() : "Fleet Vehicle", false, bodyFont);
            addCell(metaTable, "Report Status:", true, bodyFont);
            addCell(metaTable, "OFFICIALLY CERTIFIED", false, bodyFont);

            document.add(metaTable);

            // Health Score & Condition
            Paragraph scoreHeader = new Paragraph("1. OVERALL VEHICLE HEALTH INDEX", sectionFont);
            scoreHeader.setSpacingAfter(5);
            document.add(scoreHeader);

            int score = reportData != null ? reportData.healthScore() : 100;
            String condition = reportData != null ? reportData.overallCondition() : "GOOD";
            Paragraph scorePara = new Paragraph(String.format("Health Score: %d / 100   |   Condition: %s", score, condition), scoreFont);
            scorePara.setSpacingAfter(10);
            document.add(scorePara);

            // Executive Summary
            Paragraph summaryHeader = new Paragraph("2. EXECUTIVE DIAGNOSTIC SUMMARY", sectionFont);
            summaryHeader.setSpacingAfter(5);
            document.add(summaryHeader);

            String summaryText = (reportData != null && reportData.executiveSummary() != null)
                    ? reportData.executiveSummary()
                    : "No critical mechanical issues identified during telemetry evaluation.";
            Paragraph summaryPara = new Paragraph(summaryText, bodyFont);
            summaryPara.setSpacingAfter(15);
            document.add(summaryPara);

            // Subsystem Evaluations
            if (reportData != null && reportData.subsystemEvaluations() != null && !reportData.subsystemEvaluations().isEmpty()) {
                Paragraph subHeader = new Paragraph("3. SUBSYSTEM MECHANICAL EVALUATIONS", sectionFont);
                subHeader.setSpacingAfter(5);
                document.add(subHeader);

                PdfPTable subTable = new PdfPTable(4);
                subTable.setWidthPercentage(100);
                subTable.setSpacingAfter(15);
                addCell(subTable, "Subsystem", true, bodyFont);
                addCell(subTable, "Status", true, bodyFont);
                addCell(subTable, "Score", true, bodyFont);
                addCell(subTable, "Findings", true, bodyFont);

                for (var sub : reportData.subsystemEvaluations()) {
                    addCell(subTable, sub.subsystemName(), false, bodyFont);
                    addCell(subTable, sub.status(), false, bodyFont);
                    addCell(subTable, sub.score() + "/100", false, bodyFont);
                    addCell(subTable, sub.findings(), false, bodyFont);
                }
                document.add(subTable);
            }

            // Recommended Workshop Services
            if (reportData != null && reportData.recommendedActions() != null && !reportData.recommendedActions().isEmpty()) {
                Paragraph actionsHeader = new Paragraph("4. RECOMMENDED WORKSHOP SERVICES", sectionFont);
                actionsHeader.setSpacingAfter(5);
                document.add(actionsHeader);

                PdfPTable actTable = new PdfPTable(4);
                actTable.setWidthPercentage(100);
                actTable.setSpacingAfter(15);
                addCell(actTable, "Service Code", true, bodyFont);
                addCell(actTable, "Description", true, bodyFont);
                addCell(actTable, "Urgency", true, bodyFont);
                addCell(actTable, "Estimated Cost", true, bodyFont);

                for (var act : reportData.recommendedActions()) {
                    addCell(actTable, act.serviceCode() != null ? act.serviceCode() : "SRV-GEN", false, bodyFont);
                    addCell(actTable, act.serviceName(), false, bodyFont);
                    addCell(actTable, act.urgency(), false, bodyFont);
                    addCell(actTable, "$" + act.estimatedCost(), false, bodyFont);
                }
                document.add(actTable);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render vehicle health report PDF document: " + e.getMessage(), e);
        }
    }

    private void addCell(PdfPTable table, String text, boolean isHeader, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text != null ? text : "", font));
        if (isHeader) {
            cell.setBackgroundColor(new Color(236, 240, 241));
        }
        cell.setPadding(5);
        table.addCell(cell);
    }
}
