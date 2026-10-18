package com.andeva.atelier.platform.invoicing.infrastructure.external.tax.nubefact;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CreditNoteReference;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Nubefact JSON v1 payload mapping of {@link NubefactPseFiscalAdapter}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Nubefact PSE Adapter Payload Mapping Tests")
class NubefactPseFiscalAdapterTest {

    private final NubefactPseFiscalAdapter adapter =
            new NubefactPseFiscalAdapter("https://api.nubefact.com/api/v1/route", "nubefact_token_placeholder");
    private final PeruvianTaxCalculationEngine taxEngine = new PeruvianTaxCalculationEngine();

    private final TaxCalculation taxCalc = TaxCalculation.of(
            Money.of(new BigDecimal("100.00"), Currency.PEN),
            Money.of(new BigDecimal("18.00"), Currency.PEN),
            Money.of(new BigDecimal("118.00"), Currency.PEN)
    );

    @Test
    @DisplayName("Should map an invoice to a generar_comprobante payload")
    @SuppressWarnings("unchecked")
    void shouldMapInvoicePayload() {
        ElectronicVoucher invoice = issue(VoucherType.FACTURA, "F001",
                CustomerFiscalInfo.of(TaxId.ruc("20100070970"), "SUNAT SAC", "AV. GARCILASO 315, LIMA", DocumentType.RUC));

        Map<String, Object> payload = adapter.buildVoucherPayload(invoice);

        assertThat(payload)
                .containsEntry("operacion", "generar_comprobante")
                .containsEntry("tipo_de_comprobante", 1)
                .containsEntry("serie", "F001")
                .containsEntry("numero", 7)
                .containsEntry("cliente_tipo_de_documento", "6")
                .containsEntry("cliente_numero_de_documento", "20100070970")
                .containsEntry("moneda", 1)
                .containsEntry("total_gravada", new BigDecimal("100.00"))
                .containsEntry("total_igv", new BigDecimal("18.00"))
                .containsEntry("total", new BigDecimal("118.00"))
                .containsEntry("enviar_automaticamente_a_la_sunat", true);

        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
        assertThat(items).hasSize(1);
        assertThat(items.get(0))
                .containsEntry("unidad_de_medida", "NIU")
                .containsEntry("tipo_de_igv", 1)
                .containsEntry("total", new BigDecimal("118.00"));
    }

    @Test
    @DisplayName("Should map anonymous receipts to the 'VARIOS' customer document")
    void shouldMapAnonymousReceipt() {
        ElectronicVoucher receipt = issue(VoucherType.BOLETA, "B001", CustomerFiscalInfo.anonymous());

        Map<String, Object> payload = adapter.buildVoucherPayload(receipt);

        assertThat(payload)
                .containsEntry("tipo_de_comprobante", 2)
                .containsEntry("cliente_tipo_de_documento", "-")
                .containsEntry("cliente_numero_de_documento", "-")
                .containsEntry("cliente_denominacion", "CLIENTES VARIOS");
    }

    @Test
    @DisplayName("Should map credit notes with the modified document reference")
    void shouldMapCreditNoteReference() {
        CustomerFiscalInfo fiscal = CustomerFiscalInfo.of(TaxId.ruc("20100070970"), "SUNAT SAC", "LIMA", DocumentType.RUC);
        ElectronicVoucher invoice = issue(VoucherType.FACTURA, "F001", fiscal);
        ElectronicVoucher creditNote = ElectronicVoucher.issueCreditNote(
                invoice.getTenantId(), invoice.getBranchId(), invoice.getCustomerId(), null,
                VoucherSerie.of("FC01"), VoucherNumber.of(1),
                fiscal, Currency.PEN, taxCalc,
                CreditNoteReference.of(invoice.getId(), invoice.getSerie(), invoice.getNumber(), CreditNoteReason.DEVOLUCION_TOTAL, "Devolución"),
                List.of(line(VoucherItemType.SERVICE))
        );

        Map<String, Object> payload = adapter.buildVoucherPayload(creditNote);

        assertThat(payload)
                .containsEntry("tipo_de_comprobante", 3)
                .containsEntry("documento_que_se_modifica_tipo", 1)
                .containsEntry("documento_que_se_modifica_serie", "F001")
                .containsEntry("documento_que_se_modifica_numero", 7)
                .containsEntry("tipo_de_nota_de_credito", 6);
    }

    private ElectronicVoucher issue(VoucherType type, String serie, CustomerFiscalInfo fiscal) {
        return ElectronicVoucher.issue(
                TenantId.generate(), BranchId.generate(), CustomerId.generate(), WorkOrderId.generate(),
                type, VoucherSerie.of(serie), VoucherNumber.of(7),
                fiscal, Currency.PEN, taxCalc,
                List.of(line(VoucherItemType.PRODUCT))
        );
    }

    private com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine line(VoucherItemType type) {
        return taxEngine.calculateLine(null, VoucherId.generate(), Optional.empty(), type, "Aceite sintético",
                Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN));
    }
}
