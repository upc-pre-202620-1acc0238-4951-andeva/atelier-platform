package com.andeva.atelier.platform.iam.domain.model.commands;

import java.util.Objects;

public record RefreshAccessTokenCommand(
        String refreshToken
) {
    public RefreshAccessTokenCommand {
        Objects.requireNonNull(refreshToken, "refreshToken cannot be null");
    }
}
