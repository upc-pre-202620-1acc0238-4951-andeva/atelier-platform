package com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.SeriesConfigurationResource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resource Assembler transforming {@link SeriesConfiguration} aggregates into {@link SeriesConfigurationResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SeriesConfigurationResourceAssembler {

    public SeriesConfigurationResource toResource(SeriesConfiguration series) {
        if (series == null) {
            return null;
        }

        String typeCode = toSunatTypeCode(series.getVoucherType());

        return new SeriesConfigurationResource(
                series.getId().value(),
                series.getTenantId().value(),
                series.getBranchId().value(),
                typeCode,
                series.getSerie().value(),
                series.getCurrentCorrelative(),
                series.isActive(),
                java.time.Instant.now()
        );
    }

    public List<SeriesConfigurationResource> toResourceList(List<SeriesConfiguration> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream().map(this::toResource).toList();
    }

    private String toSunatTypeCode(VoucherType type) {
        if (type == null) return "01";
        return switch (type) {
            case FACTURA -> "01";
            case BOLETA -> "03";
            case NOTA_CREDITO -> "07";
        };
    }
}
