package com.andeva.atelier.platform.hr.domain.exceptions;

public class DuplicateActiveAttendanceException extends HrDomainException {

    public DuplicateActiveAttendanceException(String message) {
        super("ERR_HR_DUPLICATE_CLOCK_IN", message);
    }

    public DuplicateActiveAttendanceException() {
        super("ERR_HR_DUPLICATE_CLOCK_IN", "El colaborador ya cuenta con una jornada de asistencia activa sin registrar salida");
    }
}
