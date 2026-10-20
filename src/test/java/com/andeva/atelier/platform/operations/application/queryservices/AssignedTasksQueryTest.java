package com.andeva.atelier.platform.operations.application.queryservices;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.TenancyAclService;
import com.andeva.atelier.platform.operations.application.internal.queryservices.WorkOrderQueryServiceImpl;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetTasksAssignedToMechanicQuery;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tasks assigned to the authenticated mechanic")
class AssignedTasksQueryTest {

    @Mock
    private WorkOrderRepository workOrderRepository;
    @Mock
    private TenancyAclService tenancyAclService;

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final UUID userId = UUID.randomUUID();
    private final UUID membershipId = UUID.randomUUID();

    private WorkOrder order(int number) {
        return WorkOrder.create(tenantId, BranchId.of(UUID.randomUUID()), null, VehicleId.of(UUID.randomUUID()),
                CustomerId.of(UUID.randomUUID()), WorkOrderNumber.of(number), Mileage.of(1000), DiagnosticSummary.of("x"));
    }

    private WorkOrderTask task(WorkOrder order, UUID mechanic, String description) {
        return order.addTask(ServiceId.of(UUID.randomUUID()), mechanic, description,
                Money.soles(new BigDecimal("50.00")), LaborHours.of(BigDecimal.ONE));
    }

    @Test
    @DisplayName("Returns only the mechanic's open tasks, in progress before the rest")
    void shouldListOnlyOwnOpenTasksInProgressFirst() {
        WorkOrder first = order(1);
        task(first, membershipId, "Pendiente");
        task(first, UUID.randomUUID(), "De otro mecánico");
        WorkOrder second = order(2);
        WorkOrderTask started = task(second, membershipId, "En curso");
        started.start();

        when(tenancyAclService.resolveActiveMembershipId(userId, tenantId.value())).thenReturn(Optional.of(membershipId));
        when(workOrderRepository.findActiveWithTasksAssignedTo(tenantId, membershipId)).thenReturn(List.of(first, second));

        var service = new WorkOrderQueryServiceImpl(workOrderRepository, tenancyAclService);
        List<AssignedTaskView> tasks = service.handle(new GetTasksAssignedToMechanicQuery(tenantId, userId));

        assertThat(tasks).extracting(view -> view.task().getDescription()).containsExactly("En curso", "Pendiente");
    }

    @Test
    @DisplayName("Returns nothing when the user has no active membership in the tenant")
    void shouldReturnEmptyWithoutMembership() {
        when(tenancyAclService.resolveActiveMembershipId(userId, tenantId.value())).thenReturn(Optional.empty());

        var service = new WorkOrderQueryServiceImpl(workOrderRepository, tenancyAclService);

        assertThat(service.handle(new GetTasksAssignedToMechanicQuery(tenantId, userId))).isEmpty();
        verifyNoInteractions(workOrderRepository);
    }
}
