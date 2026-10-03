package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

/**
 * Thrown when a physical workshop Branch cannot be located by its identifier.
 *
 * @author Joel Huamani Estefanero
 */
public class BranchNotFoundException extends IamDomainException {

    public BranchNotFoundException(BranchId branchId) {
        super("BRANCH_NOT_FOUND", "Workshop branch not found with identifier: " + (branchId != null ? branchId.value() : "null"));
    }

    public BranchNotFoundException(String message) {
        super("BRANCH_NOT_FOUND", message);
    }
}
