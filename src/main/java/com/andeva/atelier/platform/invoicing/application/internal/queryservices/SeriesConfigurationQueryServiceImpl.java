package com.andeva.atelier.platform.invoicing.application.internal.queryservices;

import com.andeva.atelier.platform.invoicing.application.queryservices.SeriesConfigurationQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetActiveSeriesQuery;
import com.andeva.atelier.platform.invoicing.domain.repositories.SeriesConfigurationRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of {@link SeriesConfigurationQueryService} providing read-only queries
 * for SUNAT electronic series configurations.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class SeriesConfigurationQueryServiceImpl implements SeriesConfigurationQueryService {

    private final SeriesConfigurationRepository seriesRepository;

    public SeriesConfigurationQueryServiceImpl(SeriesConfigurationRepository seriesRepository) {
        this.seriesRepository = Objects.requireNonNull(seriesRepository, "SeriesConfigurationRepository cannot be null");
    }

    @Override
    public Optional<SeriesConfiguration> handle(GetActiveSeriesQuery query) {
        Objects.requireNonNull(query, "GetActiveSeriesQuery cannot be null");
        return seriesRepository.findByTenantIdAndBranchIdAndVoucherTypeAndActive(
                query.tenantId(),
                query.branchId(),
                query.voucherType()
        );
    }

    @Override
    public Optional<SeriesConfiguration> getSeriesById(SeriesConfigurationId id) {
        Objects.requireNonNull(id, "SeriesConfigurationId cannot be null");
        return seriesRepository.findById(id);
    }

    @Override
    public List<SeriesConfiguration> getSeriesByBranch(TenantId tenantId, BranchId branchId) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        return seriesRepository.findAllByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public Optional<SeriesConfiguration> getActiveSeriesByBranchAndType(TenantId tenantId, BranchId branchId, VoucherType voucherType) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(voucherType, "VoucherType cannot be null");
        return seriesRepository.findByTenantIdAndBranchIdAndVoucherTypeAndActive(tenantId, branchId, voucherType);
    }
}
