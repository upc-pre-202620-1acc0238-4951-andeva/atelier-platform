package com.andeva.atelier.platform.operations.application.internal.documentservices;

import com.andeva.atelier.platform.operations.application.documentservices.WorkOrderDocumentService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerDocumentMailer;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerFleetAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryReservationAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.TenancyAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.VehicleSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderDocumentRenderer;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderDocumentRenderer.Line;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkshopProfileDto;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.ProposalStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkOrderDocumentServiceImpl implements WorkOrderDocumentService {

    private static final ZoneId WORKSHOP_ZONE = ZoneId.of("America/Lima");
    private static final int QUOTATION_VALIDITY_DAYS = 7;

    private final WorkOrderRepository workOrderRepository;
    private final CustomerFleetAclService customerFleetAclService;
    private final TenancyAclService tenancyAclService;
    private final InventoryReservationAclService inventoryAclService;
    private final WorkOrderDocumentRenderer renderer;
    private final CustomerDocumentMailer mailer;

    public WorkOrderDocumentServiceImpl(
            WorkOrderRepository workOrderRepository,
            CustomerFleetAclService customerFleetAclService,
            TenancyAclService tenancyAclService,
            InventoryReservationAclService inventoryAclService,
            WorkOrderDocumentRenderer renderer,
            CustomerDocumentMailer mailer
    ) {
        this.workOrderRepository = Objects.requireNonNull(workOrderRepository);
        this.customerFleetAclService = Objects.requireNonNull(customerFleetAclService);
        this.tenancyAclService = Objects.requireNonNull(tenancyAclService);
        this.inventoryAclService = Objects.requireNonNull(inventoryAclService);
        this.renderer = Objects.requireNonNull(renderer);
        this.mailer = Objects.requireNonNull(mailer);
    }

    @Override
    public Result<byte[], ApplicationError> quotationPdf(TenantId tenantId, WorkOrderId workOrderId) {
        Optional<WorkOrder> order = findOrder(tenantId, workOrderId);
        if (order.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + workOrderId.value() + " was not found"));
        }
        return Result.success(renderer.renderQuotation(quotationOf(order.get())));
    }

    @Override
    public Result<String, ApplicationError> sendQuotation(TenantId tenantId, WorkOrderId workOrderId) {
        Optional<WorkOrder> order = findOrder(tenantId, workOrderId);
        if (order.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + workOrderId.value() + " was not found"));
        }
        Optional<CustomerSummaryDto> customer = customerFleetAclService.fetchCustomerDetails(order.get().getCustomerId().value());
        String email = customer.map(CustomerSummaryDto::email).filter(e -> !e.isBlank()).orElse(null);
        if (email == null) {
            return Result.failure(ApplicationError.unprocessableEntity("The customer has no contact email to send the quotation to"));
        }

        WorkOrderDocumentRenderer.QuotationDocument quotation = quotationOf(order.get());
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2>Proforma de mantenimiento #%d</h2>
                    <p>Estimado(a) <strong>%s</strong>, %s le envía la cotización de los trabajos para su vehículo %s (%s).</p>
                    <p>Total: <strong>%s %s</strong> (IGV incluido). Válida hasta el %s.</p>
                    <p style="color: #64748b; font-size: 14px;">El detalle de mano de obra y repuestos está en el PDF adjunto.</p>
                </div>
                """.formatted(quotation.workOrderNumber(), quotation.customerName(), quotation.workshop().name(),
                quotation.vehicle(), quotation.plate(), quotation.currency(), quotation.total().toPlainString(), quotation.validUntil());
        boolean sent = mailer.send(email, "Proforma #" + quotation.workOrderNumber() + " — " + quotation.workshop().name(), html,
                "proforma-" + quotation.workOrderNumber() + ".pdf", renderer.renderQuotation(quotation));
        if (!sent) {
            return Result.failure(ApplicationError.internalError("The quotation email could not be delivered"));
        }
        return Result.success(email);
    }

    @Override
    public Result<String, ApplicationError> notifyProposal(TenantId tenantId, WorkOrderId workOrderId, UUID proposalId) {
        Optional<WorkOrder> order = findOrder(tenantId, workOrderId);
        if (order.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + workOrderId.value() + " was not found"));
        }
        Optional<TaskProposal> proposal = order.get().getProposals().stream().filter(p -> p.getId().equals(proposalId)).findFirst();
        if (proposal.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Proposal with identifier " + proposalId + " was not found"));
        }
        if (proposal.get().getStatus() != ProposalStatus.PENDING_REVIEW) {
            return Result.failure(ApplicationError.unprocessableEntity("Only proposals pending review can be sent to the customer"));
        }
        String email = customerOf(order.get()).map(CustomerSummaryDto::email).filter(e -> !e.isBlank()).orElse(null);
        if (email == null) {
            return Result.failure(ApplicationError.unprocessableEntity("The customer has no contact email to send the proposal to"));
        }
        VehicleSummaryDto vehicle = vehicleOf(order.get());
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2>Trabajo adicional recomendado</h2>
                    <p>Durante el servicio de su vehículo %s (%s) nuestro técnico detectó:</p>
                    <p><strong>%s</strong> (severidad: %s)</p>
                    <p><img src="%s" alt="Evidencia fotográfica" style="max-width: 100%%; border-radius: 6px;"/></p>
                    <p>Comuníquese con %s para autorizar o rechazar este trabajo.</p>
                </div>
                """.formatted(describe(vehicle), vehicle != null ? vehicle.plate() : "-", proposal.get().getDescription(),
                proposal.get().getSeverity(), proposal.get().getImageUrl() != null ? proposal.get().getImageUrl().value() : "",
                workshopOf(order.get()).name());
        if (!mailer.sendWithoutAttachment(email, "Trabajo adicional recomendado — orden #" + order.get().getInternalNumber().sequence(), html)) {
            return Result.failure(ApplicationError.internalError("The proposal email could not be delivered"));
        }
        return Result.success(email);
    }

    @Override
    public Result<byte[], ApplicationError> exitPassPdf(TenantId tenantId, WorkOrderId workOrderId) {
        Optional<WorkOrder> order = findOrder(tenantId, workOrderId);
        if (order.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + workOrderId.value() + " was not found"));
        }
        WorkOrder workOrder = order.get();
        // The vehicle leaves the workshop's custody only once the order is fully paid
        if (workOrder.getStatus() != WorkOrderStatus.PAID) {
            return Result.failure(ApplicationError.unprocessableEntity(
                    "The exit pass is issued only for paid orders; current status: " + workOrder.getStatus()));
        }
        VehicleSummaryDto vehicle = vehicleOf(workOrder);
        return Result.success(renderer.renderExitPass(new WorkOrderDocumentRenderer.ExitPassDocument(
                workshopOf(workOrder),
                workOrder.getInternalNumber().sequence(),
                customerOf(workOrder).map(CustomerSummaryDto::displayName).orElse("Cliente"),
                describe(vehicle),
                vehicle != null ? vehicle.plate() : "-",
                workOrder.getTotalAmount().amount(),
                workOrder.getTotalAmount().currency().name(),
                Instant.now()
        )));
    }

    private Optional<WorkOrder> findOrder(TenantId tenantId, WorkOrderId workOrderId) {
        return workOrderRepository.findById(workOrderId).filter(order -> order.getTenantId().equals(tenantId));
    }

    private WorkOrderDocumentRenderer.QuotationDocument quotationOf(WorkOrder order) {
        List<Line> lines = new ArrayList<>();
        for (WorkOrderTask task : order.getTasks()) {
            if (task.getStatus() == WorkOrderTaskStatus.CANCELLED) {
                continue;
            }
            lines.add(new Line("Mano de obra", task.getDescription(), BigDecimal.ONE, task.getPrice().amount(), task.getPrice().amount()));
            task.getConsumedProducts().forEach(product -> lines.add(new Line(
                    "Repuesto",
                    inventoryAclService.fetchItemDetails(order.getTenantId().value(), product.getProductId())
                            .map(item -> item.name() + " (" + item.sku() + ")")
                            .orElse("Repuesto " + product.getProductId()),
                    product.getQuantity().value(),
                    product.getUnitPrice().amount(),
                    product.getTotalAmount().amount())));
        }
        LocalDate today = LocalDate.now(WORKSHOP_ZONE);
        Optional<CustomerSummaryDto> customer = customerOf(order);
        VehicleSummaryDto vehicle = vehicleOf(order);
        return new WorkOrderDocumentRenderer.QuotationDocument(
                workshopOf(order),
                order.getInternalNumber().sequence(),
                customer.map(CustomerSummaryDto::displayName).orElse("Cliente"),
                customer.map(CustomerSummaryDto::taxId).orElse("-"),
                describe(vehicle),
                vehicle != null ? vehicle.plate() : "-",
                order.getMileageIn() != null ? order.getMileageIn().value() : 0,
                order.getDiagnosticSummary() != null ? order.getDiagnosticSummary().value() : "",
                lines,
                order.getSubtotal().amount(),
                order.getTax().amount(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency().name(),
                today,
                today.plusDays(QUOTATION_VALIDITY_DAYS)
        );
    }

    private WorkshopProfileDto workshopOf(WorkOrder order) {
        return tenancyAclService.fetchWorkshopProfile(order.getTenantId().value())
                .orElse(new WorkshopProfileDto("Taller", "Taller", "-"));
    }

    private Optional<CustomerSummaryDto> customerOf(WorkOrder order) {
        return order.getCustomerId() == null ? Optional.empty()
                : customerFleetAclService.fetchCustomerDetails(order.getCustomerId().value());
    }

    private VehicleSummaryDto vehicleOf(WorkOrder order) {
        return customerFleetAclService.fetchVehicleDetails(order.getVehicleId().value()).orElse(null);
    }

    private static String describe(VehicleSummaryDto vehicle) {
        if (vehicle == null) {
            return "Vehículo";
        }
        return vehicle.brand() + " " + vehicle.model() + (vehicle.year() != null ? " " + vehicle.year() : "");
    }
}
