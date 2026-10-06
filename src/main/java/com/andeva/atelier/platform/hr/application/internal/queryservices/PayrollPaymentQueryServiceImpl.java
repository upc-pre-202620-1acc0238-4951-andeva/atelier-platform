package com.andeva.atelier.platform.hr.application.internal.queryservices;

import com.andeva.atelier.platform.hr.application.queryservices.PayrollPaymentQueryService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.entities.PayrollItem;
import com.andeva.atelier.platform.hr.domain.model.queries.ExportSunatPlameRemQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetPayrollPaymentByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListPayrollPaymentsByPeriodQuery;
import com.andeva.atelier.platform.hr.domain.repositories.PayrollPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PayrollPaymentQueryServiceImpl implements PayrollPaymentQueryService {

    private final PayrollPaymentRepository payrollRepository;

    public PayrollPaymentQueryServiceImpl(PayrollPaymentRepository payrollRepository) {
        this.payrollRepository = Objects.requireNonNull(payrollRepository, "payrollRepository cannot be null");
    }

    @Override
    public Optional<PayrollPayment> handle(GetPayrollPaymentByIdQuery query) {
        return payrollRepository.findById(query.payrollId())
                .filter(p -> p.getTenantId().equals(query.tenantId()));
    }

    @Override
    public List<PayrollPayment> handle(ListPayrollPaymentsByPeriodQuery query) {
        if (query.periodStart() != null && query.periodEnd() != null) {
            return payrollRepository.findAllByTenantIdAndPeriod(query.tenantId(), query.periodStart(), query.periodEnd());
        }
        return payrollRepository.findAllByTenantId(query.tenantId());
    }

    @Override
    public String handle(ExportSunatPlameRemQuery query) {
        List<PayrollPayment> payments = payrollRepository.findAllByTenantId(query.tenantId());

        StringBuilder sb = new StringBuilder();
        // Generando trama PLAME Formato 0601 .rem
        for (PayrollPayment payment : payments) {
            String memberId = payment.getMembershipId().value().toString();
            for (PayrollItem item : payment.getItems()) {
                String conceptCode = mapToSunatConceptCode(item.getType());
                sb.append("01|") // Tipo de Documento DNI por defecto
                        .append(memberId.substring(0, Math.min(8, memberId.length()))).append("|")
                        .append(conceptCode).append("|")
                        .append(item.getAmount().amount().toPlainString()).append("|")
                        .append(item.getAmount().amount().toPlainString()).append("\r\n");
            }
        }
        return sb.toString();
    }

    private String mapToSunatConceptCode(String itemType) {
        return switch (itemType.toUpperCase()) {
            case "BASE" -> "0121"; // Remuneracion Basica
            case "WORK_ORDER_COMMISSION", "COMMISSION" -> "0107"; // Comisiones
            case "OVERTIME" -> "0105"; // Horas Extras
            case "UNJUSTIFIED_ABSENCE" -> "0701"; // Inasistencias
            case "TARDINESS" -> "0703"; // Tardanzas
            case "AFP" -> "0601"; // AFP
            case "ONP" -> "0608"; // ONP
            default -> "0999";
        };
    }
}
