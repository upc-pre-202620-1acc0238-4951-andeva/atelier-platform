package com.andeva.atelier.platform.shared.application.handlers;

import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Base functional contract for data-read and reporting query handlers.
 *
 * @param <Q> query object type
 * @param <R> resulting data projection type
 * @author Joel Huamani Estefanero
 */
@FunctionalInterface
public interface QueryHandler<Q, R> {

    /**
     * Executes the read logic associated with the query.
     *
     * @param query input query object
     * @return Result containing either the projected data or an ApplicationError
     */
    Result<R, ApplicationError> handle(Q query);
}
