package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.iot.domain.model.enums.DtcCategory;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters.DtcCategoryConverter;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters.FaultSeverityConverter;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code dtc_catalog} relational table.
 * Persists standard SAE J2012 / ISO 15031-6 diagnostic trouble codes and their default severities.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "dtc_catalog",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dtc_catalog_code", columnNames = {"dtc_code"})
        }
)
public class DtcCatalogEntryPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "dtc_code", nullable = false, length = 10, unique = true)
    private String dtcCode;

    @Convert(converter = DtcCategoryConverter.class)
    @Column(name = "system_category", nullable = false, length = 50)
    private DtcCategory systemCategory;

    @Column(name = "standard_description", nullable = false, length = 500)
    private String standardDescription;

    @Convert(converter = FaultSeverityConverter.class)
    @Column(name = "default_severity", nullable = false, length = 20)
    private FaultSeverity defaultSeverity;

    public DtcCatalogEntryPersistenceEntity(UUID id) {
        super(id);
    }
}
