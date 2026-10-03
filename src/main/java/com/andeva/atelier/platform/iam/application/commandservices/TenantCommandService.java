package com.andeva.atelier.platform.iam.application.commandservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateTenantCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateTenantProfileCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Public command service interface orchestrating workshop Tenant lifecycle mutations.
 *
 * @author Joel Huamani Estefanero
 */
public interface TenantCommandService {

    Result<Tenant, ApplicationError> handle(CreateTenantCommand command);

    Result<Tenant, ApplicationError> handle(UpdateTenantProfileCommand command);
}
