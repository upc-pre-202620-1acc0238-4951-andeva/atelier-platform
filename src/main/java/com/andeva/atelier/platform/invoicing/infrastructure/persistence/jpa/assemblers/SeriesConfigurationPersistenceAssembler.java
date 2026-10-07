package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.SeriesConfigurationPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

/**
 * Persistence Assembler converting bidirectionally between {@link SeriesConfiguration} aggregate and {@link SeriesConfigurationPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SeriesConfigurationPersistenceAssembler {

    public SeriesConfigurationPersistenceEntity toEntity(SeriesConfiguration domain) {
        if (domain == null) {
            return null;
        }

        return new SeriesConfigurationPersistenceEntity(
                domain.getId().value(),
                domain.getTenantId().value(),
                domain.getBranchId().value(),
                domain.getVoucherType().name(),
                domain.getSerie().value(),
                domain.getCurrentCorrelative(),
                domain.isActive()
        );
    }

    public SeriesConfiguration toDomain(SeriesConfigurationPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new SeriesConfiguration(
                SeriesConfigurationId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                BranchId.of(entity.getBranchId()),
                VoucherType.valueOf(entity.getVoucherType()),
                VoucherSerie.of(entity.getSerie()),
                entity.getCurrentCorrelative(),
                entity.isActive()
        );
    }
}
