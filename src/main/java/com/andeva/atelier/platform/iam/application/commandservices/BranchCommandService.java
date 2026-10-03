package com.andeva.atelier.platform.iam.application.commandservices;

import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateBranchCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateBranchLocationCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Public command service interface orchestrating physical workshop Branch operations.
 *
 * @author Joel Huamani Estefanero
 */
public interface BranchCommandService {

    Result<Branch, ApplicationError> handle(CreateBranchCommand command);

    Result<Branch, ApplicationError> handle(UpdateBranchLocationCommand command);
}
