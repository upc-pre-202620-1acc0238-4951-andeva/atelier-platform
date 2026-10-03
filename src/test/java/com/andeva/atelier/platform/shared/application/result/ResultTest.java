package com.andeva.atelier.platform.shared.application.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for {@link Result} sealed functional monad.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Result Monad Unit Tests")
class ResultTest {

    @Nested
    @DisplayName("Creation and Invariant Tests")
    class CreationAndInvariants {

        @Test
        @DisplayName("Should create Success with non-null value")
        void shouldCreateSuccessWithNonNullValue() {
            Result<String, String> result = Result.success("OK");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.toOptional()).contains("OK");
            assertThat(result.toErrorOptional()).isEmpty();
            assertThat(result.orElse("DEFAULT")).isEqualTo("OK");
        }

        @Test
        @DisplayName("Should create Failure with non-null error")
        void shouldCreateFailureWithNonNullError() {
            Result<String, String> result = Result.failure("ERR");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.toOptional()).isEmpty();
            assertThat(result.toErrorOptional()).contains("ERR");
            assertThat(result.orElse("DEFAULT")).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("Should allow null Success value and support Result.empty() for void commands")
        void shouldAllowNullSuccessValueForVoidResults() {
            Result<Void, String> nullSuccess = Result.success(null);
            assertThat(nullSuccess.isSuccess()).isTrue();
            assertThat(nullSuccess.toOptional()).isEmpty();

            Result<Void, String> emptySuccess = Result.empty();
            assertThat(emptySuccess.isSuccess()).isTrue();
            assertThat(emptySuccess.toOptional()).isEmpty();
        }

        @Test
        @DisplayName("Should throw NullPointerException when Failure error is null")
        void shouldThrowWhenFailureErrorIsNull() {
            assertThatThrownBy(() -> Result.failure(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Error object cannot be null");
        }
    }

    @Nested
    @DisplayName("Optional Conversion Tests")
    class FromOptionalTests {

        @Test
        @DisplayName("Should convert present Optional to Success")
        void shouldConvertPresentOptionalToSuccess() {
            Optional<String> present = Optional.of("Hello");
            Result<String, String> result = Result.fromOptional(present, "NOT_FOUND");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.toOptional()).contains("Hello");
        }

        @Test
        @DisplayName("Should convert empty Optional to Failure")
        void shouldConvertEmptyOptionalToFailure() {
            Optional<String> empty = Optional.empty();
            Result<String, String> result = Result.fromOptional(empty, "NOT_FOUND");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.toErrorOptional()).contains("NOT_FOUND");
        }

