package com.andeva.atelier.platform.hr.application.commandservices;

import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.commands.*;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface PayrollPaymentCommandService {
    Result<PayrollPayment, ApplicationError> handle(GeneratePayrollCommand command);
    Result<PayrollPayment, ApplicationError> handle(AddPayrollDeductionCommand command);
    Result<PayrollPayment, ApplicationError> handle(AddPayrollBonusCommand command);
    Result<PayrollPayment, ApplicationError> handle(ApprovePayrollCommand command);
    Result<PayrollPayment, ApplicationError> handle(DisbursePayrollPaymentCommand command);
}
