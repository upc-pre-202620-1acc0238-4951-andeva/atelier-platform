package com.andeva.atelier.platform.shared.interfaces.rest.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for {@link CorrelationIdFilter}.
 * Verifies distributed trace correlation identifier generation, ingestion,
 * response header decoration, SLF4J MDC context propagation, and MDC cleanup.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("CorrelationIdFilter Unit Tests")
class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate UUID and attach to response & MDC when header is missing")
    void shouldGenerateAndPropagateCorrelationIdWhenHeaderIsMissing() throws ServletException, IOException {
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            mdcValueInsideChain.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
        };

        filter.doFilter(request, response, chain);

        String responseHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(responseHeader).isNotNull().isNotBlank();
        assertThat(mdcValueInsideChain.get()).isEqualTo(responseHeader);
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Should preserve and propagate existing correlation ID when header is present")
    void shouldPreserveExistingCorrelationIdWhenHeaderIsPresent() throws ServletException, IOException {
        String existingId = "trace-custom-abc-123";
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, existingId);

        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            mdcValueInsideChain.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
        };

        filter.doFilter(request, response, chain);

        String responseHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(responseHeader).isEqualTo(existingId);
        assertThat(mdcValueInsideChain.get()).isEqualTo(existingId);
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Should generate new UUID when incoming header is blank or whitespace")
    void shouldGenerateCorrelationIdWhenHeaderIsBlank() throws ServletException, IOException {
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "   ");

        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            mdcValueInsideChain.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
        };

        filter.doFilter(request, response, chain);

        String responseHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(responseHeader).isNotNull().isNotBlank().isNotEqualTo("   ");
        assertThat(mdcValueInsideChain.get()).isEqualTo(responseHeader);
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Should clean up MDC even if filter chain throws an unhandled exception")
    void shouldCleanUpMdcEvenWhenExceptionIsThrown() {
        FilterChain throwingChain = (req, res) -> {
            assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNotNull();
            throw new ServletException("Simulated filter chain failure");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, throwingChain))
                .isInstanceOf(ServletException.class)
                .hasMessage("Simulated filter chain failure");

        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }
}
