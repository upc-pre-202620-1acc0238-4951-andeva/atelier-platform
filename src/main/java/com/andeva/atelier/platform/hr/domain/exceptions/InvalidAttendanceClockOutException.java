package com.andeva.atelier.platform.hr.domain.exceptions;

public class InvalidAttendanceClockOutException extends HrDomainException {

    public InvalidAttendanceClockOutException(String message) {
        super("ERR_HR_INVALID_CLOCK_OUT", message);
    }
}
