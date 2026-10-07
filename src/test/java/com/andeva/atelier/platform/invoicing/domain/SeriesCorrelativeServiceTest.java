package com.andeva.atelier.platform.invoicing.domain;

import com.andeva.atelier.platform.invoicing.domain.exceptions.CorrelativeExhaustedException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.services.SeriesCorrelativeService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for SeriesCorrelativeService domain service.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("SeriesCorrelativeService Domain Service Tests")
class SeriesCorrelativeServiceTest {

    private SeriesCorrelativeService correlativeService;

    @BeforeEach
    void setUp() {
        correlativeService = new SeriesCorrelativeService();
    }

    @Test
    @DisplayName("Should sequentially advance and allocate correlative numbers")
    void shouldAllocateNextCorrelative() {
        SeriesConfiguration series = SeriesConfiguration.create(
                TenantId.generate(),
                com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId.generate(),
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                0
        );

        VoucherNumber first = correlativeService.allocateNext(series);
        assertThat(first.value()).isEqualTo(1);
        assertThat(first.format()).isEqualTo("00000001");

        VoucherNumber second = correlativeService.allocateNext(series);
        assertThat(second.value()).isEqualTo(2);
        assertThat(second.format()).isEqualTo("00000002");
    }

    @Test
    @DisplayName("Should validate format prefixes for Factura, Boleta, and Credit Note")
    void shouldValidateSeriesFormat() {
        // Valid formats
        assertThatCode(() -> correlativeService.validateSeriesFormat(VoucherSerie.of("F001"), VoucherType.FACTURA))
                .doesNotThrowAnyException();
        assertThatCode(() -> correlativeService.validateSeriesFormat(VoucherSerie.of("B001"), VoucherType.BOLETA))
                .doesNotThrowAnyException();
        assertThatCode(() -> correlativeService.validateSeriesFormat(VoucherSerie.of("FC01"), VoucherType.NOTA_CREDITO))
                .doesNotThrowAnyException();
        assertThatCode(() -> correlativeService.validateSeriesFormat(VoucherSerie.of("BC01"), VoucherType.NOTA_CREDITO))
                .doesNotThrowAnyException();

        // Invalid formats
        assertThatThrownBy(() -> correlativeService.validateSeriesFormat(VoucherSerie.of("B001"), VoucherType.FACTURA))
                .isInstanceOf(InvoicingDomainException.class)
                .hasMessageContaining("Factura series must start with 'F'");

        assertThatThrownBy(() -> correlativeService.validateSeriesFormat(VoucherSerie.of("F001"), VoucherType.BOLETA))
                .isInstanceOf(InvoicingDomainException.class)
                .hasMessageContaining("Boleta series must start with 'B'");

        assertThatThrownBy(() -> correlativeService.validateSeriesFormat(VoucherSerie.of("T001"), VoucherType.NOTA_CREDITO))
                .isInstanceOf(InvoicingDomainException.class)
                .hasMessageContaining("Credit note series must start with");
    }

    @Test
    @DisplayName("Should reject null arguments")
    void shouldRejectNullArguments() {
        assertThatThrownBy(() -> correlativeService.allocateNext(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> correlativeService.validateSeriesFormat(null, VoucherType.FACTURA))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> correlativeService.validateSeriesFormat(VoucherSerie.of("F001"), null))
                .isInstanceOf(NullPointerException.class);
    }
}
