package com.andeva.atelier.platform.invoicing.application;

import com.andeva.atelier.platform.invoicing.application.internal.commandservices.ElectronicVoucherCommandServiceImpl;
import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CustomerFiscalValidationAclService;
import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.NubefactPseFiscalGateway;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherAlreadyIssuedForWorkOrderException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.VoucherLineCommandDto;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.invoicing.domain.repositories.SeriesConfigurationRepository;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.invoicing.domain.services.SeriesCorrelativeService;
import com.andeva.atelier.platform.invoicing.domain.services.VoucherValidationService;
import com.andeva.atelier.platform.invoicing.infrastructure.external.tax.nubefact.SunatContingencyRetryWorker;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.MeasurementUnit;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxIdType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AT-24 / US37 - Electronic Voucher UBL 2.1 Issuance, Idempotency & SUNAT Contingency")
class ElectronicVoucherIssuanceAndContingencyTest {

    @Mock private ElectronicVoucherRepository voucherRepository;
    @Mock private SeriesConfigurationRepository seriesRepository;
    @Mock private NubefactPseFiscalGateway nubefactGateway;
    @Mock private CustomerFiscalValidationAclService customerFiscalAcl;

    private ElectronicVoucherCommandServiceImpl commandService;
    private SunatContingencyRetryWorker contingencyWorker;

    private TenantId tenantId;
    private BranchId branchId;
    private CustomerId customerId;
    private WorkOrderId workOrderId;

    @BeforeEach
    void setUp() {
        tenantId = TenantId.generate();
        branchId = BranchId.generate();
        customerId = CustomerId.generate();
        workOrderId = WorkOrderId.generate();

        commandService = new ElectronicVoucherCommandServiceImpl(
                voucherRepository,
                seriesRepository,
                new SeriesCorrelativeService(),
                new PeruvianTaxCalculationEngine(),
                new VoucherValidationService(),
                nubefactGateway,
                customerFiscalAcl
        );
        contingencyWorker = new SunatContingencyRetryWorker(voucherRepository, commandService);
    }

