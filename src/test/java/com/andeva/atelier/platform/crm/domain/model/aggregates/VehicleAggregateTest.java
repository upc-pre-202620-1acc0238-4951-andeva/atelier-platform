package com.andeva.atelier.platform.crm.domain.model.aggregates;

import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.Vin;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Vehicle Aggregate Unit Tests")
class VehicleAggregateTest {

    @Test
    @DisplayName("Should successfully register vehicle with customer ownership")
    void shouldRegisterVehicleWithCustomerOwnership() {
        VehicleId id = VehicleId.generate();
        LicensePlate plate = LicensePlate.of("ABC123");
        Vin vin = Vin.of("1HGCR2F83HA000000");
        CustomerId ownerId = CustomerId.of(UUID.randomUUID());

        Vehicle vehicle = Vehicle.register(
                id,
                plate,
                vin,
                "Honda",
                "Civic",
                2022,
                EngineType.GASOLINE,
                Optional.of(ownerId),
                Optional.empty()
        );

        assertThat(vehicle.id()).isEqualTo(id);
        assertThat(vehicle.plate().value()).isEqualTo("ABC123");
        assertThat(vehicle.vin().value()).isEqualTo("1HGCR2F83HA000000");
        assertThat(vehicle.brand()).isEqualTo("Honda");
        assertThat(vehicle.model()).isEqualTo("Civic");
        assertThat(vehicle.year()).isEqualTo(2022);
        assertThat(vehicle.engineType()).isEqualTo(EngineType.GASOLINE);
        assertThat(vehicle.getActiveOwnership()).isPresent();
        assertThat(vehicle.getActiveOwnership().get().getCustomerId()).isEqualTo(ownerId);
        assertThat(vehicle.domainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should transfer vehicle ownership closing prior custody")
    void shouldTransferOwnership() {
        VehicleId id = VehicleId.generate();
        CustomerId originalOwner = CustomerId.of(UUID.randomUUID());
        CustomerId newOwner = CustomerId.of(UUID.randomUUID());

        Vehicle vehicle = Vehicle.register(
                id,
                LicensePlate.of("XYZ789"),
                null,
                "Toyota",
                "Corolla",
                2020,
                EngineType.HYBRID,
                Optional.of(originalOwner),
                Optional.empty()
        );

        vehicle.clearDomainEvents();
        LocalDate transferDate = LocalDate.now();
        VehicleOwnership newOwnership = vehicle.transferOwnership(newOwner, transferDate);

        assertThat(newOwnership.getCustomerId()).isEqualTo(newOwner);
        assertThat(newOwnership.getEndDate()).isNull();
        assertThat(vehicle.getActiveOwnership()).isPresent();
        assertThat(vehicle.getActiveOwnership().get().getCustomerId()).isEqualTo(newOwner);
        assertThat(vehicle.ownershipHistory()).hasSize(2);
        assertThat(vehicle.domainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when registering without any owner")
    void shouldThrowWhenNoOwnerProvided() {
        VehicleId id = VehicleId.generate();
        LicensePlate plate = LicensePlate.of("DEF456");

        assertThatThrownBy(() -> Vehicle.register(
                id,
                plate,
                null,
                "Nissan",
                "Sentra",
                2021,
                EngineType.GASOLINE,
                Optional.empty(),
                Optional.empty()
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
