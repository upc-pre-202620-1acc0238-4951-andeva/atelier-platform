package com.andeva.atelier.platform.hr.interfaces.rest.transform;

import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.commands.CreateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.CreateWorkShiftResource;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.UpdateWorkShiftResource;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.WorkShiftResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;

public final class WorkShiftResourceAssembler {

    private WorkShiftResourceAssembler() {}

    public static CreateWorkShiftCommand toCommand(TenantId tenantId, CreateWorkShiftResource resource) {
        return new CreateWorkShiftCommand(
                tenantId,
                resource.name(),
                resource.startTime(),
                resource.endTime(),
                resource.gracePeriodMinutes()
        );
    }

    public static UpdateWorkShiftCommand toCommand(TenantId tenantId, WorkShiftId shiftId, UpdateWorkShiftResource resource) {
        return new UpdateWorkShiftCommand(
                tenantId,
                shiftId,
                resource.name(),
                resource.startTime(),
                resource.endTime(),
                resource.gracePeriodMinutes()
        );
    }

    public static WorkShiftResource toResource(WorkShift domain) {
        if (domain == null) return null;
        return new WorkShiftResource(
                domain.getId().value(),
                domain.getName(),
                domain.getSchedule().startTime(),
                domain.getSchedule().endTime(),
                domain.getGracePeriod().minutes(),
                domain.isActive()
        );
    }

    public static List<WorkShiftResource> toResourceList(List<WorkShift> list) {
        if (list == null) return List.of();
        return list.stream().map(WorkShiftResourceAssembler::toResource).toList();
    }
}
