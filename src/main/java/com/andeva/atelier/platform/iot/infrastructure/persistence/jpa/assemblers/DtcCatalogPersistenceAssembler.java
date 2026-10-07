package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iot.domain.model.entities.DtcCatalogEntry;
import com.andeva.atelier.platform.iot.domain.model.ids.DtcCatalogId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.DtcCatalogEntryPersistenceEntity;
import org.springframework.stereotype.Component;

/**
 * Assembler for bidirectional translation between domain {@link DtcCatalogEntry}
 * entities and JPA {@link DtcCatalogEntryPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class DtcCatalogPersistenceAssembler {

    public DtcCatalogEntryPersistenceEntity toEntity(DtcCatalogEntry domain) {
        if (domain == null) {
            return null;
        }
        var entity = new DtcCatalogEntryPersistenceEntity();
        entity.setId(domain.getId().value());
        entity.setDtcCode(domain.getCode().value());
        entity.setSystemCategory(domain.getCategory());
        entity.setStandardDescription(domain.getStandardDescription());
        entity.setDefaultSeverity(domain.getDefaultSeverity());
        return entity;
    }

    public DtcCatalogEntry toDomain(DtcCatalogEntryPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DtcCatalogEntry(
                new DtcCatalogId(entity.getId()),
                new DtcCode(entity.getDtcCode()),
                entity.getSystemCategory(),
                entity.getStandardDescription(),
                entity.getDefaultSeverity()
        );
    }
}
