package com.andeva.atelier.platform.hr.interfaces.acl.dto;

import java.io.Serializable;
import java.util.UUID;

public record MechanicDutyProfileAclDto(
        UUID employeeProfileId,
        UUID membershipId,
        UUID branchId,
        String jobTitle,
        String employmentStatus,
        boolean isOnDuty
) implements Serializable {}
