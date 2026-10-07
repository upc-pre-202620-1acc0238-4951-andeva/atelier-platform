package com.andeva.atelier.platform.invoicing.application.queryservices;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetActiveSeriesQuery;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Query Service interface for retrieving SUNAT series configurations.
 *
 * @author Joel Huamani Estefanero
 */
public interface SeriesConfigurationQueryService {

    /**
     * Retrieves an active series configuration for a tenant branch and voucher type.
     */
    Optional<SeriesConfiguration> handle(GetActiveSeriesQuery query);

    /**
     * Retrieves a series configuration by its unique identifier.
     */
    Optional<SeriesConfiguration> getSeriesById(SeriesConfigurationId id);

    /**
     * Retrieves all series configurations defined for a tenant branch.
     */
    List<SeriesConfiguration> getSeriesByBranch(TenantId tenantId, BranchId branchId);

    /**
     * Retrieves the active series configuration for a tenant branch and voucher type.
     */
    Optional<SeriesConfiguration> getActiveSeriesByBranchAndType(TenantId tenantId, BranchId branchId, VoucherType voucherType);
}
