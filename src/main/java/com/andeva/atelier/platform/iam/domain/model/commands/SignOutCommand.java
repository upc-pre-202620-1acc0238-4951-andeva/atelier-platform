package com.andeva.atelier.platform.iam.domain.model.commands;

import java.util.Objects;

public record SignOutCommand(
        String refreshToken
) {
    public SignOutCommand {
        Objects.requireNonNull(refreshToken, "refreshToken cannot be null");
    }
}
