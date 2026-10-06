package com.andeva.atelier.platform.hr.domain.exceptions;

import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;

public class EmployeeProfileNotFoundException extends HrDomainException {

    public EmployeeProfileNotFoundException(EmployeeProfileId id) {
        super("ERR_HR_EMPLOYEE_NOT_FOUND", "No se encontró el expediente del empleado con ID: " + id.value());
    }

    public EmployeeProfileNotFoundException(TenantMembershipId membershipId) {
        super("ERR_HR_EMPLOYEE_NOT_FOUND", "No se encontró el expediente del empleado con membresía: " + membershipId.value());
    }

    public EmployeeProfileNotFoundException(String message) {
        super("ERR_HR_EMPLOYEE_NOT_FOUND", message);
    }
}
