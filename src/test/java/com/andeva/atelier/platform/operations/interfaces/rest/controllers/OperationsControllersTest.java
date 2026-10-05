package com.andeva.atelier.platform.operations.interfaces.rest.controllers;

import com.andeva.atelier.platform.operations.application.commandservices.ServiceCommandService;
import com.andeva.atelier.platform.operations.application.commandservices.WorkBayCommandService;
import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.application.queryservices.ServiceQueryService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkBayQueryService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Workshop Operations REST Controllers Unit Tests")
class OperationsControllersTest {

    @Mock
    private WorkOrderCommandService workOrderCommandService;
    @Mock
    private WorkOrderQueryService workOrderQueryService;
    @Mock
    private WorkBayCommandService workBayCommandService;
    @Mock
    private WorkBayQueryService workBayQueryService;
    @Mock
    private ServiceCommandService serviceCommandService;
    @Mock
    private ServiceQueryService serviceQueryService;

    @Test
    @DisplayName("ServicesController should return catalog services")
    void servicesControllerShouldReturnServices() {
        ServicesController controller = new ServicesController(serviceCommandService, serviceQueryService);
        Service service = Service.create(TenantId.of(UUID.randomUUID()), "Afinamiento", Money.soles(new BigDecimal("120.00")), 60);

        when(serviceQueryService.handle(any(com.andeva.atelier.platform.operations.domain.model.queries.GetServicesByTenantIdQuery.class)))
                .thenReturn(List.of(service));

        ResponseEntity<?> response = controller.getServices(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
    }

    @Test
    @DisplayName("WorkBaysController should return 404 when bay not found")
    void workBaysControllerShouldReturnNotFound() {
        WorkBaysController controller = new WorkBaysController(workBayCommandService, workBayQueryService);
        when(workBayQueryService.getById(any(WorkBayId.class))).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getBayById(UUID.randomUUID());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("WorkOrdersController should return work order details when found")
    void workOrdersControllerShouldReturnDetails() {
        WorkOrdersController controller = new WorkOrdersController(
                workOrderCommandService,
                workOrderQueryService,
                workBayQueryService
        );

        WorkOrder order = WorkOrder.create(
                TenantId.of(UUID.randomUUID()),
                BranchId.of(UUID.randomUUID()),
                null,
                VehicleId.of(UUID.randomUUID()),
                CustomerId.of(UUID.randomUUID()),
                WorkOrderNumber.of(500),
                Mileage.of(30000),
                DiagnosticSummary.of("Chequeo general")
        );

        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(order));

        ResponseEntity<?> response = controller.getWorkOrderById(order.getId().value());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
