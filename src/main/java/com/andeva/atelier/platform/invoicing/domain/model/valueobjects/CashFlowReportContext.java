package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Institutional letterhead and reporting scope metadata rendered in Cash Flow PDF exports.
 */
public record CashFlowReportContext(
        String workshopName,
        String legalName,
        String ruc,
        String scopeLabel,
        LocalDate from,
        LocalDate to,
        boolean multiBranch
) implements Serializable {

    public static CashFlowReportContext defaultFor(LocalDate from, LocalDate to, boolean multiBranch) {
        return new CashFlowReportContext(
                "ATELIER AUTOMOTIVE PLATFORM",
                "Atelier Workshop",
                "-",
                multiBranch ? "Balance Corporativo Consolidado (Todas las Sedes)" : "Reporte de Sucursal",
                from,
                to,
                multiBranch
        );
    }
}
