package com.andeva.atelier.platform.invoicing.domain.services;

import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Domain service orchestrating the reservation and sequential advancement
 * of fiscal series correlatives.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class SeriesCorrelativeService {

    public VoucherNumber allocateNext(SeriesConfiguration configuration) {
        Objects.requireNonNull(configuration, "Series configuration cannot be null");
        return configuration.nextCorrelative();
    }

    public void validateSeriesFormat(VoucherSerie serie, VoucherType type) {
        Objects.requireNonNull(serie, "Voucher series cannot be null");
        Objects.requireNonNull(type, "Voucher type cannot be null");

        String val = serie.value();
        if (type == VoucherType.FACTURA && !val.startsWith("F")) {
            throw new InvoicingDomainException("ERR_SERIES_PREFIX_MISMATCH", "Factura series must start with 'F': " + val);
        }
        if (type == VoucherType.BOLETA && !val.startsWith("B")) {
            throw new InvoicingDomainException("ERR_SERIES_PREFIX_MISMATCH", "Boleta series must start with 'B': " + val);
        }
        if (type == VoucherType.NOTA_CREDITO && (!val.startsWith("FC") && !val.startsWith("BC") && !val.startsWith("F") && !val.startsWith("B"))) {
            throw new InvoicingDomainException("ERR_SERIES_PREFIX_MISMATCH", "Credit note series must start with 'FC', 'BC', 'F', or 'B': " + val);
        }
    }
}
