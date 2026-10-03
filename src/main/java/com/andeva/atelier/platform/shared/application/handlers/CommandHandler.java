package com.andeva.atelier.platform.shared.application.handlers;

import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Base functional contract for transactional command handlers that produce an output entity or identifier.
 *
 * @param <C> command object type
 * @param <R> return payload type
 * @author Joel Huamani Estefanero
 */
@FunctionalInterface
public interface CommandHandler<C, R> {

    /**
     * Executes the business logic associated with the command.
     *
     * @param command input command object
     * @return Result containing either the produced value or an ApplicationError
     */
    Result<R, ApplicationError> handle(C command);
}
