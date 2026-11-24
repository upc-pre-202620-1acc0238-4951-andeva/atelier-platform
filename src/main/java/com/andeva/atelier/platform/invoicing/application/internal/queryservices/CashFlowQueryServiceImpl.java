package com.andeva.atelier.platform.invoicing.application.internal.queryservices;

import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.BranchAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.TenantAclDto;
import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CashFlowPdfGeneratorPort;
import com.andeva.atelier.platform.invoicing.application.queryservices.CashFlowQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetCashFlowSummaryQuery;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.BranchCashFlowBreakdown;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowReportContext;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.invoicing.domain.repositories.VoucherPaymentRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Read-only Query Service implementing workshop cash flow aggregation,
 * fiscal breakdown (labor, spare parts, IGV), multi-branch corporate consolidation,
 * and vector PDF statement generation.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class CashFlowQueryServiceImpl implements CashFlowQueryService {

    private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");

    private final VoucherPaymentRepository paymentRepository;
    private final CashFlowPdfGeneratorPort pdfGeneratorPort;
    private final ElectronicVoucherRepository voucherRepository;
    private final TenancyContextFacade tenancyContextFacade;

    /**
     * Backward-compatible constructor for unit tests that only mock paymentRepository and pdfGeneratorPort.
     */
    public CashFlowQueryServiceImpl(
            VoucherPaymentRepository paymentRepository,
            CashFlowPdfGeneratorPort pdfGeneratorPort
    ) {
        this(paymentRepository, pdfGeneratorPort, null, null);
    }

    @Autowired
    public CashFlowQueryServiceImpl(
            VoucherPaymentRepository paymentRepository,
            CashFlowPdfGeneratorPort pdfGeneratorPort,
            ElectronicVoucherRepository voucherRepository,
            TenancyContextFacade tenancyContextFacade
    ) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "Payment repository cannot be null");
        this.pdfGeneratorPort = Objects.requireNonNull(pdfGeneratorPort, "Cash flow PDF generator port cannot be null");
        this.voucherRepository = voucherRepository;
        this.tenancyContextFacade = tenancyContextFacade;
    }

    @Override
    public CashFlowSummary handle(GetCashFlowSummaryQuery query) {
        Objects.requireNonNull(query, "GetCashFlowSummaryQuery cannot be null");

        List<CashFlowMovement> movements = getCashFlowMovements(query.tenantId(), query.branchId(), query.from(), query.to());

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (CashFlowMovement m : movements) {
            if ("INFLOW".equalsIgnoreCase(m.type())) {
                totalIncome = totalIncome.add(m.amount());
            } else {
                totalExpenses = totalExpenses.add(m.amount());
            }
        }

        List<ElectronicVoucher> activeVouchers = fetchActiveSalesVouchers(query.tenantId(), query.branchId(), query.from(), query.to());
        Currency reportCurrency = resolvePrimaryCurrency(activeVouchers);

        BigDecimal laborRevenue = BigDecimal.ZERO;
        BigDecimal partsRevenue = BigDecimal.ZERO;
        BigDecimal igvCollected = BigDecimal.ZERO;

        for (ElectronicVoucher voucher : activeVouchers) {
            igvCollected = igvCollected.add(voucher.getTaxCalculation().igvAmount().amount());
            for (VoucherLine line : voucher.getLines()) {
                if (line.getItemType() == VoucherItemType.SERVICE) {
                    laborRevenue = laborRevenue.add(line.getTotalLine().amount());
                } else {
                    partsRevenue = partsRevenue.add(line.getTotalLine().amount());
                }
            }
        }

        List<BranchCashFlowBreakdown> branchBreakdowns = List.of();
        if (query.branchId() == null) {
            branchBreakdowns = buildBranchBreakdowns(query.tenantId(), query.from(), query.to(), activeVouchers, reportCurrency);
        }

        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);
        return CashFlowSummary.of(
                Money.of(totalIncome, reportCurrency),
                Money.of(totalExpenses, reportCurrency),
                Money.of(BigDecimal.ZERO, reportCurrency),
                Money.of(netCashFlow, reportCurrency),
                Money.of(laborRevenue, reportCurrency),
                Money.of(partsRevenue, reportCurrency),
                Money.of(igvCollected, reportCurrency),
                branchBreakdowns
        );
    }

    @Override
    public List<CashFlowMovement> getCashFlowMovements(TenantId tenantId, LocalDate from, LocalDate to) {
        return getCashFlowMovements(tenantId, null, from, to);
    }

    @Override
    public List<CashFlowMovement> getCashFlowMovements(TenantId tenantId, BranchId branchId, LocalDate from, LocalDate to) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(from, "From date cannot be null");
        Objects.requireNonNull(to, "To date cannot be null");

        Instant start = from.atStartOfDay(LIMA_ZONE).toInstant();
        Instant end = to.atTime(LocalTime.MAX).atZone(LIMA_ZONE).toInstant();

        List<VoucherPayment> payments = branchId != null
                ? paymentRepository.findByBranchIdAndDateRange(branchId, start, end).stream()
                        .filter(p -> tenantId.equals(p.getTenantId()))
                        .toList()
                : paymentRepository.findByTenantIdAndDateRange(tenantId, start, end);

        List<CashFlowMovement> movements = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;

        for (VoucherPayment p : payments) {
            if (p.getStatus() == PaymentStatus.COMPLETED) {
                BigDecimal amount = p.getAmount().amount();
                runningBalance = runningBalance.add(amount);
                String ref = p.getTransactionReference() != null && !p.getTransactionReference().isBlank()
                        ? p.getTransactionReference()
                        : "N/A";
                movements.add(CashFlowMovement.of(
                        p.getId().value(),
                        p.getPaidAt(),
                        "INFLOW",
                        "PAYMENT",
                        "Pago: " + p.getPaymentMethod().name() + " (" + ref + ")",
                        ref,
                        amount,
                        runningBalance
                ));
            }
        }

        return movements;
    }

    @Override
    public byte[] exportCashFlowPdf(TenantId tenantId, LocalDate from, LocalDate to) {
        return exportCashFlowPdf(tenantId, null, from, to);
    }

    @Override
    public byte[] exportCashFlowPdf(TenantId tenantId, BranchId branchId, LocalDate from, LocalDate to) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(from, "From date cannot be null");
        Objects.requireNonNull(to, "To date cannot be null");

        CashFlowSummary summary = handle(new GetCashFlowSummaryQuery(tenantId, branchId, from, to));
        List<CashFlowMovement> movements = getCashFlowMovements(tenantId, branchId, from, to);

        if (tenancyContextFacade == null && voucherRepository == null) {
            return pdfGeneratorPort.generateCashFlowPdf(tenantId.value(), summary, movements);
        }

        CashFlowReportContext context = buildReportContext(tenantId, branchId, from, to);
        return pdfGeneratorPort.generateCashFlowPdf(tenantId.value(), context, summary, movements);
    }

    private List<ElectronicVoucher> fetchActiveSalesVouchers(TenantId tenantId, BranchId branchId, LocalDate from, LocalDate to) {
        if (voucherRepository == null) {
            return List.of();
        }
        List<ElectronicVoucher> vouchers = branchId != null
                ? voucherRepository.findAllByTenantIdAndBranchIdAndDateRange(tenantId, branchId, from, to)
                : voucherRepository.findAllByTenantIdAndDateRange(tenantId, from, to);
        if (vouchers == null) {
            return List.of();
        }
        return vouchers.stream()
                .filter(v -> v.getVoucherType() == VoucherType.FACTURA || v.getVoucherType() == VoucherType.BOLETA)
                .filter(v -> v.getStatus() == VoucherStatus.ACCEPTED_SUNAT || v.getStatus() == VoucherStatus.ISSUED)
                .toList();
    }

    private List<BranchCashFlowBreakdown> buildBranchBreakdowns(
            TenantId tenantId,
            LocalDate from,
            LocalDate to,
            List<ElectronicVoucher> activeVouchers,
            Currency currency
    ) {
        Map<UUID, BranchAccumulator> byBranch = new LinkedHashMap<>();

        for (ElectronicVoucher v : activeVouchers) {
            UUID bId = v.getBranchId().value();
            BranchAccumulator acc = byBranch.computeIfAbsent(bId, k -> new BranchAccumulator());
            acc.igv = acc.igv.add(v.getTaxCalculation().igvAmount().amount());
            for (VoucherLine line : v.getLines()) {
                if (line.getItemType() == VoucherItemType.SERVICE) {
                    acc.labor = acc.labor.add(line.getTotalLine().amount());
                } else {
                    acc.parts = acc.parts.add(line.getTotalLine().amount());
                }
            }
        }

        Instant start = from.atStartOfDay(LIMA_ZONE).toInstant();
        Instant end = to.atTime(LocalTime.MAX).atZone(LIMA_ZONE).toInstant();
        List<VoucherPayment> payments = paymentRepository.findByTenantIdAndDateRange(tenantId, start, end);
        if (payments != null) {
            for (VoucherPayment p : payments) {
                if (p.getStatus() == PaymentStatus.COMPLETED && p.getBranchId() != null) {
                    UUID bId = p.getBranchId().value();
                    BranchAccumulator acc = byBranch.computeIfAbsent(bId, k -> new BranchAccumulator());
                    acc.collected = acc.collected.add(p.getAmount().amount());
                }
            }
        }

        List<BranchCashFlowBreakdown> result = new ArrayList<>();
        for (Map.Entry<UUID, BranchAccumulator> entry : byBranch.entrySet()) {
            UUID bId = entry.getKey();
            BranchAccumulator acc = entry.getValue();
            String branchName = resolveBranchName(bId);
            BigDecimal total = acc.collected.signum() > 0 ? acc.collected : acc.labor.add(acc.parts);
            result.add(new BranchCashFlowBreakdown(
                    bId,
                    branchName,
                    Money.of(acc.labor, currency),
                    Money.of(acc.parts, currency),
                    Money.of(acc.igv, currency),
                    Money.of(total, currency)
            ));
        }
        return result;
    }

    private CashFlowReportContext buildReportContext(TenantId tenantId, BranchId branchId, LocalDate from, LocalDate to) {
        String workshopName = "ATELIER AUTOMOTIVE PLATFORM";
        String legalName = "Atelier Workshop";
        String ruc = "-";

        if (tenancyContextFacade != null) {
            TenantAclDto tenant = tenancyContextFacade.fetchTenantById(tenantId.value()).orElse(null);
            if (tenant != null) {
                if (tenant.name() != null && !tenant.name().isBlank()) {
                    workshopName = tenant.name();
                }
                if (tenant.legalName() != null && !tenant.legalName().isBlank()) {
                    legalName = tenant.legalName();
                }
                if (tenant.taxId() != null && !tenant.taxId().isBlank()) {
                    ruc = tenant.taxId();
                }
            }
        }

        boolean multiBranch = (branchId == null);
        String scopeLabel = multiBranch
                ? "Balance Corporativo Consolidado - Todas las Sedes"
                : "Sucursal: " + resolveBranchName(branchId.value());

        return new CashFlowReportContext(workshopName, legalName, ruc, scopeLabel, from, to, multiBranch);
    }

    private String resolveBranchName(UUID branchId) {
        if (tenancyContextFacade != null) {
            return tenancyContextFacade.fetchBranchById(branchId)
                    .map(BranchAclDto::name)
                    .filter(name -> name != null && !name.isBlank())
                    .orElse("Sede " + branchId.toString().substring(0, 8));
        }
        return "Sede " + branchId.toString().substring(0, 8);
    }

    private Currency resolvePrimaryCurrency(List<ElectronicVoucher> vouchers) {
        if (vouchers != null && !vouchers.isEmpty()) {
            return vouchers.getFirst().getCurrency();
        }
        return Currency.PEN;
    }

    private static final class BranchAccumulator {
        BigDecimal labor = BigDecimal.ZERO;
        BigDecimal parts = BigDecimal.ZERO;
        BigDecimal igv = BigDecimal.ZERO;
        BigDecimal collected = BigDecimal.ZERO;
    }
}
