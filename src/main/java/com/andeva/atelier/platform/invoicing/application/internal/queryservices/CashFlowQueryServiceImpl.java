package com.andeva.atelier.platform.invoicing.application.internal.queryservices;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CashFlowPdfGeneratorPort;
import com.andeva.atelier.platform.invoicing.application.queryservices.CashFlowQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetCashFlowSummaryQuery;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.invoicing.domain.repositories.VoucherPaymentRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Read-only Query Service implementing workshop cash flow aggregation,
 * income/expense movement consolidation, and vector PDF statement generation.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class CashFlowQueryServiceImpl implements CashFlowQueryService {

    private final VoucherPaymentRepository paymentRepository;
    private final CashFlowPdfGeneratorPort pdfGeneratorPort;

    public CashFlowQueryServiceImpl(
            VoucherPaymentRepository paymentRepository,
            CashFlowPdfGeneratorPort pdfGeneratorPort
    ) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "Payment repository cannot be null");
        this.pdfGeneratorPort = Objects.requireNonNull(pdfGeneratorPort, "Cash flow PDF generator port cannot be null");
    }

    @Override
    public CashFlowSummary handle(GetCashFlowSummaryQuery query) {
        Objects.requireNonNull(query, "GetCashFlowSummaryQuery cannot be null");

        List<CashFlowMovement> movements = getCashFlowMovements(query.tenantId(), query.from(), query.to());

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (CashFlowMovement m : movements) {
            if ("INFLOW".equalsIgnoreCase(m.type())) {
                totalIncome = totalIncome.add(m.amount());
            } else {
                totalExpenses = totalExpenses.add(m.amount());
            }
        }

        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);
        return CashFlowSummary.of(
                Money.of(totalIncome, Currency.PEN),
                Money.of(totalExpenses, Currency.PEN),
                Money.of(BigDecimal.ZERO, Currency.PEN),
                Money.of(netCashFlow, Currency.PEN)
        );
    }

    @Override
    public List<CashFlowMovement> getCashFlowMovements(TenantId tenantId, LocalDate from, LocalDate to) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(from, "From date cannot be null");
        Objects.requireNonNull(to, "To date cannot be null");

        java.time.Instant start = from.atStartOfDay(java.time.ZoneId.of("America/Lima")).toInstant();
        java.time.Instant end = to.atTime(java.time.LocalTime.MAX).atZone(java.time.ZoneId.of("America/Lima")).toInstant();

        List<VoucherPayment> payments = paymentRepository.findByTenantIdAndDateRange(tenantId, start, end);

        List<CashFlowMovement> movements = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;

        for (VoucherPayment p : payments) {
            if (p.getStatus() == com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus.COMPLETED) {
                BigDecimal amount = p.getAmount().amount();
                runningBalance = runningBalance.add(amount);
                String ref = p.getTransactionReference() != null && !p.getTransactionReference().isBlank() ? p.getTransactionReference() : "N/A";
                movements.add(CashFlowMovement.of(
                        p.getId().value(),
                        p.getPaidAt(),
                        "INFLOW",
                        "PAYMENT",
                        "Payment: " + p.getPaymentMethod().name() + " (" + ref + ")",
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
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(from, "From date cannot be null");
        Objects.requireNonNull(to, "To date cannot be null");

        CashFlowSummary summary = handle(new GetCashFlowSummaryQuery(tenantId, from, to));
        List<CashFlowMovement> movements = getCashFlowMovements(tenantId, from, to);

        return pdfGeneratorPort.generateCashFlowPdf(tenantId.value(), summary, movements);
    }
}
