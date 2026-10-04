package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.Vin;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.VehicleOwnershipPersistenceEntity;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.ArrayList;
import java.util.List;

public final class VehiclePersistenceAssembler {

    private VehiclePersistenceAssembler() {
    }

    public static Vehicle toDomain(VehiclePersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        List<VehicleOwnership> ownerships = new ArrayList<>();
        if (entity.getOwnershipHistory() != null) {
            for (VehicleOwnershipPersistenceEntity ownershipEntity : entity.getOwnershipHistory()) {
                ownerships.add(VehicleOwnershipPersistenceAssembler.toDomain(ownershipEntity));
            }
        }

        Vin vin = entity.getVin() != null ? Vin.of(entity.getVin()) : null;

        return new Vehicle(
                VehicleId.of(entity.getId()),
                LicensePlate.of(entity.getPlate()),
                vin,
                entity.getBrand(),
                entity.getModel(),
                entity.getYear(),
                entity.getEngineType(),
                entity.getCurrentMileage(),
                ownerships
        );
    }

    public static VehiclePersistenceEntity toEntity(Vehicle domain) {
        if (domain == null) {
            return null;
        }

        String vinString = domain.vin() != null ? domain.vin().value() : null;

        VehiclePersistenceEntity entity = new VehiclePersistenceEntity(
                domain.id().value(),
                domain.plate().value(),
                vinString,
                domain.brand(),
                domain.model(),
                domain.year(),
                domain.engineType()
        );

        entity.setCurrentMileage(domain.currentMileage());

        if (domain.ownershipHistory() != null) {
            for (VehicleOwnership ownership : domain.ownershipHistory()) {
                entity.addOwnership(VehicleOwnershipPersistenceAssembler.toEntity(ownership, entity));
            }
        }

        return entity;
    }

    public static void updateEntity(VehiclePersistenceEntity entity, Vehicle domain) {
        if (entity == null || domain == null) {
            return;
        }
        entity.setPlate(domain.plate().value());
        entity.setVin(domain.vin() != null ? domain.vin().value() : null);
        entity.setBrand(domain.brand());
        entity.setModel(domain.model());
        entity.setYear(domain.year());
        entity.setEngineType(domain.engineType());
        entity.setCurrentMileage(domain.currentMileage());
        if (domain.ownershipHistory() != null) {
            entity.getOwnershipHistory().clear();
            for (VehicleOwnership ownership : domain.ownershipHistory()) {
                entity.addOwnership(VehicleOwnershipPersistenceAssembler.toEntity(ownership, entity));
            }
        }
    }
}
