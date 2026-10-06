package com.andeva.atelier.platform.hr.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record PayrollPaymentId(UUID value) implements Serializable {

    public PayrollPaymentId {
        Objects.requireNonNull(value, "PayrollPaymentId value cannot be null");
    }

    public static PayrollPaymentId generate() {
        return new PayrollPaymentId(UUID.randomUUID());
    }

    public static PayrollPaymentId of(UUID value) {
        return new PayrollPaymentId(value);
    }
}
