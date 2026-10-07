package com.andeva.atelier.platform.invoicing.domain.model.aggregates;

import com.andeva.atelier.platform.invoicing.domain.exceptions.CorrelativeExhaustedException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.events.SeriesConfigurationCreatedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.SeriesCorrelativeIncrementedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Aggregate Root governing authorized fiscal series configurations and monotonic
 * sequential correlative allocation per physical branch and voucher type.
 *
 * @author Joel Huamani Estefanero
 */
public class SeriesConfiguration extends AbstractDomainAggregateRoot<SeriesConfiguration> {

    private final SeriesConfigurationId id;
    private final TenantId tenantId;
    private final BranchId branchId;
    private final VoucherType voucherType;
    private final VoucherSerie serie;
    private int currentCorrelative;
    private boolean isActive;

    public SeriesConfiguration(
            SeriesConfigurationId id,
            TenantId tenantId,
            BranchId branchId,
            VoucherType voucherType,
            VoucherSerie serie,
            int currentCorrelative,
            boolean isActive
    ) {
        this.id = Objects.requireNonNull(id, "SeriesConfigurationId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "BranchId cannot be null");
        this.voucherType = Objects.requireNonNull(voucherType, "VoucherType cannot be null");
        this.serie = Objects.requireNonNull(serie, "VoucherSerie cannot be null");

        if (currentCorrelative < 0 || currentCorrelative > VoucherNumber.MAX_VALUE) {
            throw new InvoicingDomainException(
                    "ERR_INVALID_CORRELATIVE",
                    "Correlative out of valid range [0, " + VoucherNumber.MAX_VALUE + "]: " + currentCorrelative
            );
        }

        validateSeriesPrefix(voucherType, serie);

        this.currentCorrelative = currentCorrelative;
        this.isActive = isActive;
    }

    public static SeriesConfiguration create(
            TenantId tenantId,
            BranchId branchId,
            VoucherType type,
            VoucherSerie serie,
            int initialCorrelative
    ) {
        SeriesConfigurationId seriesId = SeriesConfigurationId.generate();
        SeriesConfiguration config = new SeriesConfiguration(
                seriesId,
                tenantId,
                branchId,
                type,
                serie,
                initialCorrelative,
                true
        );

        config.registerEvent(SeriesConfigurationCreatedEvent.of(
                seriesId,
                tenantId,
                branchId,
                type,
                serie,
                initialCorrelative
        ));

        return config;
    }

    public VoucherNumber nextCorrelative() {
        if (!isActive) {
            throw new InvoicingDomainException(
                    "ERR_SERIES_INACTIVE",
                    "Fiscal series '" + serie.value() + "' is inactive and cannot issue new correlatives"
            );
        }

        if (currentCorrelative >= VoucherNumber.MAX_VALUE) {
            throw new CorrelativeExhaustedException(serie);
        }

        this.currentCorrelative++;

        registerEvent(SeriesCorrelativeIncrementedEvent.of(
                id,
                tenantId,
                branchId,
                serie,
                currentCorrelative
        ));

        return VoucherNumber.of(currentCorrelative);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    private static void validateSeriesPrefix(VoucherType voucherType, VoucherSerie serie) {
        String val = serie.value();
        if (voucherType == VoucherType.FACTURA && !val.startsWith("F")) {
            throw new InvoicingDomainException("ERR_SERIES_PREFIX_MISMATCH", "Factura series must start with 'F': " + val);
        }
        if (voucherType == VoucherType.BOLETA && !val.startsWith("B")) {
            throw new InvoicingDomainException("ERR_SERIES_PREFIX_MISMATCH", "Boleta series must start with 'B': " + val);
        }
        if (voucherType == VoucherType.NOTA_CREDITO && (!val.startsWith("FC") && !val.startsWith("BC") && !val.startsWith("F") && !val.startsWith("B"))) {
            throw new InvoicingDomainException("ERR_SERIES_PREFIX_MISMATCH", "Credit note series must start with 'FC', 'BC', 'F', or 'B': " + val);
        }
    }

    public SeriesConfigurationId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public BranchId getBranchId() {
        return branchId;
    }

    public VoucherType getVoucherType() {
        return voucherType;
    }

    public VoucherSerie getSerie() {
        return serie;
    }

    public int getCurrentCorrelative() {
        return currentCorrelative;
    }

    public boolean isActive() {
        return isActive;
    }
}
