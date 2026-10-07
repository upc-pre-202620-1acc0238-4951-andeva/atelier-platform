package com.andeva.atelier.platform.invoicing.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.invoicing.interfaces.acl.InvoicingContextFacade;
import com.andeva.atelier.platform.invoicing.application.commandservices.ElectronicVoucherCommandService;
import com.andeva.atelier.platform.invoicing.application.queryservices.ElectronicVoucherQueryService;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueCreditNoteCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.VoidElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.VoucherLineCommandDto;
import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherByIdQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVouchersByTenantQuery;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers.ElectronicVoucherResourceAssembler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.IssueCreditNoteRequest;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.IssueVoucherRequest;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.VoidVoucherRequest;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.VoucherLineRequest;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.ElectronicVoucherResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxIdType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller governing official electronic tax voucher lifecycle, SUNAT dispatch,
 * credit note issuance, fiscal query services, and legal voiding.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/invoicing/vouchers")
@Tag(name = "Electronic Vouchers", description = "Endpoints for managing SUNAT electronic invoices, bills, credit notes, and voiding")
public class ElectronicVouchersController {

    private final ElectronicVoucherCommandService voucherCommandService;
    private final ElectronicVoucherQueryService voucherQueryService;
    private final InvoicingContextFacade invoicingFacade;
    private final ElectronicVoucherResourceAssembler voucherAssembler;

    public ElectronicVouchersController(
            ElectronicVoucherCommandService voucherCommandService,
            ElectronicVoucherQueryService voucherQueryService,
            InvoicingContextFacade invoicingFacade,
            ElectronicVoucherResourceAssembler voucherAssembler
    ) {
        this.voucherCommandService = Objects.requireNonNull(voucherCommandService, "ElectronicVoucherCommandService cannot be null");
        this.voucherQueryService = Objects.requireNonNull(voucherQueryService, "ElectronicVoucherQueryService cannot be null");
        this.invoicingFacade = Objects.requireNonNull(invoicingFacade, "InvoicingContextFacade cannot be null");
        this.voucherAssembler = Objects.requireNonNull(voucherAssembler, "ElectronicVoucherResourceAssembler cannot be null");
    }

    @Operation(summary = "Issue electronic voucher (Factura 01 or Boleta 03)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Voucher successfully issued and dispatched to SUNAT"),
            @ApiResponse(responseCode = "400", description = "Bad request or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Series not found"),
            @ApiResponse(responseCode = "409", description = "Correlative exhausted"),
            @ApiResponse(responseCode = "502", description = "SUNAT integration failure")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('invoicing:invoices:issue_sunat') or hasAnyRole('ROLE_CASHIER', 'ROLE_WORKSHOP_ADMINISTRATOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<ElectronicVoucherResource> issueVoucher(
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody IssueVoucherRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        VoucherType type = "01".equals(request.voucherType()) || "FACTURA".equalsIgnoreCase(request.voucherType())
                ? VoucherType.FACTURA
                : VoucherType.BOLETA;

        Currency currency = Currency.valueOf(request.currency().toUpperCase().trim());

        TaxId taxId = null;
        if (request.customerInfo().taxId() != null && !"-".equals(request.customerInfo().taxId()) && !request.customerInfo().taxId().isBlank()) {
            TaxIdType idType = "6".equals(request.customerInfo().documentType()) || "RUC".equalsIgnoreCase(request.customerInfo().documentType())
                    ? TaxIdType.RUC
                    : TaxIdType.DNI;
            taxId = new TaxId(request.customerInfo().taxId(), idType);
        }

        DocumentType docType = "6".equals(request.customerInfo().documentType()) ? DocumentType.RUC : DocumentType.valueOf(request.customerInfo().documentType());
        CustomerFiscalInfo customerInfo = CustomerFiscalInfo.of(
                taxId,
                request.customerInfo().legalName(),
                request.customerInfo().fiscalAddress(),
                docType
        );

        List<VoucherLineCommandDto> lineDtos = new ArrayList<>();
        for (VoucherLineRequest lineReq : request.lines()) {
            lineDtos.add(new VoucherLineCommandDto(
                    Optional.ofNullable(lineReq.itemId()),
                    VoucherItemType.valueOf(lineReq.itemType().toUpperCase().trim()),
                    lineReq.description(),
                    Quantity.of(lineReq.quantity(), com.andeva.atelier.platform.shared.domain.model.valueobjects.MeasurementUnit.UNIT),
                    Money.of(lineReq.unitPriceWithIgv(), currency)
            ));
        }

        IssueElectronicVoucherCommand command = new IssueElectronicVoucherCommand(
                tenantId,
                BranchId.of(request.branchId()),
                CustomerId.of(request.customerId()),
                Optional.ofNullable(request.workOrderId()).map(WorkOrderId::of),
                type,
                VoucherSerie.of(request.serie()),
                customerInfo,
                currency,
                lineDtos
        );

        ElectronicVoucher voucher = voucherCommandService.handle(command);
        ElectronicVoucherResource resource = voucherAssembler.toResource(voucher);

        return ResponseEntity.created(URI.create("/api/v1/invoicing/vouchers/" + voucher.getId().value()))
                .body(resource);
    }

