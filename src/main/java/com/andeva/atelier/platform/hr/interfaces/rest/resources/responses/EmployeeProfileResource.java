package com.andeva.atelier.platform.hr.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.util.UUID;

public record EmployeeProfileResource(
        UUID id,
        UUID branchId,
        UUID membershipId,
        UUID assignedShiftId,
        BigDecimal baseSalary,
        String currency,
        String compensationType,
        String jobTitle,
        String employmentStatus
) {}
