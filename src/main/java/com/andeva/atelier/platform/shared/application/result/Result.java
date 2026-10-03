package com.andeva.atelier.platform.shared.application.result;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Sealed functional monad modeling the two deterministic outcomes of a business operation:
 * success carrying a valid value ({@link Success}), or failure carrying an error descriptor ({@link Failure}).
 *
 * @param <T> the type of the success value
 * @param <E> the type of the error object
 * @author Joel Huamani Estefanero
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    /**
     * Represents a successful operation result carrying an optional or concrete payload (e.g. {@code Void} for empty commands).
     *
     * @param <T>   the payload value type
     * @param <E>   the error type
     * @param value the successful result value (maybe null for Void results)
     */
    record Success<T, E>(T value) implements Result<T, E> {
    }

    /**
     * Represents a failed operation result carrying a non-null error descriptor.
     *
     * @param <T>   the expected payload value type
     * @param <E>   the error descriptor type
     * @param error the error cause or descriptor
     */
    record Failure<T, E>(E error) implements Result<T, E> {
        public Failure {
            Objects.requireNonNull(error, "Error object cannot be null");
        }
    }

    // --- Static Factories ---

    /**
     * Creates a successful Result instance wrapping the given value.
     *
     * @param <T>   value type
     * @param <E>   error type
     * @param value success value (can be null for Void commands)
     * @return a successful Result
     */
    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a successful empty Result instance specifically for Void CQRS command handlers.
     *
     * @param <E> error type
     * @return a successful Void Result
     */
    static <E> Result<Void, E> empty() {
        return new Success<>(null);
    }

    /**
     * Creates a failure Result instance wrapping the given non-null error descriptor.
     *
     * @param <T>   value type
     * @param <E>   error type
     * @param error non-null error descriptor
     * @return a failed Result
     */
    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    /**
     * Converts an {@link Optional} into a Result. If the optional is present, returns a Success;
     * otherwise returns a Failure containing the specified fallback error.
     *
     * @param <T>          value type
     * @param <E>          error type
     * @param optional     the optional to evaluate
     * @param errorIfEmpty fallback error if the optional is empty
     * @return a Result instance
     */
    static <T, E> Result<T, E> fromOptional(Optional<T> optional, E errorIfEmpty) {
        Objects.requireNonNull(optional, "Optional cannot be null");
        return optional.<Result<T, E>>map(Result::success)
                .orElseGet(() -> Result.failure(errorIfEmpty));
    }

    // --- Predicates ---

    /**
     * Returns true if this Result represents a success.
     *
     * @return true if success, false otherwise
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }

    /**
     * Returns true if this Result represents a failure.
     *
     * @return true if failure, false otherwise
     */
    default boolean isFailure() {
        return this instanceof Failure;
    }

    // --- Optional Conversions ---

    /**
     * Converts this result to an {@link Optional} containing the value if successful, or empty if failure.
     *
     * @return Optional containing the success value or empty
     */
    default Optional<T> toOptional() {
        if (this instanceof Success<T, E> s) {
            return Optional.ofNullable(s.value());
        }
        return Optional.empty();
    }

    /**
     * Converts this result to an {@link Optional} containing the error if failed, or empty if success.
     *
     * @return Optional containing the error descriptor or empty
     */
    default Optional<E> toErrorOptional() {
        if (this instanceof Failure<T, E> f) {
            return Optional.of(f.error());
        }
        return Optional.empty();
    }

    // --- Monadic Combinators ---

    /**
     * Transforms the success value using the provided mapping function, preserving errors.
     *
     * @param <R>    the transformed value type
     * @param mapper mapping function
     * @return a new Result holding the transformed value or the original error
     */
    @SuppressWarnings("unchecked")
    default <R> Result<R, E> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "Mapping function cannot be null");
        if (this instanceof Success<T, E> s) {
            return Result.success(mapper.apply(s.value()));
        }
        return (Result<R, E>) this;
    }

    /**
     * Chains another operation returning a Result if this result is successful, preserving errors.
     *
     * @param <R>    the transformed value type
     * @param mapper monadic flatMap function
     * @return the result returned by mapper, or the original error
     */
    @SuppressWarnings("unchecked")
    default <R> Result<R, E> flatMap(Function<? super T, Result<R, E>> mapper) {
        Objects.requireNonNull(mapper, "FlatMap function cannot be null");
        if (this instanceof Success<T, E> s) {
            return Objects.requireNonNull(mapper.apply(s.value()), "FlatMap result cannot be null");
        }
        return (Result<R, E>) this;
    }

    /**
     * Transforms the error descriptor using the provided error mapping function, preserving successes.
     *
     * @param <F>         the new error type
     * @param errorMapper mapping function for the error
     * @return a new Result holding the original value or the mapped error
     */
    @SuppressWarnings("unchecked")
    default <F> Result<T, F> mapError(Function<? super E, ? extends F> errorMapper) {
        Objects.requireNonNull(errorMapper, "Error mapping function cannot be null");
        if (this instanceof Failure<T, E> f) {
            return Result.failure(errorMapper.apply(f.error()));
        }
        return (Result<T, F>) this;
    }

    // --- Declarative Side Effects ---

    /**
     * Executes the given consumer if this result is a success.
     *
     * @param action consumer accepting the success value
     * @return this Result for chaining
     */
    default Result<T, E> onSuccess(Consumer<? super T> action) {
        Objects.requireNonNull(action, "onSuccess action cannot be null");
        if (this instanceof Success<T, E> s) {
            action.accept(s.value());
        }
        return this;
    }

    /**
     * Executes the given consumer if this result is a failure.
     *
     * @param action consumer accepting the error descriptor
     * @return this Result for chaining
     */
    default Result<T, E> onFailure(Consumer<? super E> action) {
        Objects.requireNonNull(action, "onFailure action cannot be null");
        if (this instanceof Failure<T, E> f) {
            action.accept(f.error());
        }
        return this;
    }

    // --- Safe Unwrapping ---

    /**
     * Returns the success value if present, otherwise returns the specified default value.
     *
     * @param defaultValue default value fallback
     * @return success value or default value
     */
    default T orElse(T defaultValue) {
        if (this instanceof Success<T, E> s) {
            return s.value();
        }
        return defaultValue;
    }

    /**
     * Returns the success value if present, otherwise computes a fallback using the supplier.
     *
     * @param supplier fallback supplier
     * @return success value or supplier result
     */
    default T orElseGet(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "Supplier cannot be null");
        if (this instanceof Success<T, E> s) {
            return s.value();
        }
        return supplier.get();
    }

    /**
     * Returns the success value if present, otherwise throws the exception produced by exceptionSupplier.
     *
     * @param <X>               exception type to throw
     * @param exceptionSupplier function mapping the error to an exception
     * @return the success value
     * @throws X if this Result is a failure
     */
    default <X extends Throwable> T orElseThrow(Function<? super E, X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier, "Exception supplier function cannot be null");
        if (this instanceof Success<T, E> s) {
            return s.value();
        }
        var failure = (Failure<T, E>) this;
        throw exceptionSupplier.apply(failure.error());
    }
}
