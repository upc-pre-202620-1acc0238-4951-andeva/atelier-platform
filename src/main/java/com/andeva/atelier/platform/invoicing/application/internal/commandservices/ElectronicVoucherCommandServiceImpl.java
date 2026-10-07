package com.andeva.atelier.platform.invoicing.application.internal.commandservices;

import com.andeva.atelier.platform.invoicing.application.commandservices.ElectronicVoucherCommandService;
import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CustomerFiscalValidationAclService;
import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.NubefactPseFiscalGateway;
import com.andeva.atelier.platform.invoicing.domain.exceptions.CreditNoteReferenceNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.SeriesNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueCreditNoteCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.ProcessSunatResponseCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.VoidElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.VoucherLineCommandDto;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CreditNoteReference;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.invoicing.domain.repositories.SeriesConfigurationRepository;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.invoicing.domain.services.SeriesCorrelativeService;
import com.andeva.atelier.platform.invoicing.domain.services.VoucherValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Transactional Command Service implementing electronic voucher lifecycle management,
 * tax calculation, sequential correlative reservation, and Nubefact/SUNAT dispatch.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class ElectronicVoucherCommandServiceImpl implements ElectronicVoucherCommandService {

    private static final Logger log = LoggerFactory.getLogger(ElectronicVoucherCommandServiceImpl.class);

    private final ElectronicVoucherRepository voucherRepository;
    private final SeriesConfigurationRepository seriesRepository;
    private final SeriesCorrelativeService correlativeService;
    private final PeruvianTaxCalculationEngine taxEngine;
    private final VoucherValidationService validationService;
    private final NubefactPseFiscalGateway nubefactGateway;
    private final CustomerFiscalValidationAclService customerFiscalAcl;

    public ElectronicVoucherCommandServiceImpl(
            ElectronicVoucherRepository voucherRepository,
            SeriesConfigurationRepository seriesRepository,
            SeriesCorrelativeService correlativeService,
            PeruvianTaxCalculationEngine taxEngine,
            VoucherValidationService validationService,
            NubefactPseFiscalGateway nubefactGateway,
            CustomerFiscalValidationAclService customerFiscalAcl
    ) {
        this.voucherRepository = Objects.requireNonNull(voucherRepository, "Voucher repository cannot be null");
        this.seriesRepository = Objects.requireNonNull(seriesRepository, "Series repository cannot be null");
        this.correlativeService = Objects.requireNonNull(correlativeService, "Correlative service cannot be null");
        this.taxEngine = Objects.requireNonNull(taxEngine, "Tax calculation engine cannot be null");
        this.validationService = Objects.requireNonNull(validationService, "Voucher validation service cannot be null");
        this.nubefactGateway = Objects.requireNonNull(nubefactGateway, "Nubefact PSE gateway cannot be null");
        this.customerFiscalAcl = Objects.requireNonNull(customerFiscalAcl, "Customer fiscal ACL cannot be null");
    }

    @Override
    public ElectronicVoucher handle(IssueElectronicVoucherCommand command) {
        Objects.requireNonNull(command, "IssueElectronicVoucherCommand cannot be null");

        // 1. Resolve customer fiscal info if omitted or incomplete
        CustomerFiscalInfo fiscalInfo = command.customerInfo();
        if (fiscalInfo == null || (fiscalInfo.taxId() == null && !fiscalInfo.legalName().equals("CLIENTES VARIOS"))) {
            fiscalInfo = customerFiscalAcl.getCustomerFiscalData(command.customerId().value())
                    .orElse(fiscalInfo != null ? fiscalInfo : CustomerFiscalInfo.anonymous());
        }

        // 2. Find active series configuration for branch and voucher type
        SeriesConfiguration seriesConfig = seriesRepository
                .findByTenantIdAndBranchIdAndVoucherTypeAndActive(command.tenantId(), command.branchId(), command.type())
                .orElseThrow(() -> new SeriesNotFoundException(command.branchId(), command.type()));

        correlativeService.validateSeriesFormat(seriesConfig.getSerie(), command.type());

        // 3. Reserve and allocate atomic correlative
        VoucherNumber correlative = correlativeService.allocateNext(seriesConfig);
        seriesRepository.save(seriesConfig);

        // 4. Transform line item commands into child entities
        VoucherId voucherId = VoucherId.generate();
        List<VoucherLine> lines = new ArrayList<>();
        for (VoucherLineCommandDto lineDto : command.lines()) {
            VoucherLine line = VoucherLine.create(
                    null,
                    voucherId,
                    lineDto.itemId().orElse(null),
                    lineDto.itemType(),
                    lineDto.description(),
                    lineDto.quantity(),
                    lineDto.unitPriceWithIgv()
            );
            lines.add(line);
        }

        // 5. Segregate tax calculations
        TaxCalculation taxCalc = taxEngine.calculateFromLines(lines, command.currency());

        // 6. Validate fiscal data
        validationService.validateFiscalData(command.type(), fiscalInfo, taxCalc.totalAmount());

        // 7. Instantiate electronic voucher aggregate
        ElectronicVoucher voucher = ElectronicVoucher.issue(
                command.tenantId(),
                command.branchId(),
                command.customerId(),
                command.workOrderId().orElse(null),
                command.type(),
                seriesConfig.getSerie(),
                correlative,
                fiscalInfo,
                command.currency(),
                taxCalc,
                lines
        );

        voucher = voucherRepository.save(voucher);

        // 8. Attempt synchronous dispatch to PSE/SUNAT
        try {
            NubefactPseFiscalGateway.NubefactDispatchResult dispatch = nubefactGateway.dispatchVoucher(voucher);
            if (dispatch.isAccepted()) {
                voucher.markAcceptedBySunat(dispatch.digitalSignatureHash(), dispatch.description(), dispatch.urls());
            } else {
                voucher.markRejectedBySunat(dispatch.responseCode(), dispatch.description());
            }
            voucher = voucherRepository.save(voucher);
        } catch (Exception e) {
            log.warn("Synchronous Nubefact dispatch failed for voucher {}-{}. Retaining ISSUED for Outbox retry: {}",
                    voucher.getSerie().value(), voucher.getNumber().format(), e.getMessage());
        }

        return voucher;
    }

    @Override
    public ElectronicVoucher handle(IssueCreditNoteCommand command) {
        Objects.requireNonNull(command, "IssueCreditNoteCommand cannot be null");

        // 1. Locate original voucher
        ElectronicVoucher original = voucherRepository.findById(command.referenceVoucherId())
                .orElseThrow(() -> new CreditNoteReferenceNotFoundException(command.referenceVoucherId()));

        if (!original.getTenantId().equals(command.tenantId()) || !original.getBranchId().equals(command.branchId())) {
            throw new com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException(
                    "ERR_CROSS_TENANT_ACCESS",
                    "Cannot reference a voucher belonging to another tenant or branch."
            );
        }

        // 2. Validate credit note referencing rules
        validationService.validateCreditNoteReference(original, command.reason());

        // 3. Find active credit note series configuration
        SeriesConfiguration seriesConfig = seriesRepository
                .findByTenantIdAndBranchIdAndVoucherTypeAndActive(command.tenantId(), command.branchId(), VoucherType.NOTA_CREDITO)
                .orElseThrow(() -> new SeriesNotFoundException(command.branchId(), VoucherType.NOTA_CREDITO));

        correlativeService.validateSeriesFormat(seriesConfig.getSerie(), VoucherType.NOTA_CREDITO);

        // 4. Reserve correlative
        VoucherNumber correlative = correlativeService.allocateNext(seriesConfig);
        seriesRepository.save(seriesConfig);

        // 5. Build credit note lines and calculate tax
        VoucherId creditNoteId = VoucherId.generate();
        List<VoucherLine> lines = new ArrayList<>();
        for (VoucherLineCommandDto lineDto : command.lines()) {
            VoucherLine line = VoucherLine.create(
                    null,
                    creditNoteId,
                    lineDto.itemId().orElse(null),
                    lineDto.itemType(),
                    lineDto.description(),
                    lineDto.quantity(),
                    lineDto.unitPriceWithIgv()
            );
            lines.add(line);
        }
        TaxCalculation taxCalc = taxEngine.calculateFromLines(lines, original.getCurrency());

        // 6. Build reference
        CreditNoteReference reference = CreditNoteReference.of(
                original.getId(),
                original.getSerie(),
                original.getNumber(),
                command.reason(),
                command.reasonDescription()
        );

        // 7. Issue credit note aggregate
        ElectronicVoucher creditNote = ElectronicVoucher.issueCreditNote(
                command.tenantId(),
                command.branchId(),
                original.getCustomerId(),
                original.getWorkOrderId().orElse(null),
                seriesConfig.getSerie(),
                correlative,
                original.getCustomerFiscalInfo(),
                original.getCurrency(),
                taxCalc,
                reference,
                lines
        );

        creditNote = voucherRepository.save(creditNote);

        // 8. Attempt synchronous dispatch
        try {
            NubefactPseFiscalGateway.NubefactDispatchResult dispatch = nubefactGateway.dispatchCreditNote(creditNote);
            if (dispatch.isAccepted()) {
                creditNote.markAcceptedBySunat(dispatch.digitalSignatureHash(), dispatch.description(), dispatch.urls());
            } else {
                creditNote.markRejectedBySunat(dispatch.responseCode(), dispatch.description());
            }
            creditNote = voucherRepository.save(creditNote);
        } catch (Exception e) {
            log.warn("Synchronous Nubefact dispatch failed for Credit Note {}-{}. Retaining ISSUED for Outbox retry: {}",
                    creditNote.getSerie().value(), creditNote.getNumber().format(), e.getMessage());
        }

        return creditNote;
    }

    @Override
    public ElectronicVoucher handle(VoidElectronicVoucherCommand command) {
        Objects.requireNonNull(command, "VoidElectronicVoucherCommand cannot be null");

        ElectronicVoucher voucher = voucherRepository.findById(command.voucherId())
                .orElseThrow(() -> new VoucherNotFoundException(command.voucherId()));

        if (command.tenantId() != null && !voucher.getTenantId().equals(command.tenantId())) {
            throw new com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException(
                    "ERR_CROSS_TENANT_ACCESS",
                    "Cannot void a voucher belonging to another tenant."
            );
        }

        if (voucher.getStatus() != com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus.ACCEPTED_SUNAT) {
            throw new com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException(
                    "ERR_INVALID_VOID_STATUS",
                    "Only ACCEPTED vouchers can be voided via comunicación de baja."
            );
        }

        if (voucher.getIssuedAt().plus(7, java.time.temporal.ChronoUnit.DAYS).isBefore(Instant.now())) {
            throw new com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException(
                    "ERR_VOID_WINDOW_EXPIRED",
                    "SUNAT voiding window (7 calendar days) has expired."
            );
        }

        voucher.voidVoucher(command.reason(), Instant.now());
        voucher = voucherRepository.save(voucher);

        try {
            nubefactGateway.voidVoucher(voucher, command.reason());
        } catch (Exception e) {
            log.warn("Failed to communicate voiding to Nubefact for voucher {}: {}",
                    voucher.getId().value(), e.getMessage());
        }

        return voucher;
    }

    @Override
    public ElectronicVoucher handle(ProcessSunatResponseCommand command) {
        Objects.requireNonNull(command, "ProcessSunatResponseCommand cannot be null");

        ElectronicVoucher voucher = voucherRepository.findById(command.voucherId())
                .orElseThrow(() -> new VoucherNotFoundException(command.voucherId()));

        if (command.accepted()) {
            voucher.markAcceptedBySunat(command.digitalSignatureHash(), command.description(), command.urls());
        } else {
            voucher.markRejectedBySunat(command.responseCode(), command.description());
        }

        return voucherRepository.save(voucher);
    }
}
