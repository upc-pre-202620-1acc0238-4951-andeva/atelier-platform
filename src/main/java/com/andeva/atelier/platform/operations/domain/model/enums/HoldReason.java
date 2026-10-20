package com.andeva.atelier.platform.operations.domain.model.enums;

public enum HoldReason {
    WAITING_PARTS,
    WAITING_FOR_PARTS,
    /** Meal or rest break taken by the mechanic. */
    BREAK,
    /** The mechanic's shift ended (manual pause or automatic on clock-out). */
    END_OF_SHIFT,
    /** Supplied part is defective; suspension approved by the administration. */
    DEFECTIVE_PART;

    /** Pauses the mechanic may lift alone; the others need an administrative resume. */
    public boolean isSelfResumable() {
        return this == BREAK || this == END_OF_SHIFT;
    }
}
