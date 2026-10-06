package com.andeva.atelier.platform.hr.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollItemResource(
        String category,
        String concept,
        BigDecimal amount,
        String type,
        LocalDate date
) {}