    @Test
    @DisplayName("Scenario 1: Issues Factura Electronica to company with valid 11-digit RUC and stores CDR, hash and URLs")
    void issuesFacturaSuccessfullyWithCdrHashAndUrls() {
        SeriesConfiguration series = SeriesConfiguration.create(tenantId, branchId, VoucherType.FACTURA, VoucherSerie.of("F001"), 10);
        when(voucherRepository.findAllByWorkOrderId(workOrderId)).thenReturn(List.of());
        when(seriesRepository.findByTenantIdAndBranchIdAndVoucherTypeAndActive(tenantId, branchId, VoucherType.FACTURA))
                .thenReturn(Optional.of(series));
        when(seriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(voucherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(nubefactGateway.dispatchVoucher(any())).thenReturn(new NubefactPseFiscalGateway.NubefactDispatchResult(
                true,
                "0",
                "La Factura numero F001-00000011 ha sido aceptada por SUNAT",
                "hash-sunat-abc123",
                DigitalReceiptUrls.of("https://pse/f001-11.pdf", "https://pse/f001-11.xml", "https://pse/r-f001-11.xml", "20608945231|01|F001|00000011|64.07|420.00")
        ));

        IssueElectronicVoucherCommand command = new IssueElectronicVoucherCommand(
                tenantId,
                branchId,
                customerId,
                Optional.of(workOrderId),
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                CustomerFiscalInfo.of(new TaxId("20612345679", TaxIdType.RUC), "TRANSPORTES ANDINOS S.A.C.", "Av. Javier Prado 1234, Lima", DocumentType.RUC),
                Currency.PEN,
                List.of(new VoucherLineCommandDto(Optional.of(UUID.randomUUID()), VoucherItemType.SERVICE, "Mantenimiento preventivo", Quantity.of(BigDecimal.ONE, MeasurementUnit.UNIT), Money.of(new BigDecimal("420.00"), Currency.PEN)))
        );

        ElectronicVoucher voucher = commandService.handle(command);

        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.ACCEPTED_SUNAT);
        assertThat(voucher.getSerie().value()).isEqualTo("F001");
        assertThat(voucher.getNumber().value()).isEqualTo(11);
        assertThat(voucher.getSunatResponse()).isPresent();
        assertThat(voucher.getSunatResponse().get().digitalSignatureHash()).isEqualTo("hash-sunat-abc123");
        assertThat(voucher.getDigitalReceiptUrls().pdfUrl()).isEqualTo("https://pse/f001-11.pdf");
        assertThat(voucher.getDigitalReceiptUrls().xmlUrl()).isEqualTo("https://pse/f001-11.xml");
        assertThat(voucher.getDigitalReceiptUrls().cdrUrl()).isEqualTo("https://pse/r-f001-11.xml");
    }

    @Test
    @DisplayName("Scenario 2: Issues Boleta de Venta to natural person with 8-digit DNI and QR verification data")
    void issuesBoletaSuccessfullyWithQrVerificationData() {
        SeriesConfiguration series = SeriesConfiguration.create(tenantId, branchId, VoucherType.BOLETA, VoucherSerie.of("B001"), 5);
        when(voucherRepository.findAllByWorkOrderId(workOrderId)).thenReturn(List.of());
        when(seriesRepository.findByTenantIdAndBranchIdAndVoucherTypeAndActive(tenantId, branchId, VoucherType.BOLETA))
                .thenReturn(Optional.of(series));
        when(seriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(voucherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(nubefactGateway.dispatchVoucher(any())).thenReturn(new NubefactPseFiscalGateway.NubefactDispatchResult(
                true,
                "0",
                "La Boleta numero B001-00000006 ha sido aceptada por SUNAT",
                "hash-boleta-xyz",
                DigitalReceiptUrls.of("https://pse/b001-6.pdf", "https://pse/b001-6.xml", "https://pse/r-b001-6.xml", "20601234561|03|B001|00000006|27.46|180.00|24-11-2026|1|45678912|hash-boleta-xyz")
        ));

        IssueElectronicVoucherCommand command = new IssueElectronicVoucherCommand(
                tenantId,
                branchId,
                customerId,
                Optional.of(workOrderId),
                VoucherType.BOLETA,
                VoucherSerie.of("B001"),
                CustomerFiscalInfo.of(new TaxId("45678912", TaxIdType.DNI), "CARLOS RAMIREZ TORRES", "Calle Las Begonias 200", DocumentType.DNI),
                Currency.PEN,
                List.of(new VoucherLineCommandDto(Optional.of(UUID.randomUUID()), VoucherItemType.SERVICE, "Cambio de aceite", Quantity.of(BigDecimal.ONE, MeasurementUnit.UNIT), Money.of(new BigDecimal("180.00"), Currency.PEN)))
        );

        ElectronicVoucher voucher = commandService.handle(command);

        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.ACCEPTED_SUNAT);
        assertThat(voucher.getSerie().value()).isEqualTo("B001");
        assertThat(voucher.getNumber().value()).isEqualTo(6);
        assertThat(voucher.getDigitalReceiptUrls().qrCodeData()).contains("|03|B001|00000006|");
    }

    @Test
    @DisplayName("Scenario 3: Retains ISSUED with reserved correlative on PSE outage, blocks duplicate issuance, and retries asynchronously")
    void handlesPseOutagePreventsDuplicateCorrelativeAndRetriesAsynchronously() {
        SeriesConfiguration series = SeriesConfiguration.create(tenantId, branchId, VoucherType.FACTURA, VoucherSerie.of("F001"), 20);
        when(voucherRepository.findAllByWorkOrderId(workOrderId)).thenReturn(List.of());
        when(seriesRepository.findByTenantIdAndBranchIdAndVoucherTypeAndActive(tenantId, branchId, VoucherType.FACTURA))
                .thenReturn(Optional.of(series));
        when(seriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(voucherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // 1. Simulate PSE network timeout on first dispatch, then recovery on async retry
        when(nubefactGateway.dispatchVoucher(any()))
                .thenThrow(new RuntimeException("PSE connection timed out"))
                .thenReturn(new NubefactPseFiscalGateway.NubefactDispatchResult(
                        true,
                        "0",
                        "Aceptada por SUNAT tras reintento",
                        "hash-retry-ok",
                        DigitalReceiptUrls.of("https://pse/f001-21.pdf", "https://pse/f001-21.xml", "https://pse/r-f001-21.xml", "QR-21")
                ));

        IssueElectronicVoucherCommand command = new IssueElectronicVoucherCommand(
                tenantId,
                branchId,
                customerId,
                Optional.of(workOrderId),
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                CustomerFiscalInfo.of(new TaxId("20612345679", TaxIdType.RUC), "FLOTA LIMA S.A.C.", "Av. Arequipa 500", DocumentType.RUC),
                Currency.PEN,
                List.of(new VoucherLineCommandDto(Optional.of(UUID.randomUUID()), VoucherItemType.PRODUCT, "Bateria Bosch", Quantity.of(BigDecimal.ONE, MeasurementUnit.UNIT), Money.of(new BigDecimal("350.00"), Currency.PEN)))
        );

        ElectronicVoucher issuedVoucher = commandService.handle(command);
        assertThat(issuedVoucher.getStatus()).isEqualTo(VoucherStatus.ISSUED);
        assertThat(issuedVoucher.getNumber().value()).isEqualTo(21);

        // 2. Duplicate emission attempt on same work order must fail with 409 conflict without consuming correlative 22
        when(voucherRepository.findAllByWorkOrderId(workOrderId)).thenReturn(List.of(issuedVoucher));
        assertThatThrownBy(() -> commandService.handle(command))
                .isInstanceOf(VoucherAlreadyIssuedForWorkOrderException.class);
        verify(seriesRepository, times(1)).save(any());

        // 3. PSE recovers -> Contingency worker retries asynchronously without reallocating correlative
        when(voucherRepository.findAllByStatus(VoucherStatus.ISSUED)).thenReturn(List.of(issuedVoucher));
        when(voucherRepository.findById(issuedVoucher.getId())).thenReturn(Optional.of(issuedVoucher));

        int processed = contingencyWorker.retryPendingIssuedVouchers();

        assertThat(processed).isEqualTo(1);
        assertThat(issuedVoucher.getStatus()).isEqualTo(VoucherStatus.ACCEPTED_SUNAT);
        assertThat(issuedVoucher.getNumber().value()).isEqualTo(21);
        assertThat(issuedVoucher.getSunatResponse()).isPresent();
        assertThat(issuedVoucher.getSunatResponse().get().digitalSignatureHash()).isEqualTo("hash-retry-ok");
        verify(seriesRepository, times(1)).save(any()); // Correlative never incremented again
        verify(nubefactGateway, never()).dispatchCreditNote(any());
    }
}
