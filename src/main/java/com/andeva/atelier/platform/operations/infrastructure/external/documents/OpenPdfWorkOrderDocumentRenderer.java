package com.andeva.atelier.platform.operations.infrastructure.external.documents;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderDocumentRenderer;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * OpenPDF rendering of the quotation (proforma) and the vehicle exit pass.
 */
@Component
public class OpenPdfWorkOrderDocumentRenderer implements WorkOrderDocumentRenderer {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Lima"));

    private final Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(44, 62, 80));
    private final Font section = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(22, 160, 133));
    private final Font body = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
    private final Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);

    @Override
    public byte[] renderQuotation(QuotationDocument q) {
        return render(document -> {
            header(document, q.workshop().name(), q.workshop().legalName(), q.workshop().taxId());
            Paragraph heading = new Paragraph("PROFORMA DE MANTENIMIENTO N.° " + q.workOrderNumber(), title);
            heading.setAlignment(Element.ALIGN_CENTER);
            heading.setSpacingAfter(10);
            document.add(heading);

            PdfPTable meta = new PdfPTable(4);
            meta.setWidthPercentage(100);
            meta.setSpacingAfter(12);
            pair(meta, "Cliente", q.customerName());
            pair(meta, "Documento", q.customerTaxId());
            pair(meta, "Vehículo", q.vehicle());
            pair(meta, "Placa", q.plate());
            pair(meta, "Kilometraje", q.mileage() + " km");
            pair(meta, "Emisión", DATE.format(q.issuedOn()));
            pair(meta, "Diagnóstico", q.diagnosticSummary());
            pair(meta, "Válida hasta", DATE.format(q.validUntil()));
            document.add(meta);

            document.add(new Paragraph("Detalle de labores y repuestos", section));
            PdfPTable lines = new PdfPTable(new float[]{2.2f, 6f, 1.3f, 2f, 2f});
            lines.setWidthPercentage(100);
            lines.setSpacingBefore(4);
            for (String column : new String[]{"Tipo", "Descripción", "Cant.", "P. unit.", "Importe"}) {
                PdfPCell cell = new PdfPCell(new Phrase(column, bold));
                cell.setBackgroundColor(new Color(236, 240, 241));
                lines.addCell(cell);
            }
            for (Line line : q.lines()) {
                lines.addCell(new Phrase(line.kind(), body));
                lines.addCell(new Phrase(line.description(), body));
                lines.addCell(right(plain(line.quantity())));
                lines.addCell(right(money(line.unitPrice())));
                lines.addCell(right(money(line.amount())));
            }
            document.add(lines);

            PdfPTable totals = new PdfPTable(new float[]{9.5f, 2f, 2f});
            totals.setWidthPercentage(100);
            totals.setSpacingBefore(6);
            total(totals, "Subtotal", q.currency(), q.subtotal());
            total(totals, "IGV (18%)", q.currency(), q.tax());
            total(totals, "Total", q.currency(), q.total());
            document.add(totals);

            Paragraph note = new Paragraph("Los precios incluyen mano de obra y repuestos de catálogo. Esta proforma no es un comprobante de pago.", body);
            note.setSpacingBefore(12);
            document.add(note);
        });
    }

    @Override
    public byte[] renderExitPass(ExitPassDocument pass) {
        return render(document -> {
            header(document, pass.workshop().name(), pass.workshop().legalName(), pass.workshop().taxId());
            Paragraph heading = new Paragraph("PASE DE SALIDA VEHICULAR — ORDEN N.° " + pass.workOrderNumber(), title);
            heading.setAlignment(Element.ALIGN_CENTER);
            heading.setSpacingAfter(12);
            document.add(heading);

            PdfPTable meta = new PdfPTable(4);
            meta.setWidthPercentage(100);
            pair(meta, "Cliente", pass.customerName());
            pair(meta, "Placa", pass.plate());
            pair(meta, "Vehículo", pass.vehicle());
            pair(meta, "Total pagado", pass.currency() + " " + money(pass.totalPaid()));
            pair(meta, "Emitido", DATE_TIME.format(pass.issuedAt()));
            pair(meta, "Estado", "ORDEN PAGADA — SALIDA AUTORIZADA");
            document.add(meta);

            Paragraph signature = new Paragraph("\n\n______________________________\nConformidad de entrega del cliente", body);
            signature.setAlignment(Element.ALIGN_CENTER);
            signature.setSpacingBefore(30);
            document.add(signature);
        });
    }

    private interface Content {
        void write(Document document) throws Exception;
    }

    private static byte[] render(Content content) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();
            content.write(document);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not render the work order document", e);
        }
    }

    private void header(Document document, String name, String legalName, String taxId) throws Exception {
        Paragraph workshop = new Paragraph(name, section);
        workshop.setAlignment(Element.ALIGN_CENTER);
        document.add(workshop);
        Paragraph fiscal = new Paragraph(legalName + " — RUC " + taxId, body);
        fiscal.setAlignment(Element.ALIGN_CENTER);
        fiscal.setSpacingAfter(10);
        document.add(fiscal);
    }

    private void pair(PdfPTable table, String label, String value) {
        table.addCell(new PdfPCell(new Phrase(label, bold)));
        table.addCell(new PdfPCell(new Phrase(value != null ? value : "-", body)));
    }

    private void total(PdfPTable table, String label, String currency, BigDecimal amount) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, bold));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);
        table.addCell(new PdfPCell(new Phrase(currency, body)));
        table.addCell(right(money(amount)));
    }

    private PdfPCell right(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, body));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private static String money(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2, RoundingMode.HALF_EVEN).toPlainString();
    }

    private static String plain(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }
}
