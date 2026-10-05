package com.andeva.atelier.platform.operations.domain.model.aggregates;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Service Aggregate Domain Tests")
class ServiceTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());

    @Test
    @DisplayName("Should create Service item with valid parameters")
    void shouldCreateServiceSuccessfully() {
        Service service = Service.create(tenantId, "Alineamiento y Balanceo Computarizado", Money.soles(new BigDecimal("80.00")), 45);

        assertThat(service.getId()).isNotNull();
        assertThat(service.getName()).isEqualTo("Alineamiento y Balanceo Computarizado");
        assertThat(service.getBasePrice().amount()).isEqualByComparingTo("80.00");
        assertThat(service.getEstimatedDurationMinutes()).isEqualTo(45);
    }

    @Test
    @DisplayName("Should reject invalid service name")
    void shouldRejectInvalidServiceName() {
        assertThatThrownBy(() -> Service.create(tenantId, "A", Money.soles(new BigDecimal("50.00")), 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service name must be between 3 and 150 characters");
    }

    @Test
    @DisplayName("Should reject zero or negative estimated duration")
    void shouldRejectNonPositiveEstimatedDuration() {
        assertThatThrownBy(() -> Service.create(tenantId, "Mantenimiento General", Money.soles(new BigDecimal("50.00")), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("estimatedDurationMinutes must be positive");
    }
}