    @Operation(summary = "Issue electronic credit note referencing an accepted voucher")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Credit Note successfully issued and dispatched"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Reference voucher or series not found")
    })
    @PostMapping("/credit-notes")
    @PreAuthorize("hasAuthority('invoicing:invoices:issue_sunat') or hasAnyRole('ROLE_WORKSHOP_ADMINISTRATOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<ElectronicVoucherResource> issueCreditNote(
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody IssueCreditNoteRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        CreditNoteReason reason = switch (request.reason()) {
            case "01" -> CreditNoteReason.ANULACION_DE_LA_OPERACION;
            case "02" -> CreditNoteReason.ANULACION_POR_ERROR_EN_EL_RUC;
            case "03" -> CreditNoteReason.CORRECCION_POR_ERROR_EN_LA_DESCRIPCION;
            case "04" -> CreditNoteReason.DESCUENTO_GLOBAL;
            case "06" -> CreditNoteReason.DEVOLUCION_TOTAL;
            default -> CreditNoteReason.valueOf(request.reason());
        };

        ElectronicVoucher refVoucher = voucherQueryService.handle(new GetVoucherByIdQuery(VoucherId.of(request.referenceVoucherId())))
                .orElse(null);
        Currency currency = refVoucher != null ? refVoucher.getCurrency() : Currency.PEN;

        List<VoucherLineCommandDto> lineDtos = new ArrayList<>();
        for (VoucherLineRequest lineReq : request.lines()) {
            lineDtos.add(new VoucherLineCommandDto(
                    Optional.ofNullable(lineReq.itemId()),
                    VoucherItemType.valueOf(lineReq.itemType().toUpperCase().trim()),
                    lineReq.description(),
                    Quantity.of(lineReq.quantity(), com.andeva.atelier.platform.shared.domain.model.valueobjects.MeasurementUnit.UNIT),
                    Money.of(lineReq.unitPriceWithIgv(), currency)
            ));
        }

        IssueCreditNoteCommand command = new IssueCreditNoteCommand(
                tenantId,
                BranchId.of(request.branchId()),
                CustomerId.of(request.customerId()),
                VoucherId.of(request.referenceVoucherId()),
                VoucherSerie.of(request.serie()),
                reason,
                request.reasonDescription(),
                lineDtos
        );

        ElectronicVoucher creditNote = voucherCommandService.handle(command);
        ElectronicVoucherResource resource = voucherAssembler.toResource(creditNote);

        return ResponseEntity.created(URI.create("/api/v1/invoicing/vouchers/" + creditNote.getId().value()))
                .body(resource);
    }

    @Operation(summary = "Get electronic voucher by identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voucher retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Voucher not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('invoicing:invoices:read') or hasAnyRole('ROLE_CASHIER', 'ROLE_SERVICE_ADVISOR', 'ROLE_WORKSHOP_ADMINISTRATOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<ElectronicVoucherResource> getVoucherById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        ElectronicVoucher voucher = voucherQueryService.handle(new GetVoucherByIdQuery(VoucherId.of(id)))
                .orElseThrow(() -> new VoucherNotFoundException(VoucherId.of(id)));

        if (!voucher.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Voucher does not belong to the authenticated workshop");
        }

        return ResponseEntity.ok(voucherAssembler.toResource(voucher));
    }

    @Operation(summary = "List electronic vouchers for authenticated tenant with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vouchers listed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('invoicing:invoices:read') or hasAnyRole('ROLE_CASHIER', 'ROLE_SERVICE_ADVISOR', 'ROLE_WORKSHOP_ADMINISTRATOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<List<ElectronicVoucherResource>> getVouchers(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        VoucherType type = null;
        if (voucherType != null && !voucherType.isBlank()) {
            type = "01".equals(voucherType) ? VoucherType.FACTURA :
                    "03".equals(voucherType) ? VoucherType.BOLETA :
                            "07".equals(voucherType) ? VoucherType.NOTA_CREDITO :
                                    VoucherType.valueOf(voucherType.toUpperCase().trim());
        }

        VoucherStatus voucherStatus = (status != null && !status.isBlank())
                ? VoucherStatus.valueOf(status.toUpperCase().trim())
                : null;

        LocalDate startDate = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate endDate = to != null ? to : LocalDate.now();

        GetVouchersByTenantQuery query = GetVouchersByTenantQuery.of(
                tenantId,
                type,
                startDate,
                endDate
        );

        List<ElectronicVoucher> vouchers = voucherQueryService.handle(query);
        List<ElectronicVoucherResource> resources = vouchers.stream()
                .map(voucherAssembler::toResource)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get electronic vouchers linked to a repair work order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vouchers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/work-order/{workOrderId}")
    @PreAuthorize("hasAuthority('invoicing:invoices:read') or hasAnyRole('ROLE_CASHIER', 'ROLE_SERVICE_ADVISOR', 'ROLE_WORKSHOP_ADMINISTRATOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<List<ElectronicVoucherResource>> getVouchersByWorkOrder(
            @PathVariable UUID workOrderId,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        List<ElectronicVoucher> vouchers = voucherQueryService.getVouchersByWorkOrderId(WorkOrderId.of(workOrderId)).stream()
                .filter(v -> v.getTenantId().equals(tenantId))
                .toList();
        List<ElectronicVoucherResource> resources = vouchers.stream()
                .map(voucherAssembler::toResource)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Void electronic voucher via comunicación de baja within 7 calendar days")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voucher voided successfully"),
            @ApiResponse(responseCode = "400", description = "Validation or window expired"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Voucher not found")
    })
    @PostMapping("/{id}/void")
    @PreAuthorize("hasAuthority('invoicing:invoices:issue_sunat') or hasAnyRole('ROLE_WORKSHOP_ADMINISTRATOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<ElectronicVoucherResource> voidVoucher(
            @PathVariable UUID id,
            @Valid @RequestBody VoidVoucherRequest request,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID headerTenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails, headerTenantId);

        VoidElectronicVoucherCommand command = new VoidElectronicVoucherCommand(
                tenantId,
                VoucherId.of(id),
                request.reason()
        );

        ElectronicVoucher voided = voucherCommandService.handle(command);
        return ResponseEntity.ok(voucherAssembler.toResource(voided));
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails, UUID headerTenantId) {
        if (userDetails != null && userDetails.getTenantId() != null) {
            if (headerTenantId != null && !userDetails.getTenantId().equals(headerTenantId)) {
                throw new AccessDeniedException("Tenant ID in header does not match authenticated user context");
            }
            return TenantId.of(userDetails.getTenantId());
        }
        if (headerTenantId != null) {
            return TenantId.of(headerTenantId);
        }
        throw new AccessDeniedException("Active tenant context is required");
    }
}
