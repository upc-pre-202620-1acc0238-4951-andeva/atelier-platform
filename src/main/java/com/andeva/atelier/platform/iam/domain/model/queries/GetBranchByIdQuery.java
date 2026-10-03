package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

import java.util.Objects;

/**
 * Domain query to retrieve a physical workshop branch by its unique identifier.
 *
 * @author Joel Huamani Estefanero
 */
public record GetBranchByIdQuery(
        BranchId branchId
) {
    public GetBranchByIdQuery {
        Objects.requireNonNull(branchId, "Branch identifier cannot be null");
    }
}
