package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.BranchResource;

import java.util.Objects;

/**
 * Assembler projecting domain {@link Branch} entities into REST {@link BranchResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
public final class BranchResourceFromEntityAssembler {

    private BranchResourceFromEntityAssembler() {
    }

    /**
     * Converts a {@link Branch} entity into a {@link BranchResource}.
     *
     * @param branch Domain branch entity
     * @return REST response projection
     */
    public static BranchResource toResourceFromEntity(Branch branch) {
        Objects.requireNonNull(branch, "Branch entity cannot be null");
        return new BranchResource(
                branch.id().value(),
                branch.tenantId().value(),
                branch.name(),
                branch.sunatCode(),
                branch.location().latitude(),
                branch.location().longitude(),
                branch.geofenceRadiusMeters(),
                branch.isActive()
        );
    }
}
