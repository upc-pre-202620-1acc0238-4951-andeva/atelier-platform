package com.andeva.atelier.platform.hr.domain.exceptions;

public class ShiftConflictException extends HrDomainException {

    public ShiftConflictException(String message) {
        super("ERR_HR_SHIFT_CONFLICT", message);
    }
}
