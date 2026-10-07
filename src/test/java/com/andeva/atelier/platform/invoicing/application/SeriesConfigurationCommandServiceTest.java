package com.andeva.atelier.platform.invoicing.application;

import com.andeva.atelier.platform.invoicing.application.commandservices.SeriesConfigurationCommandService;
import com.andeva.atelier.platform.invoicing.application.internal.commandservices.SeriesConfigurationCommandServiceImpl;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.SeriesNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.commands.ConfigureSeriesCommand;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.repositories.SeriesConfigurationRepository;
import com.andeva.atelier.platform.invoicing.domain.services.SeriesCorrelativeService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SeriesConfigurationCommandServiceImpl.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("SeriesConfigurationCommandServiceImpl Tests")
class SeriesConfigurationCommandServiceTest {

    private SeriesConfigurationRepository seriesRepository;
    private SeriesCorrelativeService correlativeService;
    private SeriesConfigurationCommandService commandService;

    private final TenantId tenantId = TenantId.generate();
    private final BranchId branchId = BranchId.generate();

    @BeforeEach
    void setUp() {
        seriesRepository = Mockito.mock(SeriesConfigurationRepository.class);
        correlativeService = new SeriesCorrelativeService();
        commandService = new SeriesConfigurationCommandServiceImpl(seriesRepository, correlativeService);

        when(seriesRepository.save(any(SeriesConfiguration.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("Should successfully configure and persist a new fiscal series")
    void shouldConfigureSeries() {
        when(seriesRepository.existsByTenantIdAndBranchIdAndSerie(tenantId, branchId, VoucherSerie.of("F001")))
                .thenReturn(false);

        ConfigureSeriesCommand command = new ConfigureSeriesCommand(
                tenantId,
                branchId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                1
        );

        SeriesConfiguration series = commandService.handle(command);

        assertThat(series).isNotNull();
        assertThat(series.getTenantId()).isEqualTo(tenantId);
        assertThat(series.getBranchId()).isEqualTo(branchId);
        assertThat(series.getVoucherType()).isEqualTo(VoucherType.FACTURA);
        assertThat(series.getSerie().value()).isEqualTo("F001");
        assertThat(series.getCurrentCorrelative()).isEqualTo(1);
        assertThat(series.isActive()).isTrue();

        verify(seriesRepository).save(any(SeriesConfiguration.class));
    }

    @Test
    @DisplayName("Should reject configuring duplicate series for same branch")
    void shouldRejectDuplicateSeries() {
        when(seriesRepository.existsByTenantIdAndBranchIdAndSerie(tenantId, branchId, VoucherSerie.of("F001")))
                .thenReturn(true);

        ConfigureSeriesCommand command = new ConfigureSeriesCommand(
                tenantId,
                branchId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                1
        );

        assertThatThrownBy(() -> commandService.handle(command))
                .isInstanceOf(InvoicingDomainException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("Should activate and deactivate series configuration")
    void shouldToggleSeriesActivation() {
        SeriesConfiguration series = SeriesConfiguration.create(
                tenantId, branchId, VoucherType.FACTURA, VoucherSerie.of("F001"), 0
        );

        when(seriesRepository.findById(series.getId())).thenReturn(Optional.of(series));

        // Deactivate
        commandService.deactivateSeries(series.getId());
        assertThat(series.isActive()).isFalse();

        // Reactivate
        commandService.activateSeries(series.getId());
        assertThat(series.isActive()).isTrue();

        verify(seriesRepository, Mockito.times(2)).save(series);
    }

    @Test
    @DisplayName("Should throw SeriesNotFoundException when activating non-existent series")
    void shouldThrowWhenSeriesNotFound() {
        SeriesConfigurationId missingId = SeriesConfigurationId.generate();
        when(seriesRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandService.deactivateSeries(missingId))
                .isInstanceOf(SeriesNotFoundException.class);
    }
}
