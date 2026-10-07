package com.andeva.atelier.platform.invoicing.application.commandservices;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.commands.ConfigureSeriesCommand;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;

/**
 * Command Service interface orchestrating the authorization, setup,
 * and lifecycle management of fiscal series configurations.
 *
 * @author Joel Huamani Estefanero
 */
public interface SeriesConfigurationCommandService {

    /**
     * Configures a new authorized fiscal series for a branch and voucher type.
     */
    SeriesConfiguration handle(ConfigureSeriesCommand command);

    /**
     * Deactivates an active fiscal series, blocking new correlative allocations.
     */
    default void deactivateSeries(SeriesConfigurationId id) {
        deactivateSeries(id, null);
    }

    /**
     * Deactivates an active fiscal series with multi-tenant verification.
     */
    void deactivateSeries(SeriesConfigurationId id, com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId tenantId);

    /**
     * Reactivates an existing fiscal series for voucher issuance.
     */
    default void activateSeries(SeriesConfigurationId id) {
        activateSeries(id, null);
    }

    /**
     * Reactivates an existing fiscal series with multi-tenant verification.
     */
    void activateSeries(SeriesConfigurationId id, com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId tenantId);
}
