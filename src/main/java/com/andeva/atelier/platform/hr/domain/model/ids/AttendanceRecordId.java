package com.andeva.atelier.platform.hr.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record AttendanceRecordId(UUID value) implements Serializable {

    public AttendanceRecordId {
        Objects.requireNonNull(value, "AttendanceRecordId value cannot be null");
    }

    public static AttendanceRecordId generate() {
        return new AttendanceRecordId(UUID.randomUUID());
    }

    public static AttendanceRecordId of(UUID value) {
        return new AttendanceRecordId(value);
    }
}
