package com.andeva.atelier.platform.invoicing.domain.repositories;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for {@link SeriesConfiguration} aggregate roots.
 *
 * @author Joel Huamani Estefanero
 */
public interface SeriesConfigurationRepository {

    SeriesConfiguration save(SeriesConfiguration seriesConfig);

    Optional<SeriesConfiguration> findById(SeriesConfigurationId id);

    Optional<SeriesConfiguration> findByTenantIdAndBranchIdAndVoucherTypeAndActive(
            TenantId tenantId,
            BranchId branchId,
            VoucherType type
    );

    Optional<SeriesConfiguration> findByTenantIdAndBranchIdAndVoucherTypeAndSerie(
            TenantId tenantId,
            BranchId branchId,
            VoucherType type,
            VoucherSerie serie
    );

    List<SeriesConfiguration> findAllByTenantIdAndBranchId(TenantId tenantId, BranchId branchId);

    boolean existsByTenantIdAndBranchIdAndSerie(TenantId tenantId, BranchId branchId, VoucherSerie serie);
}
