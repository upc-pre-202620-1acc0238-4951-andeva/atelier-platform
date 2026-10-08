package com.andeva.atelier.platform.operations.application.acl;

import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.application.queryservices.ServiceQueryService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkBayQueryService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.MarkWorkOrderAsPaidCommand;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetServicesByTenantIdQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkBaySummaryDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkOrderBillingDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkOrderConsumedProductDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkOrderSummaryDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkshopServiceCatalogAclDto;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WorkshopOperationsContextFacadeImpl (Inbound Open Host Service Facade).
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkshopOperationsContextFacade Unit Tests")
class WorkshopOperationsContextFacadeTest {

    @Mock
    private WorkOrderQueryService workOrderQueryService;

    @Mock
    private WorkBayQueryService workBayQueryService;

    @Mock
    private WorkOrderCommandService workOrderCommandService;

    @Mock
    private ServiceQueryService serviceQueryService;

    private WorkshopOperationsContextFacadeImpl facade;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID branchId = UUID.randomUUID();
    private final UUID vehicleId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        facade = new WorkshopOperationsContextFacadeImpl(
                workOrderQueryService,
                workBayQueryService,
                workOrderCommandService,
                serviceQueryService
        );
    }

    private WorkOrder buildSampleWorkOrder() {
        WorkOrder order = WorkOrder.create(
                TenantId.of(tenantId),
                BranchId.of(branchId),
                null,
                VehicleId.of(vehicleId),
                CustomerId.of(customerId),
                WorkOrderNumber.of(101),
                Mileage.of(15000),
                DiagnosticSummary.of("Diagnóstico inicial de prueba")
        );
        WorkOrderTask task = order.addTask(
                ServiceId.generate(),
                UUID.randomUUID(),
                "Afinamiento de motor",
                Money.soles(new BigDecimal("120.00")),
                LaborHours.of(1.5)
        );
        order.addProductToTask(
                task.getId(),
                UUID.randomUUID(),
                Quantity.of(new BigDecimal("4.0")),
                Money.soles(new BigDecimal("35.00"))
        );
        return order;
    }

    @Test
    @DisplayName("Should fetch work order summary by ID")
    void shouldFetchWorkOrderById() {
        WorkOrder order = buildSampleWorkOrder();
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(order));

        Optional<WorkOrderSummaryDto> dtoOpt = facade.fetchWorkOrderById(order.getId().value());
        assertThat(dtoOpt).isPresent();
        WorkOrderSummaryDto dto = dtoOpt.get();
        assertThat(dto.workOrderId()).isEqualTo(order.getId().value());
        assertThat(dto.tenantId()).isEqualTo(tenantId);
        assertThat(dto.internalSequenceNumber()).isEqualTo(101);
        assertThat(dto.status()).isEqualTo("DRAFT");
        assertThat(dto.totalAmount()).isEqualByComparingTo(order.getTotalAmount().amount());
    }

    @Test
    @DisplayName("Should fetch billing details with calculated labor and product sums")
    void shouldFetchWorkOrderBillingDetails() {
        WorkOrder order = buildSampleWorkOrder();
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(order));

        Optional<WorkOrderBillingDto> billingOpt = facade.fetchWorkOrderBillingDetails(order.getId().value());
        assertThat(billingOpt).isPresent();
        WorkOrderBillingDto billing = billingOpt.get();
        assertThat(billing.laborAmount()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(billing.productsAmount()).isEqualByComparingTo(new BigDecimal("140.00")); // 4 * 35 = 140
        assertThat(billing.totalAmount()).isEqualByComparingTo(order.getTotalAmount().amount());
        assertThat(billing.customerId()).isEqualTo(customerId);
    }

    @Test
    @DisplayName("Should fetch consumed products in order")
    void shouldFetchProductsConsumedInOrder() {
        WorkOrder order = buildSampleWorkOrder();
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(order));

        List<WorkOrderConsumedProductDto> products = facade.fetchProductsConsumedInOrder(order.getId().value());
        assertThat(products).hasSize(1);
        WorkOrderConsumedProductDto prod = products.get(0);
        assertThat(prod.quantity()).isEqualByComparingTo(new BigDecimal("4.0"));
        assertThat(prod.unitPrice()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(prod.totalAmount()).isEqualByComparingTo(new BigDecimal("140.00"));
    }

    @Test
    @DisplayName("Should mark work order as paid via command service")
    void shouldMarkWorkOrderAsPaid() {
        UUID orderId = UUID.randomUUID();
        WorkOrder order = buildSampleWorkOrder();
        when(workOrderCommandService.handle(any(MarkWorkOrderAsPaidCommand.class)))
                .thenReturn(Result.success(order));

        boolean success = facade.markWorkOrderAsPaid(orderId);
        assertThat(success).isTrue();
    }

    @Test
    @DisplayName("Should check bay occupied status and fetch bay summary")
    void shouldCheckBayStatus() {
        UUID bayId = UUID.randomUUID();
        WorkBay bay = WorkBay.create(TenantId.of(tenantId), BranchId.of(branchId), "Elevador 1", BayType.LIFT);
        when(workBayQueryService.getById(new WorkBayId(bayId))).thenReturn(Optional.of(bay));

        assertThat(facade.isBayOccupied(bayId)).isFalse();

        Optional<WorkBaySummaryDto> summaryOpt = facade.fetchBayStatus(bayId);
        assertThat(summaryOpt).isPresent();
        assertThat(summaryOpt.get().name()).isEqualTo("Elevador 1");
        assertThat(summaryOpt.get().status()).isEqualTo("AVAILABLE");
    }

    @Test
    @DisplayName("Should fetch available services catalog for tenant")
    void shouldFetchAvailableServices() {
        Service s1 = Service.create(TenantId.of(tenantId), "Cambio de Aceite", Money.soles(new BigDecimal("50.00")), 30);
        Service s2 = Service.create(TenantId.of(tenantId), "Alineacion Laser", Money.soles(new BigDecimal("80.00")), 45);

        when(serviceQueryService.handle(any(GetServicesByTenantIdQuery.class))).thenReturn(List.of(s1, s2));

        List<WorkshopServiceCatalogAclDto> catalog = facade.fetchAvailableServices(tenantId);
        assertThat(catalog).hasSize(2);
        assertThat(catalog.get(0).name()).isEqualTo("Cambio de Aceite");
        assertThat(catalog.get(0).basePrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(catalog.get(1).name()).isEqualTo("Alineacion Laser");
    }

    @Test
    @DisplayName("Should return empty list when serviceQueryService is null or tenantId is null")
    void shouldHandleNullServiceQueryService() {
        WorkshopOperationsContextFacadeImpl noServiceFacade = new WorkshopOperationsContextFacadeImpl(
                workOrderQueryService,
                workBayQueryService,
                workOrderCommandService
        );
        List<WorkshopServiceCatalogAclDto> list = noServiceFacade.fetchAvailableServices(tenantId);
        assertThat(list).isEmpty();

        List<WorkshopServiceCatalogAclDto> nullTenantList = facade.fetchAvailableServices(null);
        assertThat(nullTenantList).isEmpty();
    }
}
