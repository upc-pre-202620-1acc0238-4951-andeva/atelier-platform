package com.andeva.atelier.platform.hr.interfaces.acl.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollLaborCostAclDto(
        UUID tenantId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal totalBaseSalary,
        BigDecimal totalBonuses,
        BigDecimal totalDeductions,
        BigDecimal totalNetDisbursed
) implements Serializable {}
