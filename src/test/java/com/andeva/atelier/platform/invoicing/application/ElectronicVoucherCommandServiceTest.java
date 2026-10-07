package com.andeva.atelier.platform.invoicing.application;

import com.andeva.atelier.platform.invoicing.application.commandservices.ElectronicVoucherCommandService;
import com.andeva.atelier.platform.invoicing.application.internal.commandservices.ElectronicVoucherCommandServiceImpl;
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
import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.invoicing.domain.repositories.SeriesConfigurationRepository;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.invoicing.domain.services.SeriesCorrelativeService;
import com.andeva.atelier.platform.invoicing.domain.services.VoucherValidationService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ElectronicVoucherCommandServiceImpl.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("ElectronicVoucherCommandServiceImpl Tests")
class ElectronicVoucherCommandServiceTest {

    private ElectronicVoucherRepository voucherRepository;
    private SeriesConfigurationRepository seriesRepository;
    private SeriesCorrelativeService correlativeService;
    private PeruvianTaxCalculationEngine taxEngine;
    private VoucherValidationService validationService;
    private NubefactPseFiscalGateway nubefactGateway;
    private CustomerFiscalValidationAclService customerFiscalAcl;

    private ElectronicVoucherCommandService commandService;

    private final TenantId tenantId = TenantId.generate();
    private final BranchId branchId = BranchId.generate();
    private final CustomerId customerId = CustomerId.generate();
    private final WorkOrderId workOrderId = WorkOrderId.generate();

