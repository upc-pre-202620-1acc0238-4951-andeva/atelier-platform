package com.andeva.atelier.platform.operations.application.queryservices;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface WorkOrderQueryService {

    Optional<WorkOrder> handle(GetWorkOrderByIdQuery query);

    List<WorkOrder> handle(GetWorkOrdersByTenantIdQuery query);

    List<WorkOrder> handle(GetWorkOrdersByBranchIdQuery query);

    List<WorkOrder> handle(GetWorkOrdersByVehicleIdQuery query);

    List<WorkOrder> handle(GetWorkOrdersByCustomerIdQuery query);

    Optional<WorkOrder> handle(GetWorkOrdersByBayIdQuery query);

    List<TaskProposal> handle(GetTaskProposalsByWorkOrderIdQuery query);

    Optional<WorkOrderTask> handle(GetWorkOrderTaskByIdQuery query);
}
