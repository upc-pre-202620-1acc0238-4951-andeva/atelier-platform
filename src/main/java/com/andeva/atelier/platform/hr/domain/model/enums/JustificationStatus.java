package com.andeva.atelier.platform.hr.domain.model.enums;

/**
 * Review state of a lateness or absence justification.
 */
public enum JustificationStatus {
    /** Submitted by the employee, awaiting the administration's verdict. */
    PENDING,
    /** Accepted: the attendance is excused. */
    APPROVED,
    /** Dismissed: the attendance keeps its late or absent status. */
    REJECTED
}
