package com.andeva.atelier.platform.crm.domain.model.queries;

import com.andeva.atelier.platform.crm.domain.model.valueobjects.Vin;

import java.util.Objects;

/**
 * Query for retrieving a vehicle by its universal 17-character VIN.
 *
 * @author Adiel Sanchez Santin
 */
public record GetVehicleByVinQuery(Vin vin) {
    public GetVehicleByVinQuery {
        Objects.requireNonNull(vin, "Vin cannot be null");
    }
}
