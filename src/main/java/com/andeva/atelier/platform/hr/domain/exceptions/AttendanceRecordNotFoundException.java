package com.andeva.atelier.platform.hr.domain.exceptions;

import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;

public class AttendanceRecordNotFoundException extends HrDomainException {

    public AttendanceRecordNotFoundException(AttendanceRecordId id) {
        super("ERR_HR_ATTENDANCE_NOT_FOUND", "No se encontró el registro de asistencia con ID: " + id.value());
    }

    public AttendanceRecordNotFoundException(String message) {
        super("ERR_HR_ATTENDANCE_NOT_FOUND", message);
    }
}
