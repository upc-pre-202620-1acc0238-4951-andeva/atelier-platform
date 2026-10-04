package com.andeva.atelier.platform.crm.domain.model.aggregates;

import com.andeva.atelier.platform.crm.domain.exceptions.VehicleActiveOwnershipNotFoundException;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.domain.model.events.VehicleOwnershipTransferredEvent;
import com.andeva.atelier.platform.crm.domain.model.events.VehicleRegisteredEvent;
import com.andeva.atelier.platform.crm.domain.model.ids.VehicleOwnershipId;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.Vin;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Universal Aggregate Root representing an automotive vehicle asset across the platform.
 * Independent of TenantId to maintain complete clinical history across workshops.
 *
 * @author Adiel Sanchez Santin
 */
public class Vehicle extends AbstractDomainAggregateRoot<Vehicle> {

    private final VehicleId id;
    private final LicensePlate plate;
    private Vin vin;
    private String brand;
    private String model;
    private int year;
    private EngineType engineType;
    private Integer currentMileage;
    private final List<VehicleOwnership> ownershipHistory;

    public Vehicle(
            VehicleId id,
            LicensePlate plate,
            Vin vin,
            String brand,
            String model,
            int year,
            EngineType engineType,
            Integer currentMileage,
            List<VehicleOwnership> ownershipHistory
    ) {
        this.id = Objects.requireNonNull(id, "VehicleId cannot be null");
        this.plate = Objects.requireNonNull(plate, "LicensePlate cannot be null");
        this.brand = validateBrand(brand);
        this.model = validateModel(model);
        this.year = validateYear(year);
        this.engineType = Objects.requireNonNull(engineType, "EngineType cannot be null");
        this.vin = vin;
        this.currentMileage = currentMileage != null && currentMileage >= 0 ? currentMileage : null;
        this.ownershipHistory = ownershipHistory != null ? new ArrayList<>(ownershipHistory) : new ArrayList<>();
    }

    public Vehicle(
            VehicleId id,
            LicensePlate plate,
            Vin vin,
            String brand,
            String model,
            int year,
            EngineType engineType,
            List<VehicleOwnership> ownershipHistory
    ) {
        this(id, plate, vin, brand, model, year, engineType, null, ownershipHistory);
    }

    public static Vehicle register(
            VehicleId id,
            LicensePlate plate,
            Vin vin,
            String brand,
            String model,
            int year,
            EngineType engineType,
            Optional<CustomerId> initialOwnerId,
            Optional<UserId> initialUserId
    ) {
        if ((initialOwnerId == null || initialOwnerId.isEmpty()) && (initialUserId == null || initialUserId.isEmpty())) {
            throw new IllegalArgumentException("A vehicle must be registered with an initial customer or user owner");
        }

        Vehicle vehicle = new Vehicle(
                id,
                plate,
                vin,
                brand,
                model,
                year,
                engineType,
                new ArrayList<>()
        );

        VehicleOwnership initialOwnership = VehicleOwnership.create(
                VehicleOwnershipId.generate(),
                id,
                initialOwnerId != null ? initialOwnerId.orElse(null) : null,
                initialUserId != null ? initialUserId.orElse(null) : null,
                LocalDate.now()
        );
        vehicle.ownershipHistory.add(initialOwnership);

        CustomerId eventOwnerId = initialOwnerId != null ? initialOwnerId.orElse(null) : null;
        vehicle.registerEvent(VehicleRegisteredEvent.of(id, plate, eventOwnerId));
        return vehicle;
    }

    public VehicleOwnership transferOwnership(CustomerId newOwnerId, LocalDate transferDate) {
        Objects.requireNonNull(newOwnerId, "New owner ID cannot be null");
        Objects.requireNonNull(transferDate, "Transfer date cannot be null");

        VehicleOwnership activeOwnership = getActiveOwnership()
                .orElseThrow(() -> new VehicleActiveOwnershipNotFoundException(this.id.value()));

        CustomerId previousOwnerId = activeOwnership.getCustomerId();
        if (previousOwnerId != null && previousOwnerId.equals(newOwnerId)) {
            throw new IllegalArgumentException("Vehicle is already owned by this customer");
        }

        activeOwnership.terminate(transferDate);

        VehicleOwnership newOwnership = VehicleOwnership.create(
                VehicleOwnershipId.generate(),
                this.id,
                newOwnerId,
                null,
                transferDate
        );
        this.ownershipHistory.add(newOwnership);

        registerEvent(VehicleOwnershipTransferredEvent.of(this.id, previousOwnerId, newOwnerId, transferDate));
        return newOwnership;
    }

    public void linkCustomer(CustomerId customerId) {
        VehicleOwnership activeOwnership = getActiveOwnership()
                .orElseThrow(() -> new VehicleActiveOwnershipNotFoundException(this.id.value()));
        activeOwnership.linkCustomer(customerId);
    }

    public Optional<VehicleOwnership> getActiveOwnership() {
        return this.ownershipHistory.stream()
                .filter(VehicleOwnership::isCurrent)
                .findFirst();
    }

    public Optional<CustomerId> getCurrentOwnerId() {
        return getActiveOwnership().map(VehicleOwnership::getCustomerId);
    }

    public void updateTechnicalDetails(Vin newVin, EngineType newEngineType) {
        this.vin = newVin;
        if (newEngineType != null) {
            this.engineType = newEngineType;
        }
    }

    public void updateTechnicalData(String brand, String model, int year, EngineType engineType) {
        this.brand = validateBrand(brand);
        this.model = validateModel(model);
        this.year = validateYear(year);
        if (engineType != null) {
            this.engineType = engineType;
        }
    }

    public void updateMileage(int newMileage) {
        if (newMileage < 0) {
            throw new IllegalArgumentException("El kilometraje no puede ser negativo");
        }
        if (this.currentMileage != null && newMileage < this.currentMileage) {
            throw new com.andeva.atelier.platform.crm.domain.exceptions.InvalidMileageException(this.currentMileage, newMileage);
        }
        this.currentMileage = newMileage;
    }

    public Integer currentMileage() {
        return currentMileage;
    }

    public VehicleId id() {
        return id;
    }

    public LicensePlate plate() {
        return plate;
    }

    public Vin vin() {
        return vin;
    }

    public String brand() {
        return brand;
    }

    public String model() {
        return model;
    }

    public int year() {
        return year;
    }

    public EngineType engineType() {
        return engineType;
    }

    public List<VehicleOwnership> ownershipHistory() {
        return Collections.unmodifiableList(ownershipHistory);
    }

    private static String validateBrand(String brand) {
        Objects.requireNonNull(brand, "Vehicle brand cannot be null");
        String trimmed = brand.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Vehicle brand cannot be empty");
        }
        return trimmed;
    }

    private static String validateModel(String model) {
        Objects.requireNonNull(model, "Vehicle model cannot be null");
        String trimmed = model.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Vehicle model cannot be empty");
        }
        return trimmed;
    }

    private static int validateYear(int year) {
        int maxYear = Year.now().getValue() + 1;
        if (year < 1950 || year > maxYear) {
            throw new IllegalArgumentException(String.format("Vehicle manufacturing year must be between 1950 and %d", maxYear));
        }
        return year;
    }
}
