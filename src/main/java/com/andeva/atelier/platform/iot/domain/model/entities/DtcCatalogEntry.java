package com.andeva.atelier.platform.iot.domain.model.entities;

import com.andeva.atelier.platform.iot.domain.model.enums.DtcCategory;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.ids.DtcCatalogId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;

import java.io.Serializable;
import java.util.Objects;

/**
 * Child Entity representing a standardized SAE J2012 / ISO 15031-6 Diagnostic Trouble Code
 * master catalog entry for semantic enrichment and default severity assignment.
 *
 * @author Joel Huamani Estefanero
 */
public class DtcCatalogEntry implements Serializable {

    private final DtcCatalogId id;
    private final DtcCode code;
    private final DtcCategory category;
    private final String standardDescription;
    private final FaultSeverity defaultSeverity;

    public DtcCatalogEntry(
            DtcCatalogId id,
            DtcCode code,
            DtcCategory category,
            String standardDescription,
            FaultSeverity defaultSeverity
    ) {
        this.id = Objects.requireNonNull(id, "DtcCatalogId cannot be null");
        this.code = Objects.requireNonNull(code, "DtcCode cannot be null");
        this.category = Objects.requireNonNull(category, "DtcCategory cannot be null");
        this.standardDescription = Objects.requireNonNull(standardDescription, "Standard description cannot be null");
        this.defaultSeverity = Objects.requireNonNull(defaultSeverity, "Default severity cannot be null");
    }

    public static DtcCatalogEntry register(
            DtcCode code,
            DtcCategory category,
            String standardDescription,
            FaultSeverity defaultSeverity
    ) {
        return new DtcCatalogEntry(
                DtcCatalogId.generate(),
                code,
                category,
                standardDescription,
                defaultSeverity
        );
    }

    public static DtcCatalogEntry register(
            DtcCatalogId id,
            DtcCode code,
            DtcCategory category,
            String standardDescription,
            FaultSeverity defaultSeverity
    ) {
        return new DtcCatalogEntry(id, code, category, standardDescription, defaultSeverity);
    }

    public DtcCatalogId getId() {
        return id;
    }

    public DtcCode getCode() {
        return code;
    }

    public DtcCategory getCategory() {
        return category;
    }

    public String getStandardDescription() {
        return standardDescription;
    }

    public FaultSeverity getDefaultSeverity() {
        return defaultSeverity;
    }

    public boolean isCritical() {
        return defaultSeverity.isCritical();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DtcCatalogEntry that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DtcCatalogEntry{" +
                "id=" + id +
                ", code=" + code +
                ", category=" + category +
                ", defaultSeverity=" + defaultSeverity +
                '}';
    }
}
