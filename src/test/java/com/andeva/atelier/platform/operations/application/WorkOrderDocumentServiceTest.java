package com.andeva.atelier.platform.operations.application;

import com.andeva.atelier.platform.operations.application.internal.documentservices.WorkOrderDocumentServiceImpl;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerDocumentMailer;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerFleetAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryReservationAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.TenancyAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.VehicleSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkshopProfileDto;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.operations.infrastructure.external.documents.OpenPdfWorkOrderDocumentRenderer;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Quotation PDF, quotation email and exit pass (US24, US39)")
class WorkOrderDocumentServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;
    @Mock
    private CustomerFleetAclService customerFleetAclService;
    @Mock
    private TenancyAclService tenancyAclService;
    @Mock
    private InventoryReservationAclService inventoryAclService;
    @Mock
    private CustomerDocumentMailer mailer;

    private WorkOrderDocumentServiceImpl service;
    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private WorkOrder order;

    @BeforeEach
    void setUp() {
        service = new WorkOrderDocumentServiceImpl(workOrderRepository, customerFleetAclService, tenancyAclService,
                inventoryAclService, new OpenPdfWorkOrderDocumentRenderer(), mailer);
        order = WorkOrder.create(tenantId, BranchId.of(UUID.randomUUID()), null, VehicleId.of(UUID.randomUUID()),
                CustomerId.of(UUID.randomUUID()), WorkOrderNumber.of(12), Mileage.of(48000), DiagnosticSummary.of("Ruido en frenos"));
        order.addTask(ServiceId.of(UUID.randomUUID()), null, "Cambio de pastillas", Money.soles(new BigDecimal("120.00")), LaborHours.of(BigDecimal.ONE));
        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(tenancyAclService.fetchWorkshopProfile(tenantId.value()))
                .thenReturn(Optional.of(new WorkshopProfileDto("Taller Veloz", "TALLER VELOZ S.A.C.", "20601234567")));
        when(customerFleetAclService.fetchCustomerDetails(order.getCustomerId().value()))
                .thenReturn(Optional.of(new CustomerSummaryDto(order.getCustomerId().value(), "Ana Rojas", "41234567", "ana@correo.pe", null, "INDIVIDUAL")));
    }

    @Test
    @DisplayName("Renders the quotation as a PDF and emails it to the customer")
    void shouldRenderAndSendQuotation() {
        when(customerFleetAclService.fetchVehicleDetails(order.getVehicleId().value()))
                .thenReturn(Optional.of(new VehicleSummaryDto(order.getVehicleId().value(), "ABC-123", "VIN", "Kia", "Rio", 2021, null)));
        when(mailer.send(eq("ana@correo.pe"), any(), any(), eq("proforma-12.pdf"), any())).thenReturn(true);

        byte[] pdf = service.quotationPdf(tenantId, order.getId()).getOrThrow();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");

        assertThat(service.sendQuotation(tenantId, order.getId()).getOrThrow()).isEqualTo("ana@correo.pe");
        verify(mailer).send(eq("ana@correo.pe"), any(), any(), eq("proforma-12.pdf"), any());
    }

    @Test
    @DisplayName("Refuses the exit pass while the order is not paid, and hides orders of other workshops")
    void shouldGuardExitPass() {
        var result = service.exitPassPdf(tenantId, order.getId());
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("UNPROCESSABLE_ENTITY");

        var foreign = service.quotationPdf(TenantId.of(UUID.randomUUID()), order.getId());
        assertThat(foreign.getError().code()).isEqualTo("NOT_FOUND");
    }
}
