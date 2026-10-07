package com.andeva.atelier.platform.invoicing.application;

import com.andeva.atelier.platform.invoicing.application.acl.InvoicingContextFacadeImpl;
import com.andeva.atelier.platform.invoicing.application.commandservices.ElectronicVoucherCommandService;
import com.andeva.atelier.platform.invoicing.application.queryservices.ElectronicVoucherQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.invoicing.interfaces.acl.InvoicingContextFacade;
import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.GenerateVoucherFromWorkOrderCommandDto;
import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.VoucherGenerationResultDto;
import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.VoucherSummaryDto;
import com.andeva.atelier.platform.invoicing.interfaces.acl.dto.WorkOrderItemBillingDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for InvoicingContextFacadeImpl Inbound ACL OHS.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("InvoicingContextFacadeImpl Tests")
class InvoicingContextFacadeTest {

    private ElectronicVoucherCommandService commandService;
    private ElectronicVoucherQueryService queryService;
    private com.andeva.atelier.platform.invoicing.application.queryservices.SeriesConfigurationQueryService seriesQueryService;
    private InvoicingContextFacade facade;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID branchId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID workOrderId = UUID.randomUUID();
    private final PeruvianTaxCalculationEngine taxEngine = new PeruvianTaxCalculationEngine();

    @BeforeEach
    void setUp() {
        commandService = Mockito.mock(ElectronicVoucherCommandService.class);
        queryService = Mockito.mock(ElectronicVoucherQueryService.class);
        seriesQueryService = Mockito.mock(com.andeva.atelier.platform.invoicing.application.queryservices.SeriesConfigurationQueryService.class);
        facade = new InvoicingContextFacadeImpl(commandService, queryService, seriesQueryService);
    }

    @Test
    @DisplayName("Should generate voucher from work order command")
    void shouldGenerateVoucherFromWorkOrder() {
        VoucherId voucherId = VoucherId.generate();
        ElectronicVoucher voucher = ElectronicVoucher.issue(
                TenantId.of(tenantId),
                BranchId.of(branchId),
                CustomerId.of(customerId),
                WorkOrderId.of(workOrderId),
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                CustomerFiscalInfo.of(TaxId.ruc("20100070970"), "CLIENTE SAC", "LIMA", DocumentType.RUC),
                Currency.PEN,
                TaxCalculation.of(Money.of(new BigDecimal("100.00"), Currency.PEN), Money.of(new BigDecimal("18.00"), Currency.PEN), Money.of(new BigDecimal("118.00"), Currency.PEN)),
                List.of(taxEngine.calculateLine(null, voucherId, Optional.empty(), VoucherItemType.SERVICE, "Servicio", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );

        when(commandService.handle(any(IssueElectronicVoucherCommand.class))).thenReturn(voucher);

        WorkOrderItemBillingDto item = new WorkOrderItemBillingDto(
                UUID.randomUUID(),
                "SERVICE",
                "Mantenimiento 10k",
                BigDecimal.ONE,
                new BigDecimal("118.00")
        );

        GenerateVoucherFromWorkOrderCommandDto cmd = new GenerateVoucherFromWorkOrderCommandDto(
                tenantId,
                branchId,
                workOrderId,
                customerId,
                "FACTURA",
                "20100070970",
                "CLIENTE SAC",
                "AV AVIACION 123",
                "PEN",
                List.of(item)
        );

        VoucherGenerationResultDto result = facade.generateVoucherFromWorkOrder(cmd);

        assertThat(result).isNotNull();
        assertThat(result.fullVoucherNumber()).isEqualTo("F001-00000001");
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("118.00"));
        assertThat(result.status()).isEqualTo("ISSUED");
    }

    @Test
    @DisplayName("Should retrieve vouchers by work order ID")
    void shouldGetVouchersByWorkOrderId() {
        VoucherId voucherId = VoucherId.generate();
        ElectronicVoucher voucher = ElectronicVoucher.issue(
                TenantId.of(tenantId),
                BranchId.of(branchId),
                CustomerId.of(customerId),
                WorkOrderId.of(workOrderId),
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                VoucherNumber.of(1),
                CustomerFiscalInfo.of(TaxId.ruc("20100070970"), "CLIENTE SAC", "LIMA", DocumentType.RUC),
                Currency.PEN,
                TaxCalculation.of(Money.of(new BigDecimal("100.00"), Currency.PEN), Money.of(new BigDecimal("18.00"), Currency.PEN), Money.of(new BigDecimal("118.00"), Currency.PEN)),
                List.of(taxEngine.calculateLine(null, voucherId, Optional.empty(), VoucherItemType.SERVICE, "Servicio", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );

        when(queryService.getVouchersByWorkOrderId(WorkOrderId.of(workOrderId))).thenReturn(List.of(voucher));

        List<VoucherSummaryDto> summaries = facade.getVouchersByWorkOrderId(workOrderId);

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).voucherType()).isEqualTo("FACTURA");
        assertThat(summaries.get(0).isFullyPaid()).isFalse();
    }

    @Test
    @DisplayName("Should verify whether work order is fully settled")
    void shouldVerifyWorkOrderFullySettled() {
        // Case 1: No vouchers -> false
        when(queryService.getVouchersByWorkOrderId(WorkOrderId.of(workOrderId))).thenReturn(List.of());
        assertThat(facade.isWorkOrderFullySettled(workOrderId)).isFalse();

        // Case 2: Unpaid voucher -> false
        VoucherId voucherId = VoucherId.generate();
        ElectronicVoucher unpaidVoucher = ElectronicVoucher.issue(
                TenantId.of(tenantId),
                BranchId.of(branchId),
                CustomerId.of(customerId),
                WorkOrderId.of(workOrderId),
                VoucherType.BOLETA,
                VoucherSerie.of("B001"),
                VoucherNumber.of(1),
                CustomerFiscalInfo.anonymous(),
                Currency.PEN,
                TaxCalculation.of(Money.of(new BigDecimal("100.00"), Currency.PEN), Money.of(new BigDecimal("18.00"), Currency.PEN), Money.of(new BigDecimal("118.00"), Currency.PEN)),
                List.of(taxEngine.calculateLine(null, voucherId, Optional.empty(), VoucherItemType.SERVICE, "Servicio", Quantity.ofUnits(1), Money.of(new BigDecimal("118.00"), Currency.PEN)))
        );

        when(queryService.getVouchersByWorkOrderId(WorkOrderId.of(workOrderId))).thenReturn(List.of(unpaidVoucher));
        assertThat(facade.isWorkOrderFullySettled(workOrderId)).isFalse();

        // Case 3: Fully paid voucher -> true
        unpaidVoucher.recordPayment(PaymentId.generate(), Money.of(new BigDecimal("118.00"), Currency.PEN), com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod.CASH, null);
        assertThat(facade.isWorkOrderFullySettled(workOrderId)).isTrue();
    }
}
