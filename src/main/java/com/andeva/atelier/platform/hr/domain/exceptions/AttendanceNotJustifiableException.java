package com.andeva.atelier.platform.hr.domain.exceptions;

public class AttendanceNotJustifiableException extends HrDomainException {

    public AttendanceNotJustifiableException(String message) {
        super("ERR_HR_ATTENDANCE_NOT_JUSTIFIABLE", message);
    }

    public AttendanceNotJustifiableException() {
        super("ERR_HR_ATTENDANCE_NOT_JUSTIFIABLE", "Únicamente las marcaciones clasificadas como LATE o ABSENT admiten justificación");
    }
}
