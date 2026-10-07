package com.andeva.atelier.platform.invoicing.domain;

import com.andeva.atelier.platform.invoicing.domain.exceptions.CorrelativeExhaustedException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.events.SeriesConfigurationCreatedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.SeriesCorrelativeIncrementedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link SeriesConfiguration} aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("SeriesConfiguration Aggregate Unit Tests")
class SeriesConfigurationTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final BranchId branchId = BranchId.of(UUID.randomUUID());

    @Test
    @DisplayName("Should create series configuration and register created event")
    void shouldCreateSeriesConfigurationSuccessfully() {
        VoucherSerie serie = VoucherSerie.of("F001");
        SeriesConfiguration config = SeriesConfiguration.create(
                tenantId,
                branchId,
                VoucherType.FACTURA,
                serie,
                0
        );

        assertThat(config.getId()).isNotNull();
        assertThat(config.getTenantId()).isEqualTo(tenantId);
        assertThat(config.getBranchId()).isEqualTo(branchId);
        assertThat(config.getVoucherType()).isEqualTo(VoucherType.FACTURA);
        assertThat(config.getSerie()).isEqualTo(serie);
        assertThat(config.getCurrentCorrelative()).isEqualTo(0);
        assertThat(config.isActive()).isTrue();

        assertThat(config.domainEvents()).hasSize(1);
        assertThat(config.domainEvents()).first().isInstanceOf(SeriesConfigurationCreatedEvent.class);
    }

    @Test
    @DisplayName("Should increment correlative monotonically and register incremented event")
    void shouldIncrementCorrelativeMonotonically() {
        SeriesConfiguration config = SeriesConfiguration.create(
                tenantId,
                branchId,
                VoucherType.BOLETA,
                VoucherSerie.of("B001"),
                10
        );
        config.clearDomainEvents();

        VoucherNumber next1 = config.nextCorrelative();
        assertThat(next1.value()).isEqualTo(11);
        assertThat(next1.format()).isEqualTo("00000011");
        assertThat(config.getCurrentCorrelative()).isEqualTo(11);

        VoucherNumber next2 = config.nextCorrelative();
        assertThat(next2.value()).isEqualTo(12);

        assertThat(config.domainEvents()).hasSize(2);
        assertThat(config.domainEvents()).first().isInstanceOf(SeriesCorrelativeIncrementedEvent.class);
    }

    @Test
    @DisplayName("Should throw CorrelativeExhaustedException when reaching 99,999,999 upper limit")
    void shouldThrowWhenReachingCorrelativeLimit() {
        SeriesConfiguration config = SeriesConfiguration.create(
                tenantId,
                branchId,
                VoucherType.FACTURA,
                VoucherSerie.of("F001"),
                99_999_999
        );

        assertThatThrownBy(config::nextCorrelative)
                .isInstanceOf(CorrelativeExhaustedException.class);
    }

    @Test
    @DisplayName("Should prevent correlative allocation when series is deactivated")
    void shouldPreventAllocationWhenDeactivated() {
        SeriesConfiguration config = SeriesConfiguration.create(
                tenantId,
                branchId,
                VoucherType.BOLETA,
                VoucherSerie.of("B001"),
                5
        );
        config.deactivate();
        assertThat(config.isActive()).isFalse();

        assertThatThrownBy(config::nextCorrelative)
                .isInstanceOf(InvoicingDomainException.class);

        config.activate();
        assertThat(config.isActive()).isTrue();
        assertThat(config.nextCorrelative().value()).isEqualTo(6);
    }

    @Test
    @DisplayName("Should reject prefix mismatch between voucher type and series")
    void shouldRejectPrefixMismatch() {
        assertThatThrownBy(() -> SeriesConfiguration.create(
                tenantId, branchId, VoucherType.FACTURA, VoucherSerie.of("B001"), 0
        )).isInstanceOf(InvoicingDomainException.class);

        assertThatThrownBy(() -> SeriesConfiguration.create(
                tenantId, branchId, VoucherType.BOLETA, VoucherSerie.of("F001"), 0
        )).isInstanceOf(InvoicingDomainException.class);
    }
}
