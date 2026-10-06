package com.andeva.atelier.platform.hr.interfaces.acl.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceSummaryAclDto(
        UUID membershipId,
        LocalDate date,
        String status,
        long workedMinutes,
        boolean isJustified
) implements Serializable {}
