package com.andeva.atelier.platform.hr.domain.exceptions;

import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;

public class PayrollPaymentNotFoundException extends HrDomainException {

    public PayrollPaymentNotFoundException(PayrollPaymentId id) {
        super("ERR_HR_PAYROLL_NOT_FOUND", "No se encontró la liquidación de nómina con ID: " + id.value());
    }

    public PayrollPaymentNotFoundException(String message) {
        super("ERR_HR_PAYROLL_NOT_FOUND", message);
    }
}
