package com.andeva.atelier.platform.invoicing.application.internal.commandservices;

import com.andeva.atelier.platform.invoicing.application.commandservices.SeriesConfigurationCommandService;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.SeriesNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.commands.ConfigureSeriesCommand;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.repositories.SeriesConfigurationRepository;
import com.andeva.atelier.platform.invoicing.domain.services.SeriesCorrelativeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Transactional Command Service managing fiscal series configurations,
 * authorization, and lifecycle status.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class SeriesConfigurationCommandServiceImpl implements SeriesConfigurationCommandService {

    private final SeriesConfigurationRepository seriesRepository;
    private final SeriesCorrelativeService correlativeService;

    public SeriesConfigurationCommandServiceImpl(
            SeriesConfigurationRepository seriesRepository,
            SeriesCorrelativeService correlativeService
    ) {
        this.seriesRepository = Objects.requireNonNull(seriesRepository, "Series repository cannot be null");
        this.correlativeService = Objects.requireNonNull(correlativeService, "Correlative service cannot be null");
    }

    @Override
    public SeriesConfiguration handle(ConfigureSeriesCommand command) {
        Objects.requireNonNull(command, "ConfigureSeriesCommand cannot be null");

        correlativeService.validateSeriesFormat(command.serie(), command.type());

        boolean exists = seriesRepository.existsByTenantIdAndBranchIdAndSerie(
                command.tenantId(),
                command.branchId(),
                command.serie()
        );

        if (exists) {
            throw new InvoicingDomainException(
                    "ERR_SERIES_ALREADY_EXISTS",
                    "Fiscal series '" + command.serie().value() + "' is already registered for this branch."
            );
        }

        SeriesConfiguration series = SeriesConfiguration.create(
                command.tenantId(),
                command.branchId(),
                command.type(),
                command.serie(),
                command.initialCorrelative()
        );

        return seriesRepository.save(series);
    }

    @Override
    public void deactivateSeries(SeriesConfigurationId id) {
        deactivateSeries(id, null);
    }

    @Override
    public void deactivateSeries(SeriesConfigurationId id, com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId tenantId) {
        Objects.requireNonNull(id, "SeriesConfigurationId cannot be null");

        SeriesConfiguration series = seriesRepository.findById(id)
                .orElseThrow(() -> new SeriesNotFoundException("Series not found: " + id.value()));

        if (tenantId != null && !series.getTenantId().equals(tenantId)) {
            throw new InvoicingDomainException("ERR_CROSS_TENANT_ACCESS", "Cannot modify series belonging to another tenant.");
        }

        series.deactivate();
        seriesRepository.save(series);
    }

    @Override
    public void activateSeries(SeriesConfigurationId id) {
        activateSeries(id, null);
    }

    @Override
    public void activateSeries(SeriesConfigurationId id, com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId tenantId) {
        Objects.requireNonNull(id, "SeriesConfigurationId cannot be null");

        SeriesConfiguration series = seriesRepository.findById(id)
                .orElseThrow(() -> new SeriesNotFoundException("Series not found: " + id.value()));

        if (tenantId != null && !series.getTenantId().equals(tenantId)) {
            throw new InvoicingDomainException("ERR_CROSS_TENANT_ACCESS", "Cannot modify series belonging to another tenant.");
        }

        series.activate();
        seriesRepository.save(series);
    }
}
