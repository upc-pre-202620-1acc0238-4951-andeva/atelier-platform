package com.andeva.atelier.platform.crm.domain.model.aggregates;

import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentAlreadyArrivedException;
import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentInvalidStateTransitionException;
import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentPastDateException;
import com.andeva.atelier.platform.crm.domain.model.enums.AppointmentStatus;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Appointment Aggregate Unit Tests")
class AppointmentAggregateTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final BranchId branchId = BranchId.of(UUID.randomUUID());
    private final CustomerId customerId = CustomerId.of(UUID.randomUUID());
    private final VehicleId vehicleId = VehicleId.of(UUID.randomUUID());

    @Test
    @DisplayName("Should schedule appointment in pending state and record domain event")
    void shouldScheduleAppointment() {
        AppointmentId id = AppointmentId.generate();
        Instant futureTime = Instant.now().plus(Duration.ofDays(2));

        Appointment appointment = Appointment.schedule(
                id,
                tenantId,
                branchId,
                customerId,
                vehicleId,
                futureTime,
                45,
                "Oil change and brake check"
        );

        assertThat(appointment.id()).isEqualTo(id);
        assertThat(appointment.status()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(appointment.scheduledAt()).isEqualTo(futureTime);
        assertThat(appointment.estimatedDurationMinutes()).isEqualTo(45);
        assertThat(appointment.domainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when scheduling appointment in past date")
    void shouldThrowWhenSchedulingInPast() {
        AppointmentId id = AppointmentId.generate();
        Instant pastTime = Instant.now().minus(Duration.ofHours(1));

        assertThatThrownBy(() -> Appointment.schedule(
                id,
                tenantId,
                branchId,
                customerId,
                vehicleId,
                pastTime,
                30,
                "Tune up"
        )).isInstanceOf(AppointmentPastDateException.class);
    }

    @Test
    @DisplayName("Should progress through status transitions: confirm, arrive")
    void shouldProgressStatusTransitions() {
        Instant futureTime = Instant.now().plus(Duration.ofDays(1));
        Appointment appointment = Appointment.schedule(
                AppointmentId.generate(),
                tenantId,
                branchId,
                customerId,
                vehicleId,
                futureTime,
                30,
                "Inspection"
        );

        appointment.confirm();
        assertThat(appointment.status()).isEqualTo(AppointmentStatus.CONFIRMED);

        appointment.markArrived();
        assertThat(appointment.status()).isEqualTo(AppointmentStatus.ARRIVED);

        assertThatThrownBy(appointment::markArrived)
                .isInstanceOf(AppointmentAlreadyArrivedException.class);
    }

    @Test
    @DisplayName("Should cancel appointment with reason")
    void shouldCancelAppointment() {
        Instant futureTime = Instant.now().plus(Duration.ofDays(1));
        Appointment appointment = Appointment.schedule(
                AppointmentId.generate(),
                tenantId,
                branchId,
                customerId,
                vehicleId,
                futureTime,
                30,
                "Inspection"
        );

        appointment.cancel("Client called to cancel due to trip");
        assertThat(appointment.status()).isEqualTo(AppointmentStatus.CANCELED);
        assertThat(appointment.cancellationReason()).isEqualTo("Client called to cancel due to trip");

        assertThatThrownBy(appointment::confirm)
                .isInstanceOf(AppointmentInvalidStateTransitionException.class);
    }
}
