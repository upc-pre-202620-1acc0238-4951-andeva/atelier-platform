package com.andeva.atelier.platform.shared.interfaces.rest.transform;

import com.andeva.atelier.platform.shared.application.pagination.PagedResult;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.interfaces.rest.resources.PagedResultResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Generic utility assembler transforming monadic {@link Result} objects into Spring {@link ResponseEntity} structures.
 * Eliminates repetitive boilerplate and conditional error branches across REST controllers in all bounded contexts.
 *
 * @author Joel Huamani Estefanero
 */
public final class ResponseEntityAssembler {

    private ResponseEntityAssembler() {
    }

    /**
     * Assembles an HTTP response for a single entity from a monadic Result.
     *
     * @param <T>               domain entity or application payload type
     * @param <R>               REST response resource type
     * @param result            monadic Result from application layer
     * @param resourceAssembler mapping function from entity to REST resource
     * @param successStatus     HTTP status code to emit upon success (e.g. OK, CREATED)
     * @return ResponseEntity holding the transformed resource or the structured RFC 7807 ErrorResource
     */
    public static <T, R> ResponseEntity<?> toResponseEntityFromResult(
            Result<T, ApplicationError> result,
            Function<T, R> resourceAssembler,
            HttpStatusCode successStatus) {
        Objects.requireNonNull(result, "Result cannot be null");
        Objects.requireNonNull(resourceAssembler, "Resource assembler function cannot be null");
        Objects.requireNonNull(successStatus, "Status code cannot be null");

        return switch (result) {
            case Result.Success<T, ApplicationError> success ->
                    new ResponseEntity<>(resourceAssembler.apply(success.value()), successStatus);
            case Result.Failure<T, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }

    /**
     * Assembles an HTTP response for an entity collection applying element-by-element mapping.
     *
     * @param <T>           element type in the domain list
     * @param <R>           resource type in the resulting REST list
     * @param result        monadic Result containing the list of elements
     * @param itemAssembler element mapper
     * @param successStatus HTTP status code to emit upon success (e.g. OK)
     * @return ResponseEntity with the list of resources or structured error
     */
    public static <T, R> ResponseEntity<?> toResponseEntityFromListResult(
            Result<List<T>, ApplicationError> result,
            Function<T, R> itemAssembler,
            HttpStatusCode successStatus) {
        Objects.requireNonNull(result, "Result cannot be null");
        Objects.requireNonNull(itemAssembler, "Item assembler function cannot be null");
        Objects.requireNonNull(successStatus, "Status code cannot be null");

        return switch (result) {
            case Result.Success<List<T>, ApplicationError> success -> {
                List<R> items = success.value().stream().map(itemAssembler).toList();
                yield new ResponseEntity<>(items, successStatus);
            }
            case Result.Failure<List<T>, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }

    /**
     * Assembles an HTTP response for paginated query results, transforming items into a {@link PagedResultResource}.
     *
     * @param <T>           element type in the domain page
     * @param <R>           resource type in the resulting page
     * @param result        monadic Result containing the PagedResult
     * @param itemAssembler element mapper
     * @param successStatus HTTP status code to emit upon success (e.g. OK)
     * @return ResponseEntity holding the PagedResultResource or structured error
     */
    public static <T, R> ResponseEntity<?> toResponseEntityFromPagedResult(
            Result<PagedResult<T>, ApplicationError> result,
            Function<T, R> itemAssembler,
            HttpStatusCode successStatus) {
        Objects.requireNonNull(result, "Result cannot be null");
        Objects.requireNonNull(itemAssembler, "Item assembler function cannot be null");
        Objects.requireNonNull(successStatus, "Status code cannot be null");

        return switch (result) {
            case Result.Success<PagedResult<T>, ApplicationError> success -> {
                PagedResult<T> paged = success.value();
                List<R> items = paged.content().stream().map(itemAssembler).toList();
                PagedResultResource<R> resource = PagedResultResource.of(
                        items,
                        paged.page(),
                        paged.size(),
                        paged.totalElements()
                );
                yield new ResponseEntity<>(resource, successStatus);
            }
            case Result.Failure<PagedResult<T>, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }

    /**
     * Assembles an HTTP response for void command operations (e.g. logical deletions or updates without payload).
     *
     * @param result        monadic Result of type Void
     * @param successStatus HTTP status code to emit upon success (e.g. NO_CONTENT, OK)
     * @return empty ResponseEntity or structured ErrorResource upon failure
     */
    public static ResponseEntity<?> toResponseEntityFromEmptyResult(
            Result<Void, ApplicationError> result,
            HttpStatusCode successStatus) {
        Objects.requireNonNull(result, "Result cannot be null");
        Objects.requireNonNull(successStatus, "Status code cannot be null");

        return switch (result) {
            case Result.Success<Void, ApplicationError> ignored ->
                    new ResponseEntity<>(successStatus);
            case Result.Failure<Void, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
