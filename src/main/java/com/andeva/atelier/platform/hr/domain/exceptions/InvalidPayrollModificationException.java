package com.andeva.atelier.platform.hr.domain.exceptions;

public class InvalidPayrollModificationException extends HrDomainException {

    public InvalidPayrollModificationException(String message) {
        super("ERR_HR_PAYROLL_ALREADY_LOCKED", message);
    }

    public InvalidPayrollModificationException() {
        super("ERR_HR_PAYROLL_ALREADY_LOCKED", "No se pueden realizar modificaciones en una liquidación de nómina aprobada o pagada");
    }
}
