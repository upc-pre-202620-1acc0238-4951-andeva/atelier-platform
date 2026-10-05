package com.andeva.atelier.platform.operations.application.commandservices;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.*;
import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface WorkOrderCommandService {

    Result<WorkOrder, ApplicationError> handle(CreateWorkOrderCommand command);

    Result<WorkOrder, ApplicationError> handle(AssignWorkBayCommand command);

    Result<WorkOrder, ApplicationError> handle(ReleaseWorkBayCommand command);

    Result<WorkOrder, ApplicationError> handle(StartWorkOrderCommand command);

    Result<WorkOrder, ApplicationError> handle(AddTaskToWorkOrderCommand command);

    Result<WorkOrder, ApplicationError> handle(AssignTaskMechanicCommand command);

    Result<WorkOrder, ApplicationError> handle(StartWorkOrderTaskCommand command);

    Result<WorkOrder, ApplicationError> handle(HoldWorkOrderTaskCommand command);

    Result<WorkOrder, ApplicationError> handle(ResumeWorkOrderTaskCommand command);

    Result<WorkOrder, ApplicationError> handle(CompleteWorkOrderTaskCommand command);

    Result<WorkOrder, ApplicationError> handle(ReopenWorkOrderTaskCommand command);

    Result<WorkOrder, ApplicationError> handle(AddProductToTaskCommand command);

    Result<WorkOrder, ApplicationError> handle(UpdateTaskProductQuantityCommand command);

    Result<WorkOrder, ApplicationError> handle(RemoveProductFromTaskCommand command);

    Result<WorkOrder, ApplicationError> handle(AttachIntakeImageCommand command);

    Result<WorkOrderTask, ApplicationError> handle(AttachTaskEvidenceImageCommand command);

    Result<TaskProposal, ApplicationError> handle(SubmitTaskProposalCommand command);

    Result<WorkOrderTask, ApplicationError> handle(ApproveTaskProposalCommand command);

    Result<TaskProposal, ApplicationError> handle(RejectTaskProposalCommand command);

    Result<WorkOrder, ApplicationError> handle(MarkWorkOrderAsPaidCommand command);

    Result<WorkOrder, ApplicationError> handle(DeliverVehicleCommand command);

    Result<WorkOrder, ApplicationError> handle(CancelWorkOrderCommand command);
}
