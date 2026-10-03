package com.andeva.atelier.platform.shared.application.handlers;

import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Base functional contract for mutational command handlers that do not return a substantial payload.
 *
 * @param <C> command object type
 * @author Joel Huamani Estefanero
 */
@FunctionalInterface
public interface VoidCommandHandler<C> {

    /**
     * Executes the business logic associated with the void command.
     *
     * @param command input command object
     * @return Result containing either Void (success) or an ApplicationError
     */
    Result<Void, ApplicationError> handle(C command);
}
