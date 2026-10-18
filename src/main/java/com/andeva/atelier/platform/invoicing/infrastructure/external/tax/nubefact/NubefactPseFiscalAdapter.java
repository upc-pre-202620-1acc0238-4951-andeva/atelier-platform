package com.andeva.atelier.platform.invoicing.infrastructure.external.tax.nubefact;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.NubefactPseFiscalGateway;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CreditNoteReference;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Outbound Adapter implementing {@link NubefactPseFiscalGateway} against the Nubefact JSON API v1.
 * <p>
 * Every operation is a POST to the account RUTA authenticated with its TOKEN. Nubefact signs the
 * UBL 2.1 XML, forwards it to SUNAT and hosts the PDF/XML/CDR, so the returned links are stored as-is.
 * When no real token is configured (local development and tests) the adapter simulates acceptance.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class NubefactPseFiscalAdapter implements NubefactPseFiscalGateway {

    private static final Logger log = LoggerFactory.getLogger(NubefactPseFiscalAdapter.class);

    private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");
    private static final DateTimeFormatter NUBEFACT_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final int ERROR_DOCUMENT_ALREADY_EXISTS = 23;

    /** Marks every simulated SUNAT response so it is never mistaken for a real one. */
    public static final String SIMULATED_TAG = "[SIMULADO]";
    public static final String SIMULATED_PREFIX = "SIMULADO-";

    private final String baseUrl;
    private final String apiToken;
    private final boolean mockMode;
    private final RestClient restClient;

    public NubefactPseFiscalAdapter(
            @Value("${app.invoicing.nubefact.base-url:https://api.nubefact.com/api/v1}") String baseUrl,
            @Value("${app.invoicing.nubefact.token:nubefact_token_placeholder}") String apiToken
    ) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.mockMode = apiToken == null || apiToken.isBlank() || apiToken.contains("placeholder") || apiToken.contains("mock");
        this.restClient = RestClient.builder().build();
        if (mockMode) {
            log.warn("Nubefact running in SIMULATED mode: vouchers are accepted locally and never sent to SUNAT");
        }
    }

    @Override
    public NubefactDispatchResult dispatchVoucher(ElectronicVoucher voucher) {
        Objects.requireNonNull(voucher, "ElectronicVoucher cannot be null");
        return dispatch(voucher);
    }

    @Override
    public NubefactDispatchResult dispatchCreditNote(ElectronicVoucher creditNote) {
        Objects.requireNonNull(creditNote, "Credit note ElectronicVoucher cannot be null");
        return dispatch(creditNote);
    }

    @Override
    public NubefactVoidResult voidVoucher(ElectronicVoucher voucher, String voidReason) {
        Objects.requireNonNull(voucher, "ElectronicVoucher cannot be null");
        String voucherRef = reference(voucher);

        if (mockMode) {
            String ticketNumber = SIMULATED_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("[Simulated Nubefact] Voiding communication for voucher {}: ticket {}", voucherRef, ticketNumber);
            return new NubefactVoidResult(true, "0",
                    SIMULATED_TAG + " Comunicación de baja " + ticketNumber + " registrada en modo demostración, sin envío a SUNAT.",
                    ticketNumber);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("operacion", "generar_anulacion");
        payload.put("tipo_de_comprobante", nubefactVoucherType(voucher.getVoucherType()));
        payload.put("serie", voucher.getSerie().value());
        payload.put("numero", voucher.getNumber().value());
        payload.put("motivo", truncate(voidReason != null && !voidReason.isBlank() ? voidReason : "ANULACION DE LA OPERACION", 100));

        log.info("Sending voiding communication (baja) for voucher {} to Nubefact", voucherRef);
        Map<String, Object> response = post(payload);

        String responseCode = asString(response.get("sunat_responsecode"));
        boolean rejected = responseCode != null && !responseCode.isBlank() && !"0".equals(responseCode);
        return new NubefactVoidResult(
                !rejected,
                responseCode != null ? responseCode : "0",
                firstNonBlank(asString(response.get("sunat_description")), "Comunicación de baja registrada en Nubefact"),
                asString(response.get("sunat_ticket_numero"))
        );
    }

    private NubefactDispatchResult dispatch(ElectronicVoucher voucher) {
        String voucherRef = reference(voucher);

        if (mockMode) {
            log.info("[Simulated Nubefact] Simulating SUNAT acceptance for voucher {}", voucherRef);
            // No PDF/XML/CDR exists for a simulated voucher, so no links are stored
            return new NubefactDispatchResult(
                    true,
                    "0",
                    SIMULATED_TAG + " Comprobante " + voucherRef + " aceptado en modo demostración, sin validez ante SUNAT.",
                    SIMULATED_PREFIX + computeHash(voucher.getTenantId().value() + ":" + voucherRef + ":" + voucher.getTaxCalculation().totalAmount().amount()),
                    DigitalReceiptUrls.empty()
            );
        }

        log.info("Dispatching voucher {} to Nubefact", voucherRef);
        Map<String, Object> response;
        try {
            response = post(buildVoucherPayload(voucher));
        } catch (NubefactIntegrationException ex) {
            if (ex.errorCode() != ERROR_DOCUMENT_ALREADY_EXISTS) {
                throw ex;
            }
            // Retried dispatch: the document was already generated, fetch its current state instead
            log.info("Voucher {} already exists in Nubefact, querying its status", voucherRef);
            response = post(Map.of(
                    "operacion", "consultar_comprobante",
                    "tipo_de_comprobante", nubefactVoucherType(voucher.getVoucherType()),
                    "serie", voucher.getSerie().value(),
                    "numero", voucher.getNumber().value()
            ));
        }
        return toDispatchResult(voucherRef, response);
    }

    /**
     * Builds the "generar_comprobante" JSON body for invoices, receipts and credit notes.
     */
    Map<String, Object> buildVoucherPayload(ElectronicVoucher voucher) {
        TaxCalculation tax = voucher.getTaxCalculation();
        CustomerFiscalInfo customer = voucher.getCustomerFiscalInfo();
        boolean identifiedCustomer = customer.taxId() != null;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("operacion", "generar_comprobante");
        payload.put("tipo_de_comprobante", nubefactVoucherType(voucher.getVoucherType()));
        payload.put("serie", voucher.getSerie().value());
        payload.put("numero", voucher.getNumber().value());
        payload.put("sunat_transaction", 1);
        payload.put("cliente_tipo_de_documento", identifiedCustomer ? customer.documentType().getSunatCode() : "-");
        payload.put("cliente_numero_de_documento", identifiedCustomer ? customer.taxId().value() : "-");
        payload.put("cliente_denominacion", truncate(firstNonBlank(customer.legalName(), "CLIENTES VARIOS"), 100));
        payload.put("cliente_direccion", truncate(firstNonBlank(customer.fiscalAddress(), "-"), 100));
        payload.put("fecha_de_emision", LocalDate.ofInstant(voucher.getIssuedAt(), LIMA_ZONE).format(NUBEFACT_DATE));
        payload.put("moneda", voucher.getCurrency() == Currency.USD ? 2 : 1);
        payload.put("porcentaje_de_igv", tax.igvRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        payload.put("total_gravada", money(tax.subtotal().amount()));
        payload.put("total_igv", money(tax.igvAmount().amount()));
        payload.put("total", money(tax.totalAmount().amount()));
        payload.put("enviar_automaticamente_a_la_sunat", true);
        payload.put("enviar_automaticamente_al_cliente", false);
        payload.put("codigo_unico", truncate(voucher.getVoucherType().getSunatCode() + "-" + voucher.getSerie().value() + "-" + voucher.getNumber().value(), 20));

        voucher.getCreditNoteReference().ifPresent(ref -> addCreditNoteReference(payload, ref));

        payload.put("items", voucher.getLines().stream().map(this::toItem).toList());
        return payload;
    }

    private void addCreditNoteReference(Map<String, Object> payload, CreditNoteReference ref) {
        payload.put("documento_que_se_modifica_tipo", ref.referenceSerie().value().startsWith("B") ? 2 : 1);
        payload.put("documento_que_se_modifica_serie", ref.referenceSerie().value());
        payload.put("documento_que_se_modifica_numero", ref.referenceNumber().value());
        payload.put("tipo_de_nota_de_credito", nubefactCreditNoteType(ref.reason()));
        payload.put("observaciones", truncate(ref.reasonDescription(), 1000));
    }

    private Map<String, Object> toItem(VoucherLine line) {
        BigDecimal quantity = line.getQuantity().value();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("unidad_de_medida", line.getItemType() == VoucherItemType.SERVICE ? "ZZ" : "NIU");
        line.getItemId().ifPresent(id -> item.put("codigo", id.toString()));
        item.put("descripcion", truncate(line.getDescription(), 250));
        item.put("cantidad", quantity);
        item.put("valor_unitario", line.getUnitValue().amount());
        item.put("precio_unitario", line.getUnitPrice().amount());
        item.put("subtotal", money(line.getTotalLine().amount().subtract(line.getIgvAmount().amount())));
        item.put("tipo_de_igv", 1);
        item.put("igv", money(line.getIgvAmount().amount()));
        item.put("total", money(line.getTotalLine().amount()));
        item.put("anticipo_regularizacion", false);
        return item;
    }

    private NubefactDispatchResult toDispatchResult(String voucherRef, Map<String, Object> response) {
        String responseCode = asString(response.get("sunat_responsecode"));
        String description = firstNonBlank(
                asString(response.get("sunat_description")),
                asString(response.get("sunat_soap_error")),
                "Comprobante " + voucherRef + " generado en Nubefact; envío a SUNAT en proceso."
        );
        // Only an explicit SUNAT response code other than "0" is a rejection; a pending SUNAT
        // submission is retried by Nubefact itself and must not void the correlative.
        boolean rejected = responseCode != null && !responseCode.isBlank() && !"0".equals(responseCode)
                && !Boolean.TRUE.equals(response.get("aceptada_por_sunat"));

        DigitalReceiptUrls urls = DigitalReceiptUrls.of(
                asString(response.get("enlace_del_pdf")),
                asString(response.get("enlace_del_xml")),
                asString(response.get("enlace_del_cdr"))
        );
        return new NubefactDispatchResult(
                !rejected,
                responseCode != null ? responseCode : "0",
                description,
                firstNonBlank(asString(response.get("codigo_hash")), ""),
                urls
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(Map<String, Object> payload) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri(baseUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Token token=\"" + apiToken + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            if (response == null) {
                throw new NubefactIntegrationException(0, "Empty response from Nubefact");
            }
            if (response.containsKey("errors")) {
                throw new NubefactIntegrationException(asInt(response.get("codigo")), asString(response.get("errors")));
            }
            return response;
        } catch (RestClientResponseException ex) {
            Map<String, Object> error = ex.getResponseBodyAs(Map.class);
            int code = error != null ? asInt(error.get("codigo")) : 0;
            String message = error != null ? asString(error.get("errors")) : ex.getStatusText();
            throw new NubefactIntegrationException(code, "HTTP " + ex.getStatusCode().value() + ": " + message);
        }
    }

    private static int nubefactVoucherType(VoucherType type) {
        return switch (type) {
            case FACTURA -> 1;
            case BOLETA -> 2;
            case NOTA_CREDITO -> 3;
        };
    }

    private static int nubefactCreditNoteType(CreditNoteReason reason) {
        return switch (reason) {
            case ANULACION_DE_LA_OPERACION -> 1;
            case ANULACION_POR_ERROR_EN_EL_RUC -> 2;
            case CORRECCION_POR_ERROR_EN_LA_DESCRIPCION -> 3;
            case DESCUENTO_GLOBAL -> 4;
            case DEVOLUCION_TOTAL -> 6;
        };
    }

    private static String reference(ElectronicVoucher voucher) {
        return voucher.getSerie().value() + "-" + voucher.getNumber().format();
    }

    private static BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String clean = value.replace("\"", "").trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    private static int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value != null ? Integer.parseInt(value.toString()) : 0;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    /**
     * Raised when Nubefact rejects the request itself (authentication, format, duplicates, account state).
     */
    static final class NubefactIntegrationException extends RuntimeException {

        private final int errorCode;

        NubefactIntegrationException(int errorCode, String message) {
            super("Nubefact error " + errorCode + ": " + message);
            this.errorCode = errorCode;
        }

        int errorCode() {
            return errorCode;
        }
    }
}