    @BeforeEach
    void setUp() {
        voucherRepository = Mockito.mock(ElectronicVoucherRepository.class);
        seriesRepository = Mockito.mock(SeriesConfigurationRepository.class);
        correlativeService = new SeriesCorrelativeService();
        taxEngine = new PeruvianTaxCalculationEngine();
        validationService = new VoucherValidationService();
        nubefactGateway = Mockito.mock(NubefactPseFiscalGateway.class);
        customerFiscalAcl = Mockito.mock(CustomerFiscalValidationAclService.class);

        commandService = new ElectronicVoucherCommandServiceImpl(
                voucherRepository,
                seriesRepository,
                correlativeService,
                taxEngine,
                validationService,
                nubefactGateway,
                customerFiscalAcl
        );

        when(voucherRepository.save(any(ElectronicVoucher.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("Should successfully issue Factura with synchronous Nubefact acceptance")
    void shouldIssueFacturaWithNubefactAcceptance() {
        SeriesConfiguration facturaSeries = SeriesConfiguration.create(
                tenantId, branchId, VoucherType.FACTURA, VoucherSerie.of("F001"), 0
        );
        when(seriesRepository.findByTenantIdAndBranchIdAndVoucherTypeAndActive(tenantId, branchId, VoucherType.FACTURA))
                .thenReturn(Optional.of(facturaSeries));

        CustomerFiscalInfo customerInfo = CustomerFiscalInfo.of(
                TaxId.ruc("20100070970"),
                "SUNAT",
                "AV GARCILASO DE LA VEGA",
                DocumentType.RUC
        );

        VoucherLineCommandDto line = new VoucherLineCommandDto(
                Optional.of(UUID.randomUUID()),
                VoucherItemType.PRODUCT,
                "Repuesto Pastillas",
                Quantity.ofUnits(1),
                Money.of(new BigDecimal("118.00"), Currency.PEN)
        );

        IssueElectronicVoucherCommand command = new IssueElectronicVoucherCommand(
                tenantId,
                branchId,
                customerId,
                Optional.of(workOrderId),
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                customerInfo,
                Currency.PEN,
                List.of(line)
        );

        DigitalReceiptUrls urls = DigitalReceiptUrls.of("http://pdf", "http://xml", "http://cdr");
        when(nubefactGateway.dispatchVoucher(any(ElectronicVoucher.class)))
                .thenReturn(new NubefactPseFiscalGateway.NubefactDispatchResult(
                        true, "0", "Aceptada por SUNAT", "hash123", urls
                ));

        ElectronicVoucher issued = commandService.handle(command);

        assertThat(issued).isNotNull();
        assertThat(issued.getStatus()).isEqualTo(VoucherStatus.ACCEPTED_SUNAT);
        assertThat(issued.getSerie().value()).isEqualTo("F001");
        assertThat(issued.getNumber().value()).isEqualTo(1);
        assertThat(issued.getDigitalReceiptUrls().pdfUrl()).isEqualTo("http://pdf");

        verify(seriesRepository).save(facturaSeries);
        verify(nubefactGateway).dispatchVoucher(any(ElectronicVoucher.class));
    }

    @Test
    @DisplayName("Should issue Boleta with anonymous profile under S/ 700 threshold")
    void shouldIssueAnonymousBoleta() {
        SeriesConfiguration boletaSeries = SeriesConfiguration.create(
                tenantId, branchId, VoucherType.BOLETA, VoucherSerie.of("B001"), 0
        );
        when(seriesRepository.findByTenantIdAndBranchIdAndVoucherTypeAndActive(tenantId, branchId, VoucherType.BOLETA))
                .thenReturn(Optional.of(boletaSeries));

        VoucherLineCommandDto line = new VoucherLineCommandDto(
                Optional.empty(),
                VoucherItemType.SERVICE,
                "Lavado de motor",
                Quantity.ofUnits(1),
                Money.of(new BigDecimal("50.00"), Currency.PEN)
        );

        IssueElectronicVoucherCommand command = new IssueElectronicVoucherCommand(
                tenantId,
                branchId,
                customerId,
                Optional.empty(),
                VoucherType.BOLETA,
                VoucherSerie.of("B001"),
                CustomerFiscalInfo.anonymous(),
                Currency.PEN,
                List.of(line)
        );

        when(nubefactGateway.dispatchVoucher(any(ElectronicVoucher.class)))
                .thenThrow(new RuntimeException("Gateway connection timeout"));

        ElectronicVoucher issued = commandService.handle(command);

        assertThat(issued).isNotNull();
        // Fallback to ISSUED for Outbox retry when gateway times out
        assertThat(issued.getStatus()).isEqualTo(VoucherStatus.ISSUED);
        assertThat(issued.getVoucherType()).isEqualTo(VoucherType.BOLETA);
        assertThat(issued.getNumber().value()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw SeriesNotFoundException when no active series configured")
    void shouldThrowWhenSeriesNotFound() {
        when(seriesRepository.findByTenantIdAndBranchIdAndVoucherTypeAndActive(tenantId, branchId, VoucherType.FACTURA))
                .thenReturn(Optional.empty());

        IssueElectronicVoucherCommand command = new IssueElectronicVoucherCommand(
                tenantId,
                branchId,
                customerId,
                Optional.empty(),
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                CustomerFiscalInfo.anonymous(),
                Currency.PEN,
                List.of()
        );

        assertThatThrownBy(() -> commandService.handle(command))
                .isInstanceOf(SeriesNotFoundException.class);
    }

    @Test
    @DisplayName("Should issue Credit Note referencing original voucher")
    void shouldIssueCreditNote() {
        ElectronicVoucher originalVoucher = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                workOrderId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(10),
                CustomerFiscalInfo.of(TaxId.ruc("20100070970"), "SUNAT", "LIMA", DocumentType.RUC),
                Currency.PEN,
                TaxCalculation.of(Money.of(new BigDecimal("100.00"), Currency.PEN), Money.of(new BigDecimal("18.00"), Currency.PEN), Money.of(new BigDecimal("118.00"), Currency.PEN)),
                List.of(taxEngine.calculateLine(null, VoucherId.generate(), Optional.empty(), VoucherItemType.PRODUCT, "Item", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );
        originalVoucher.markAcceptedBySunat("hash123", "Accepted", DigitalReceiptUrls.empty());
        VoucherId origId = originalVoucher.getId();

        when(voucherRepository.findById(origId)).thenReturn(Optional.of(originalVoucher));

        SeriesConfiguration cnSeries = SeriesConfiguration.create(
                tenantId, branchId, VoucherType.NOTA_CREDITO, VoucherSerie.of("FC01"), 0
        );
        when(seriesRepository.findByTenantIdAndBranchIdAndVoucherTypeAndActive(tenantId, branchId, VoucherType.NOTA_CREDITO))
                .thenReturn(Optional.of(cnSeries));

        VoucherLineCommandDto line = new VoucherLineCommandDto(
                Optional.empty(),
                VoucherItemType.PRODUCT,
                "Item Anulado",
                Quantity.ofUnits(1),
                Money.of(new BigDecimal("118.00"), Currency.PEN)
        );

        IssueCreditNoteCommand command = new IssueCreditNoteCommand(
                tenantId,
                branchId,
                customerId,
                origId,
                VoucherSerie.of("FC01"),
                CreditNoteReason.ANULACION_DE_LA_OPERACION,
                "Anulación total por devolución",
                List.of(line)
        );

        DigitalReceiptUrls urls = DigitalReceiptUrls.of("http://pdf", "http://xml", "http://cdr");
        when(nubefactGateway.dispatchCreditNote(any(ElectronicVoucher.class)))
                .thenReturn(new NubefactPseFiscalGateway.NubefactDispatchResult(
                        true, "0", "Nota de crédito aceptada", "hash999", urls
                ));

        ElectronicVoucher creditNote = commandService.handle(command);

        assertThat(creditNote).isNotNull();
        assertThat(creditNote.getVoucherType()).isEqualTo(VoucherType.NOTA_CREDITO);
        assertThat(creditNote.getStatus()).isEqualTo(VoucherStatus.ACCEPTED_SUNAT);
        assertThat(creditNote.getCreditNoteReference()).isPresent();
        assertThat(creditNote.getCreditNoteReference().get().referenceVoucherId()).isEqualTo(origId);
    }

    @Test
    @DisplayName("Should throw CreditNoteReferenceNotFoundException when original voucher missing")
    void shouldThrowWhenCreditNoteReferenceMissing() {
        VoucherId origId = VoucherId.generate();
        when(voucherRepository.findById(origId)).thenReturn(Optional.empty());

        IssueCreditNoteCommand command = new IssueCreditNoteCommand(
                tenantId,
                branchId,
                customerId,
                origId,
                VoucherSerie.of("FC01"),
                CreditNoteReason.ANULACION_DE_LA_OPERACION,
                "Motivo",
                List.of()
        );

        assertThatThrownBy(() -> commandService.handle(command))
                .isInstanceOf(CreditNoteReferenceNotFoundException.class);
    }

    @Test
    @DisplayName("Should void electronic voucher")
    void shouldVoidElectronicVoucher() {
        VoucherId voucherId = VoucherId.generate();
        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                workOrderId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                CustomerFiscalInfo.of(TaxId.ruc("20100070970"), "SUNAT", "LIMA", DocumentType.RUC),
                Currency.PEN,
                TaxCalculation.of(Money.of(new BigDecimal("100.00"), Currency.PEN), Money.of(new BigDecimal("18.00"), Currency.PEN), Money.of(new BigDecimal("118.00"), Currency.PEN)),
                List.of(taxEngine.calculateLine(null, voucherId, Optional.empty(), VoucherItemType.SERVICE, "Svc", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );
        voucher.markAcceptedBySunat("hash123", "Accepted", DigitalReceiptUrls.empty());

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        VoidElectronicVoucherCommand command = new VoidElectronicVoucherCommand(voucherId, "Error en concepto");
        commandService.handle(command);

        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.VOIDED);
        verify(voucherRepository).save(voucher);
        verify(nubefactGateway).voidVoucher(voucher, "Error en concepto");
    }

    @Test
    @DisplayName("Should process asynchronous SUNAT response for acceptance and rejection")
    void shouldProcessSunatResponse() {
        VoucherId voucherId = VoucherId.generate();
        ElectronicVoucher voucher = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                workOrderId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                CustomerFiscalInfo.of(TaxId.ruc("20100070970"), "SUNAT", "LIMA", DocumentType.RUC),
                Currency.PEN,
                TaxCalculation.of(Money.of(new BigDecimal("100.00"), Currency.PEN), Money.of(new BigDecimal("18.00"), Currency.PEN), Money.of(new BigDecimal("118.00"), Currency.PEN)),
                List.of(taxEngine.calculateLine(null, voucherId, Optional.empty(), VoucherItemType.SERVICE, "Svc", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        // Acceptance
        DigitalReceiptUrls urls = DigitalReceiptUrls.of("pdf", "xml", "cdr");
        ProcessSunatResponseCommand acceptCmd = new ProcessSunatResponseCommand(
                voucherId, true, "0", "CDR Aceptado", "hashABC", urls
        );
        ElectronicVoucher accepted = commandService.handle(acceptCmd);
        assertThat(accepted.getStatus()).isEqualTo(VoucherStatus.ACCEPTED_SUNAT);

        // Reset and test rejection on another voucher
        VoucherId rejId = VoucherId.generate();
        ElectronicVoucher voucher2 = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                workOrderId,
                VoucherType.BOLETA,
                VoucherSerie.of("B001"),
                VoucherNumber.of(2),
                CustomerFiscalInfo.anonymous(),
                Currency.PEN,
                TaxCalculation.of(Money.of(new BigDecimal("100.00"), Currency.PEN), Money.of(new BigDecimal("18.00"), Currency.PEN), Money.of(new BigDecimal("118.00"), Currency.PEN)),
                List.of(taxEngine.calculateLine(null, rejId, Optional.empty(), VoucherItemType.SERVICE, "Svc", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );
        when(voucherRepository.findById(rejId)).thenReturn(Optional.of(voucher2));

        ProcessSunatResponseCommand rejectCmd = new ProcessSunatResponseCommand(
                rejId, false, "2015", "RUC no habido", "", DigitalReceiptUrls.empty()
        );
        ElectronicVoucher rejected = commandService.handle(rejectCmd);
        assertThat(rejected.getStatus()).isEqualTo(VoucherStatus.REJECTED_SUNAT);
    }
}
