package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;

public record UpdateWorkBayStatusCommand(
        WorkBayId bayId,
        BayStatus status,
        String reason
) {
}
