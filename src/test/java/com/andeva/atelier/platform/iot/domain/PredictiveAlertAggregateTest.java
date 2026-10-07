package com.andeva.atelier.platform.iot.domain;

import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.events.PredictiveAlertAcknowledgedEvent;
import com.andeva.atelier.platform.iot.domain.model.events.PredictiveAlertDispatchedEvent;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for the PredictiveAlert aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
class PredictiveAlertAggregateTest {

    @Test
    @DisplayName("create() factory method should initialize dispatched alert")
    void testCreateAlert() {
        VehicleId vehicleId = VehicleId.generate();
        TenantId tenantId = TenantId.generate();
        ServiceId serviceId = ServiceId.generate();
        ConfidenceScore score = ConfidenceScore.of(88.50);

        PredictiveAlert alert = PredictiveAlert.create(
                vehicleId,
                tenantId,
                Optional.of(serviceId),
                AlertType.ENGINE_OVERHEATING_RISK,
                score,
                "Critical temperature elevation detected."
        );

        assertThat(alert.getId()).isNotNull();
        assertThat(alert.getVehicleId()).isEqualTo(vehicleId);
        assertThat(alert.getTenantId()).isEqualTo(tenantId);
        assertThat(alert.getRecommendedServiceId()).contains(serviceId);
        assertThat(alert.getAlertType()).isEqualTo(AlertType.ENGINE_OVERHEATING_RISK);
        assertThat(alert.getConfidenceScore()).isEqualTo(score);
        assertThat(alert.getMessage()).isEqualTo("Critical temperature elevation detected.");
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.DISPATCHED);
        assertThat(alert.getFcmMessageId()).isEmpty();
        assertThat(alert.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("recordDispatch() should store fcmMessageId and emit PredictiveAlertDispatchedEvent")
    void testRecordDispatch() {
        PredictiveAlert alert = PredictiveAlert.create(
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                AlertType.BATTERY_FAILURE_RISK,
                ConfidenceScore.of(91.20),
                "Battery voltage critically low in rest."
        );

        alert.recordDispatch("fcm-msg-12345");

        assertThat(alert.getFcmMessageId()).contains("fcm-msg-12345");
        assertThat(alert.domainEvents()).hasSize(1);
        assertThat(alert.domainEvents().iterator().next()).isInstanceOf(PredictiveAlertDispatchedEvent.class);

        PredictiveAlertDispatchedEvent event = (PredictiveAlertDispatchedEvent) alert.domainEvents().iterator().next();
        assertThat(event.alertId()).isEqualTo(alert.getId());
        assertThat(event.fcmMessageId()).isEqualTo("fcm-msg-12345");
    }

    @Test
    @DisplayName("acknowledge() should transition status to ACKNOWLEDGED and emit PredictiveAlertAcknowledgedEvent")
    void testAcknowledgeAlert() {
        PredictiveAlert alert = PredictiveAlert.create(
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                AlertType.BATTERY_FAILURE_RISK,
                ConfidenceScore.of(91.20),
                "Battery voltage critically low in rest."
        );

        alert.acknowledge();

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(alert.domainEvents()).hasSize(1);
        assertThat(alert.domainEvents().iterator().next()).isInstanceOf(PredictiveAlertAcknowledgedEvent.class);
    }

    @Test
    @DisplayName("resolve() and dismiss() lifecycle transitions")
    void testResolveAndDismiss() {
        PredictiveAlert alert = PredictiveAlert.create(
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                AlertType.CATALYTIC_SYSTEM_DEGRADATION,
                ConfidenceScore.of(80.00),
                "Catalytic degradation risk."
        );

        alert.resolve();
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.RESOLVED);

        alert.dismiss();
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.DISMISSED);
    }

    @Test
    @DisplayName("acknowledge() on resolved alert should throw IllegalStateException")
    void testAcknowledgeOnTerminalStateThrows() {
        PredictiveAlert alert = PredictiveAlert.create(
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                AlertType.CATALYTIC_SYSTEM_DEGRADATION,
                ConfidenceScore.of(80.00),
                "Catalytic degradation risk."
        );
        alert.resolve();

        org.assertj.core.api.Assertions.assertThatThrownBy(alert::acknowledge)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot acknowledge an alert that is already RESOLVED");
    }
}
