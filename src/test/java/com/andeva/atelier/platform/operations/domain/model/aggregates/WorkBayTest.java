package com.andeva.atelier.platform.operations.domain.model.aggregates;

import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkBay Aggregate Domain Tests")
class WorkBayTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final BranchId branchId = BranchId.of(UUID.randomUUID());

    @Test
    @DisplayName("Should create WorkBay in AVAILABLE status")
    void shouldCreateWorkBayInAvailableStatus() {
        WorkBay bay = WorkBay.create(tenantId, branchId, "Elevador Hidráulico #1", BayType.LIFT);

        assertThat(bay.getId()).isNotNull();
        assertThat(bay.getName()).isEqualTo("Elevador Hidráulico #1");
        assertThat(bay.getType()).isEqualTo(BayType.LIFT);
        assertThat(bay.getStatus()).isEqualTo(BayStatus.AVAILABLE);
        assertThat(bay.getCurrentWorkOrderId()).isEmpty();
    }

    @Test
    @DisplayName("Should occupy and release WorkBay properly")
    void shouldOccupyAndReleaseWorkBay() {
        WorkBay bay = WorkBay.create(tenantId, branchId, "Bahía de Alineamiento", BayType.ALIGNMENT);
        WorkOrderId orderId = WorkOrderId.generate();

        bay.occupy(orderId);
        assertThat(bay.getStatus()).isEqualTo(BayStatus.OCCUPIED);
        assertThat(bay.getCurrentWorkOrderId()).contains(orderId);

        bay.release();
        assertThat(bay.getStatus()).isEqualTo(BayStatus.AVAILABLE);
        assertThat(bay.getCurrentWorkOrderId()).isEmpty();
    }

    @Test
    @DisplayName("Should reject occupying a bay that is already OCCUPIED")
    void shouldRejectOccupyingOccupiedBay() {
        WorkBay bay = WorkBay.create(tenantId, branchId, "Cabina de Pintura", BayType.PAINT_BOOTH);
        bay.occupy(WorkOrderId.generate());

        assertThatThrownBy(() -> bay.occupy(WorkOrderId.generate()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot occupy bay in status");
    }

    @Test
    @DisplayName("Should handle maintenance lifecycle transitions")
    void shouldHandleMaintenanceLifecycle() {
        WorkBay bay = WorkBay.create(tenantId, branchId, "Zona de Lavado", BayType.WASHING);

        bay.setUnderMaintenance("Calibración y limpieza de boquillas");
        assertThat(bay.getStatus()).isEqualTo(BayStatus.MAINTENANCE);

        bay.restoreAvailable();
        assertThat(bay.getStatus()).isEqualTo(BayStatus.AVAILABLE);
    }
}