        @Test
        @DisplayName("Should reject null Optional in fromOptional")
        void shouldRejectNullOptional() {
            assertThatThrownBy(() -> Result.fromOptional(null, "ERR"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Optional cannot be null");
        }
    }

    @Nested
    @DisplayName("Monadic Transformation Tests (map, flatMap, mapError)")
    class MonadicTransformationTests {

        @Test
        @DisplayName("Should map Success value using function")
        void shouldMapSuccessValue() {
            Result<Integer, String> result = Result.success(10);
            Result<String, String> mapped = result.map(i -> "Number: " + i);

            assertThat(mapped.isSuccess()).isTrue();
            assertThat(mapped.toOptional()).contains("Number: 10");
        }

        @Test
        @DisplayName("Should pass Failure unchanged through map")
        void shouldPassFailureUnchangedThroughMap() {
            Result<Integer, String> result = Result.failure("INVALID");
            Result<String, String> mapped = result.map(i -> "Number: " + i);

            assertThat(mapped.isFailure()).isTrue();
            assertThat(mapped.toErrorOptional()).contains("INVALID");
        }

        @Test
        @DisplayName("Should throw NullPointerException if map function is null")
        void shouldThrowWhenMapFunctionIsNull() {
            Result<String, String> result = Result.success("A");
            assertThatThrownBy(() -> result.map(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Mapping function cannot be null");
        }

        @Test
        @DisplayName("Should flatMap Success to new Result")
        void shouldFlatMapSuccessToNewResult() {
            Result<String, String> result = Result.success("123");
            Result<Integer, String> flatMapped = result.flatMap(s -> Result.success(Integer.parseInt(s)));

            assertThat(flatMapped.isSuccess()).isTrue();
            assertThat(flatMapped.toOptional()).contains(123);
        }

        @Test
        @DisplayName("Should flatMap Success to Failure when function returns Failure")
        void shouldFlatMapSuccessToFailure() {
            Result<String, String> result = Result.success("invalid");
            Result<Integer, String> flatMapped = result.flatMap(s -> Result.failure("PARSE_ERROR"));

            assertThat(flatMapped.isFailure()).isTrue();
            assertThat(flatMapped.toErrorOptional()).contains("PARSE_ERROR");
        }

        @Test
        @DisplayName("Should pass Failure unchanged through flatMap")
        void shouldPassFailureThroughFlatMap() {
            Result<String, String> result = Result.failure("INITIAL_ERROR");
            Result<Integer, String> flatMapped = result.flatMap(s -> Result.success(42));

            assertThat(flatMapped.isFailure()).isTrue();
            assertThat(flatMapped.toErrorOptional()).contains("INITIAL_ERROR");
        }

        @Test
        @DisplayName("Should throw NullPointerException if flatMap returns null")
        void shouldThrowWhenFlatMapReturnsNull() {
            Result<String, String> result = Result.success("hello");
            assertThatThrownBy(() -> result.flatMap(s -> null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("FlatMap result cannot be null");
        }

        @Test
        @DisplayName("Should map Failure error using errorMapper")
        void shouldMapFailureError() {
            Result<String, Integer> result = Result.failure(404);
            Result<String, String> mapped = result.mapError(code -> "HTTP_" + code);

            assertThat(mapped.isFailure()).isTrue();
            assertThat(mapped.toErrorOptional()).contains("HTTP_404");
        }

        @Test
        @DisplayName("Should pass Success unchanged through mapError")
        void shouldPassSuccessThroughMapError() {
            Result<String, Integer> result = Result.success("OK");
            Result<String, String> mapped = result.mapError(code -> "HTTP_" + code);

            assertThat(mapped.isSuccess()).isTrue();
            assertThat(mapped.toOptional()).contains("OK");
        }
    }

    @Nested
    @DisplayName("Declarative Side Effect Tests (onSuccess, onFailure)")
    class SideEffectTests {

        @Test
        @DisplayName("Should execute onSuccess when Result is Success")
        void shouldExecuteOnSuccess() {
            AtomicReference<String> captured = new AtomicReference<>();
            Result<String, String> result = Result.success("hello");

            Result<String, String> returned = result.onSuccess(captured::set);

            assertThat(captured.get()).isEqualTo("hello");
            assertThat(returned).isSameAs(result);
        }

        @Test
        @DisplayName("Should not execute onSuccess when Result is Failure")
        void shouldNotExecuteOnSuccessWhenFailure() {
            AtomicBoolean executed = new AtomicBoolean(false);
            Result<String, String> result = Result.failure("err");

            result.onSuccess(v -> executed.set(true));

            assertThat(executed.get()).isFalse();
        }

        @Test
        @DisplayName("Should execute onFailure when Result is Failure")
        void shouldExecuteOnFailure() {
            AtomicReference<String> captured = new AtomicReference<>();
            Result<String, String> result = Result.failure("err_code");

            Result<String, String> returned = result.onFailure(captured::set);

            assertThat(captured.get()).isEqualTo("err_code");
            assertThat(returned).isSameAs(result);
        }

        @Test
        @DisplayName("Should not execute onFailure when Result is Success")
        void shouldNotExecuteOnFailureWhenSuccess() {
            AtomicBoolean executed = new AtomicBoolean(false);
            Result<String, String> result = Result.success("val");

            result.onFailure(e -> executed.set(true));

            assertThat(executed.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Unwrapping Tests (orElse, orElseGet, orElseThrow)")
    class UnwrappingTests {

        @Test
        @DisplayName("Should return value in orElse when Success")
        void shouldReturnValueInOrElse() {
            Result<String, String> result = Result.success("present");
            assertThat(result.orElse("fallback")).isEqualTo("present");
        }

        @Test
        @DisplayName("Should return fallback in orElse when Failure")
        void shouldReturnFallbackInOrElse() {
            Result<String, String> result = Result.failure("missing");
            assertThat(result.orElse("fallback")).isEqualTo("fallback");
        }

        @Test
        @DisplayName("Should return value in orElseGet when Success")
        void shouldReturnValueInOrElseGet() {
            Result<String, String> result = Result.success("computed");
            assertThat(result.orElseGet(() -> "fallback")).isEqualTo("computed");
        }

        @Test
        @DisplayName("Should invoke supplier in orElseGet when Failure")
        void shouldInvokeSupplierInOrElseGet() {
            Result<String, String> result = Result.failure("missing");
            assertThat(result.orElseGet(() -> "fallback")).isEqualTo("fallback");
        }

        @Test
        @DisplayName("Should return value in orElseThrow when Success")
        void shouldReturnValueInOrElseThrow() {
            Result<String, String> result = Result.success("valid");
            assertThat(result.orElseThrow(IllegalStateException::new)).isEqualTo("valid");
        }

        @Test
        @DisplayName("Should throw supplied exception in orElseThrow when Failure")
        void shouldThrowSuppliedExceptionInOrElseThrow() {
            Result<String, String> result = Result.failure("RESOURCE_LOCKED");

            assertThatThrownBy(() -> result.orElseThrow(err -> new IllegalStateException("Failed: " + err)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Failed: RESOURCE_LOCKED");
        }
    }
}
