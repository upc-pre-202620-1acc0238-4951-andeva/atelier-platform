package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.converters.EngineTypeAttributeConverter;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code vehicles} relational table.
 * Universal vehicle asset independent of TenantId.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicles_plate", columnNames = {"plate"})
        },
        indexes = {
                @Index(name = "idx_vehicles_plate", columnList = "plate"),
                @Index(name = "idx_vehicles_vin", columnList = "vin")
        }
)
public class VehiclePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "plate", nullable = false, unique = true, length = 15)
    private String plate;

    @Column(name = "vin", length = 17)
    private String vin;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "year", nullable = false)
    private int year;

    @Convert(converter = EngineTypeAttributeConverter.class)
    @Column(name = "engine_type", nullable = false, length = 20)
    private EngineType engineType;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("startDate DESC")
    private List<VehicleOwnershipPersistenceEntity> ownershipHistory = new ArrayList<>();

    public VehiclePersistenceEntity(UUID id) {
        super(id);
    }

    public VehiclePersistenceEntity(
            UUID id,
            String plate,
            String vin,
            String brand,
            String model,
            int year,
            EngineType engineType
    ) {
        super(id);
        this.plate = plate;
        this.vin = vin;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.engineType = engineType;
        this.ownershipHistory = new ArrayList<>();
    }

    public void addOwnership(VehicleOwnershipPersistenceEntity ownership) {
        this.ownershipHistory.add(ownership);
        ownership.setVehicle(this);
    }
}
