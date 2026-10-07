package com.andeva.atelier.platform.invoicing.interfaces.rest.advice;

import com.andeva.atelier.platform.invoicing.domain.exceptions.*;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for {@link InvoicingExceptionHandler}.
 * Verifies RFC 7807 problem details, error codes, HTTP status codes,
 * and bilingual internationalization (i18n) in English and Spanish.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("InvoicingExceptionHandler Unit Tests")
class InvoicingExceptionHandlerTest {

    private InvoicingExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");

        handler = new InvoicingExceptionHandler(messageSource);
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/invoicing/test");
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    @DisplayName("InvalidTaxIdException maps to 400 BAD_REQUEST with proper error code")
    void handleInvalidTaxId() {
        InvalidTaxIdException ex = new InvalidTaxIdException("123");
        ResponseEntity<ProblemDetail> response = handler.handleInvalidTaxId(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getType().toString()).contains("invalid-tax-id");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_INVALID_TAX_ID");
    }

    @Test
    @DisplayName("CustomerFiscalDataMissingException maps to 400 BAD_REQUEST")
    void handleCustomerFiscalDataMissing() {
        CustomerFiscalDataMissingException ex = new CustomerFiscalDataMissingException("RUC missing for Factura");
        ResponseEntity<ProblemDetail> response = handler.handleCustomerFiscalDataMissing(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getType().toString()).contains("customer-fiscal-data-missing");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_CUSTOMER_FISCAL_MISSING");
    }

    @Test
    @DisplayName("InvalidVoucherAmountException maps to 400 BAD_REQUEST")
    void handleInvalidVoucherAmount() {
        InvalidVoucherAmountException ex = new InvalidVoucherAmountException("Negative amount");
        ResponseEntity<ProblemDetail> response = handler.handleInvalidVoucherAmount(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getType().toString()).contains("invalid-voucher-amount");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_INVALID_VOUCHER_AMOUNT");
    }

    @Test
    @DisplayName("VoucherAlreadyPaidException maps to 409 CONFLICT")
    void handleVoucherAlreadyPaid() {
        VoucherAlreadyPaidException ex = new VoucherAlreadyPaidException(VoucherId.generate());
        ResponseEntity<ProblemDetail> response = handler.handleVoucherAlreadyPaid(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getType().toString()).contains("voucher-already-paid");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_VOUCHER_ALREADY_PAID");
    }

    @Test
    @DisplayName("VoucherNotFoundException maps to 404 NOT_FOUND")
    void handleVoucherNotFound() {
        VoucherNotFoundException ex = new VoucherNotFoundException(VoucherId.generate());
        ResponseEntity<ProblemDetail> response = handler.handleVoucherNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getType().toString()).contains("voucher-not-found");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_VOUCHER_NOT_FOUND");
    }

    @Test
    @DisplayName("SeriesNotFoundException maps to 404 NOT_FOUND")
    void handleSeriesNotFound() {
        SeriesNotFoundException ex = new SeriesNotFoundException(new VoucherSerie("F001"));
        ResponseEntity<ProblemDetail> response = handler.handleSeriesNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getType().toString()).contains("series-not-found");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_SERIES_NOT_FOUND");
    }

    @Test
    @DisplayName("CreditNoteReferenceNotFoundException maps to 404 NOT_FOUND")
    void handleCreditNoteReferenceNotFound() {
        CreditNoteReferenceNotFoundException ex = new CreditNoteReferenceNotFoundException(VoucherId.generate());
        ResponseEntity<ProblemDetail> response = handler.handleCreditNoteReferenceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getType().toString()).contains("credit-note-reference-not-found");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_CREDIT_NOTE_REF_NOT_FOUND");
    }

    @Test
    @DisplayName("CorrelativeExhaustedException maps to 409 CONFLICT")
    void handleCorrelativeExhausted() {
        CorrelativeExhaustedException ex = new CorrelativeExhaustedException(new VoucherSerie("F001"));
        ResponseEntity<ProblemDetail> response = handler.handleCorrelativeExhausted(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getType().toString()).contains("correlative-exhausted");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_CORRELATIVE_EXHAUSTED");
    }

    @Test
    @DisplayName("VoucherImmutableException maps to 409 CONFLICT")
    void handleVoucherImmutable() {
        VoucherImmutableException ex = new VoucherImmutableException(VoucherId.generate());
        ResponseEntity<ProblemDetail> response = handler.handleVoucherImmutable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getType().toString()).contains("voucher-immutable");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_VOUCHER_IMMUTABLE");
    }

    @Test
    @DisplayName("SunatIntegrationException maps to 502 BAD_GATEWAY")
    void handleSunatIntegration() {
        SunatIntegrationException ex = new SunatIntegrationException("SUNAT timeout");
        ResponseEntity<ProblemDetail> response = handler.handleSunatIntegration(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(502);
        assertThat(response.getBody().getType().toString()).contains("sunat-error");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_SUNAT_INTEGRATION_FAILED");
    }

    @Test
    @DisplayName("Generic InvoicingDomainException maps to 422 UNPROCESSABLE_ENTITY")
    void handleInvoicingDomain() {
        InvoicingDomainException ex = new InvoicingDomainException("Generic domain error") {};
        ResponseEntity<ProblemDetail> response = handler.handleInvoicingDomain(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getType().toString()).contains("invoicing-business-rule");
    }

    @Test
    @DisplayName("AccessDeniedException maps to 403 FORBIDDEN")
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Tenant context required");
        ResponseEntity<ProblemDetail> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getType().toString()).contains("forbidden");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ERR_ACCESS_DENIED");
    }

    @Test
    @DisplayName("IllegalArgumentException maps to 400 BAD_REQUEST")
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid state transition");
        ResponseEntity<ProblemDetail> response = handler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getType().toString()).contains("bad-request");
    }

    @Test
    @DisplayName("Bilingual i18n: Spanish locale resolves Spanish message from messages_es.properties")
    void i18nSpanishResolution() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        VoucherNotFoundException ex = new VoucherNotFoundException(VoucherId.generate());
        ResponseEntity<ProblemDetail> response = handler.handleVoucherNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isNotEmpty();
    }
}
