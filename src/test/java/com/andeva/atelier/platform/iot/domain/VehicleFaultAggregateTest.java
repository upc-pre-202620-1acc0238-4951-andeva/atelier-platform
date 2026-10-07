package com.andeva.atelier.platform.iot.domain;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.events.VehicleFaultDetectedEvent;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for the VehicleFault aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
class VehicleFaultAggregateTest {

    @Test
    @DisplayName("detect() factory method should initialize unresolved fault and emit VehicleFaultDetectedEvent")
    void testDetectFault() {
        VehicleId vehicleId = VehicleId.generate();
        TenantId tenantId = TenantId.generate();
        DtcCode dtcCode = DtcCode.of("P0300");

        VehicleFault fault = VehicleFault.detect(
                vehicleId,
                tenantId,
                dtcCode,
                FaultSeverity.CRITICAL,
                "Random/Multiple Cylinder Misfire Detected"
        );

        assertThat(fault.getId()).isNotNull();
        assertThat(fault.getVehicleId()).isEqualTo(vehicleId);
        assertThat(fault.getTenantId()).isEqualTo(tenantId);
        assertThat(fault.getDtcCode()).isEqualTo(dtcCode);
        assertThat(fault.getSeverity()).isEqualTo(FaultSeverity.CRITICAL);
        assertThat(fault.getDescription()).isEqualTo("Random/Multiple Cylinder Misfire Detected");
        assertThat(fault.getDetectedAt()).isNotNull();
        assertThat(fault.isResolved()).isFalse();
        assertThat(fault.getResolvedAt()).isEmpty();

        assertThat(fault.domainEvents()).hasSize(1);
        assertThat(fault.domainEvents().iterator().next()).isInstanceOf(VehicleFaultDetectedEvent.class);

        VehicleFaultDetectedEvent event = (VehicleFaultDetectedEvent) fault.domainEvents().iterator().next();
        assertThat(event.faultId()).isEqualTo(fault.getId());
        assertThat(event.vehicleId()).isEqualTo(vehicleId);
        assertThat(event.tenantId()).isEqualTo(tenantId);
        assertThat(event.dtcCode()).isEqualTo(dtcCode);
        assertThat(event.severity()).isEqualTo(FaultSeverity.CRITICAL);
    }

    @Test
    @DisplayName("markResolved() should transition fault to resolved state with timestamp")
    void testMarkResolved() {
        VehicleFault fault = VehicleFault.detect(
                VehicleId.generate(),
                TenantId.generate(),
                DtcCode.of("P0420"),
                FaultSeverity.MEDIUM,
                "Catalyst System Efficiency Below Threshold"
        );

        Instant resolvedTime = Instant.now();
        fault.markResolved(resolvedTime);

        assertThat(fault.isResolved()).isTrue();
        assertThat(fault.getResolvedAt()).contains(resolvedTime);
    }

    @Test
    @DisplayName("markResolved() with timestamp prior to detectedAt should throw IllegalArgumentException")
    void testMarkResolvedPriorTimestampThrows() {
        VehicleFault fault = VehicleFault.detect(
                VehicleId.generate(),
                TenantId.generate(),
                DtcCode.of("P0420"),
                FaultSeverity.MEDIUM,
                "Catalyst System Efficiency"
        );

        Instant prior = fault.getDetectedAt().minusSeconds(60);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> fault.markResolved(prior))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("prior to detection timestamp");
    }
}
