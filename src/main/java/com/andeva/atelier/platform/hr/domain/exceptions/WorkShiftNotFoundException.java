package com.andeva.atelier.platform.hr.domain.exceptions;

import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;

public class WorkShiftNotFoundException extends HrDomainException {

    public WorkShiftNotFoundException(WorkShiftId id) {
        super("ERR_HR_SHIFT_NOT_FOUND", "No se encontró el turno de trabajo con ID: " + id.value());
    }

    public WorkShiftNotFoundException(String message) {
        super("ERR_HR_SHIFT_NOT_FOUND", message);
    }
}
