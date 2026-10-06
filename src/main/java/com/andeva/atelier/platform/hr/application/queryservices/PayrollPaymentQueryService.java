package com.andeva.atelier.platform.hr.application.queryservices;

import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.queries.ExportSunatPlameRemQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetPayrollPaymentByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListPayrollPaymentsByPeriodQuery;

import java.util.List;
import java.util.Optional;

public interface PayrollPaymentQueryService {
    Optional<PayrollPayment> handle(GetPayrollPaymentByIdQuery query);
    List<PayrollPayment> handle(ListPayrollPaymentsByPeriodQuery query);
    String handle(ExportSunatPlameRemQuery query);
}
