package com.andeva.atelier.platform.invoicing.application.acl;

import com.andeva.atelier.platform.invoicing.application.commandservices.ElectronicVoucherCommandService;
import com.andeva.atelier.platform.invoicing.application.queryservices.ElectronicVoucherQueryService;
import com.andeva.atelier.platform.invoicing.application.queryservices.SeriesConfigurationQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.VoucherLineCommandDto;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.interfaces.acl.InvoicingContextFacade;
import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.GenerateVoucherFromWorkOrderCommandDto;
import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.VoucherGenerationResultDto;
import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.VoucherSummaryDto;
import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.WorkOrderItemBillingDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.MeasurementUnit;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of Inbound ACL Facade (Open Host Service) enabling
 * Workshop Operations (MRO) to bill work orders and verify financial settlement.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class InvoicingContextFacadeImpl implements InvoicingContextFacade {

    private final ElectronicVoucherCommandService commandService;
    private final ElectronicVoucherQueryService queryService;
    private final SeriesConfigurationQueryService seriesQueryService;

    public InvoicingContextFacadeImpl(
            ElectronicVoucherCommandService commandService,
            ElectronicVoucherQueryService queryService,
            SeriesConfigurationQueryService seriesQueryService
    ) {
        this.commandService = Objects.requireNonNull(commandService, "Command service cannot be null");
        this.queryService = Objects.requireNonNull(queryService, "Query service cannot be null");
        this.seriesQueryService = seriesQueryService;
    }

    @Override
    public VoucherGenerationResultDto generateVoucherFromWorkOrder(GenerateVoucherFromWorkOrderCommandDto command) {
        Objects.requireNonNull(command, "GenerateVoucherFromWorkOrderCommandDto cannot be null");

        TenantId tenantId = TenantId.of(command.tenantId());
        BranchId branchId = BranchId.of(command.branchId());
        CustomerId customerId = CustomerId.of(command.customerId());
        WorkOrderId workOrderId = WorkOrderId.of(command.workOrderId());

        VoucherType voucherType = VoucherType.valueOf(command.voucherType().toUpperCase());
        Currency currency = Currency.valueOf(command.currency().toUpperCase());

        CustomerFiscalInfo customerFiscalInfo;
        if (command.customerTaxId() != null && !command.customerTaxId().isBlank()) {
            TaxId taxId = TaxId.of(command.customerTaxId());
            DocumentType docType = taxId.type().name().equals("RUC") ? DocumentType.RUC : DocumentType.DNI;
            customerFiscalInfo = CustomerFiscalInfo.of(
                    taxId,
                    command.customerLegalName(),
                    command.customerFiscalAddress(),
                    docType
            );
        } else {
            customerFiscalInfo = CustomerFiscalInfo.anonymous();
        }

        List<VoucherLineCommandDto> lines = new ArrayList<>();
        for (WorkOrderItemBillingDto item : command.items()) {
            VoucherItemType itemType = VoucherItemType.valueOf(item.itemType().toUpperCase());
            Quantity qty = Quantity.of(item.quantity(), MeasurementUnit.UNIT);
            Money unitPrice = Money.of(item.unitPriceWithIgv(), currency);

            lines.add(new VoucherLineCommandDto(
                    Optional.ofNullable(item.itemId()),
                    itemType,
                    item.description(),
                    qty,
                    unitPrice
            ));
        }

        VoucherSerie defaultSerie = VoucherSerie.of(voucherType == VoucherType.FACTURA ? "F001" : "B001");
        VoucherSerie serie = defaultSerie;
        if (seriesQueryService != null) {
            serie = seriesQueryService.getActiveSeriesByBranchAndType(tenantId, branchId, voucherType)
                    .map(SeriesConfiguration::getSerie)
                    .orElse(defaultSerie);
        }

        IssueElectronicVoucherCommand issueCmd = new IssueElectronicVoucherCommand(
                tenantId,
                branchId,
                customerId,
                Optional.of(workOrderId),
                voucherType,
                serie,
                customerFiscalInfo,
                currency,
                lines
        );

        ElectronicVoucher voucher = commandService.handle(issueCmd);

        return new VoucherGenerationResultDto(
                voucher.getId().value(),
                voucher.getSerie().value() + "-" + voucher.getNumber().format(),
                voucher.getTotalAmount().amount(),
                voucher.getStatus().name(),
                voucher.getDigitalReceiptUrls().pdfUrl()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherSummaryDto> getVouchersByWorkOrderId(UUID workOrderId) {
        Objects.requireNonNull(workOrderId, "WorkOrderId cannot be null");
        List<ElectronicVoucher> vouchers = queryService.getVouchersByWorkOrderId(WorkOrderId.of(workOrderId));

        return vouchers.stream()
                .map(v -> new VoucherSummaryDto(
                        v.getId().value(),
                        v.getSerie().value() + "-" + v.getNumber().format(),
                        v.getVoucherType().name(),
                        v.getTotalAmount().amount(),
                        v.getStatus().name(),
                        v.isFullyPaid()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWorkOrderFullySettled(UUID workOrderId) {
        Objects.requireNonNull(workOrderId, "WorkOrderId cannot be null");
        List<ElectronicVoucher> vouchers = queryService.getVouchersByWorkOrderId(WorkOrderId.of(workOrderId));

        if (vouchers.isEmpty()) {
            return false;
        }

        return vouchers.stream().allMatch(v ->
                v.isFullyPaid() &&
                        v.getStatus() != VoucherStatus.VOIDED &&
                        v.getStatus() != VoucherStatus.REJECTED_SUNAT
        );
    }
}
