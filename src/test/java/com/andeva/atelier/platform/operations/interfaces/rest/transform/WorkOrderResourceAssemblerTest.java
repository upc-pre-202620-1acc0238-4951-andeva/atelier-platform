package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderDetailResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Work order resources expose the cascade subtotal, IGV and total (US22, US36)")
class WorkOrderResourceAssemblerTest {

    private WorkOrder orderWithTask() {
        WorkOrder order = WorkOrder.create(TenantId.of(UUID.randomUUID()), BranchId.of(UUID.randomUUID()), null,
                VehicleId.of(UUID.randomUUID()), CustomerId.of(UUID.randomUUID()), WorkOrderNumber.of(1),
                Mileage.of(1000), DiagnosticSummary.of("Ruido en frenos"));
        order.addTask(ServiceId.of(UUID.randomUUID()), UUID.randomUUID(), "Cambio de pastillas",
                Money.soles(new BigDecimal("100.00")), LaborHours.of(BigDecimal.ONE));
        order.recalculateTotalAmount();
        return order;
    }

    @Test
    @DisplayName("The plain resource carries subtotal, tax and total")
    void shouldExposeAmountsOnResource() {
        WorkOrderResource resource = WorkOrderResourceAssembler.toResourceFromEntity(orderWithTask());

        assertThat(resource.subtotal()).isEqualByComparingTo("100.00");
        assertThat(resource.taxAmount()).isEqualByComparingTo("18.00");
        assertThat(resource.totalAmount()).isEqualByComparingTo("118.00");
    }

    @Test
    @DisplayName("The detail resource carries the same breakdown")
    void shouldExposeAmountsOnDetail() {
        WorkOrderDetailResource detail = WorkOrderResourceAssembler.toDetailResourceFromEntity(orderWithTask(), null, List.of(), List.of());

        assertThat(detail.subtotal()).isEqualByComparingTo("100.00");
        assertThat(detail.taxAmount()).isEqualByComparingTo("18.00");
        assertThat(detail.totalAmount()).isEqualByComparingTo("118.00");
    }
}
