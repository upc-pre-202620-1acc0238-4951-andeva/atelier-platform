package com.andeva.atelier.platform.operations.domain.model.aggregates;

import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderTaskLabelsQueryService;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskProduct;
import com.andeva.atelier.platform.operations.domain.model.events.ProductStockReservationCancelledEvent;
import com.andeva.atelier.platform.operations.domain.model.events.ProductStockReservationRequestedEvent;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers.WorkOrderTaskPersistenceAssembler;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskPersistenceEntity;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderTaskResource;
import com.andeva.atelier.platform.operations.interfaces.rest.transform.WorkOrderTaskResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Spare part lines of a task: withdrawal, quantity correction and display names")
class WorkOrderTaskProductLinesTest {

    private final UUID productId = UUID.randomUUID();
    private WorkOrder order;
    private WorkOrderTask task;
    private WorkOrderTaskProduct line;

    @BeforeEach
    void setUp() {
        order = WorkOrder.create(TenantId.of(UUID.randomUUID()), BranchId.of(UUID.randomUUID()), null,
                VehicleId.of(UUID.randomUUID()), CustomerId.of(UUID.randomUUID()), WorkOrderNumber.of(1),
                Mileage.of(1000), DiagnosticSummary.of("Prueba de carga de la bateria"));
        task = order.addTask(ServiceId.of(UUID.randomUUID()), null, "Lectura de codigos",
                Money.soles(new BigDecimal("80.00")), LaborHours.of(new BigDecimal("1.0")));
        order.addProductToTask(task.getId(), productId, Quantity.of(new BigDecimal("1")), Money.soles(new BigDecimal("30.00")));
        line = task.getConsumedProducts().get(0);
        order.clearDomainEvents();
    }

    @Test
    @DisplayName("H38: raising the quantity updates the line and reserves only the extra units")
    void shouldReserveOnlyTheExtraUnits() {
        order.updateProductQuantityInTask(task.getId(), line.getId().value(), Quantity.of(new BigDecimal("3")), null);

        assertThat(line.getQuantity().value()).isEqualByComparingTo("3");
        assertThat(line.getTotalAmount().amount()).isEqualByComparingTo("90.00");
        assertThat(order.domainEvents()).singleElement().isInstanceOfSatisfying(ProductStockReservationRequestedEvent.class,
                event -> assertThat(event.quantity().value()).isEqualByComparingTo("2"));
    }

    @Test
    @DisplayName("H38: lowering the quantity returns only the units given back, matching the line by catalog id")
    void shouldReturnOnlyTheUnitsGivenBack() {
        order.updateProductQuantityInTask(task.getId(), productId, Quantity.of(new BigDecimal("3")), null);
        order.clearDomainEvents();
        UUID supervisor = UUID.randomUUID();

        order.updateProductQuantityInTask(task.getId(), productId, Quantity.of(new BigDecimal("1")), supervisor);

        assertThat(line.getQuantity().value()).isEqualByComparingTo("1");
        assertThat(order.domainEvents()).singleElement().isInstanceOfSatisfying(ProductStockReservationCancelledEvent.class, event -> {
            assertThat(event.quantity().value()).isEqualByComparingTo("2");
            assertThat(event.removedBy()).isEqualTo(supervisor);
        });
    }

    @Test
    @DisplayName("H38: an unchanged quantity moves no stock, an unknown line is rejected")
    void shouldIgnoreSameQuantityAndRejectUnknownLine() {
        order.updateProductQuantityInTask(task.getId(), productId, Quantity.of(new BigDecimal("1")), null);
        assertThat(order.domainEvents()).isEmpty();

        assertThatThrownBy(() -> order.updateProductQuantityInTask(task.getId(), UUID.randomUUID(), Quantity.of(new BigDecimal("2")), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("H37/H38: saving drops withdrawn lines and writes corrected quantities to the stored lines")
    void shouldSyncStoredLinesWithTheDomain() {
        UUID otherProduct = UUID.randomUUID();
        order.addProductToTask(task.getId(), otherProduct, Quantity.of(new BigDecimal("2")), Money.soles(new BigDecimal("10.00")));
        WorkOrderTaskPersistenceEntity entity = WorkOrderTaskPersistenceAssembler.toEntity(task, null);
        assertThat(entity.getProducts()).hasSize(2);

        order.removeProductFromTask(task.getId(), new com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskProductId(otherProduct));
        order.updateProductQuantityInTask(task.getId(), productId, Quantity.of(new BigDecimal("4")), null);
        WorkOrderTaskPersistenceAssembler.updateEntity(entity, task);

        assertThat(entity.getProducts()).singleElement().satisfies(stored -> {
            assertThat(stored.getProductId()).isEqualTo(productId);
            assertThat(stored.getQuantity()).isEqualByComparingTo("4");
            assertThat(stored.getTotalAmount()).isEqualByComparingTo("120.00");
        });
    }

    @Test
    @DisplayName("H36/H39: the task detail carries the catalog service name and the spare part names")
    void shouldShowCatalogNames() {
        WorkOrderTaskLabelsQueryService labels = new WorkOrderTaskLabelsQueryService() {
            @Override
            public String serviceNameOf(WorkOrderTask t) {
                return "Diagnostico electrico";
            }

            @Override
            public String productNameOf(UUID tenantId, UUID id) {
                return "Aceite 5W30 1L";
            }
        };

        WorkOrderTaskResource resource = WorkOrderTaskResourceAssembler.toResourceFromEntity(task,
                labels.serviceNameOf(task), null, id -> labels.productNameOf(null, id));

        assertThat(resource.serviceName()).isEqualTo("Diagnostico electrico");
        assertThat(resource.products()).singleElement()
                .satisfies(p -> assertThat(p.productName()).isEqualTo("Aceite 5W30 1L"));
    }
}
