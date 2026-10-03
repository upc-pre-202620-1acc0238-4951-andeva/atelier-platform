package com.andeva.atelier.platform.shared.application.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for {@link ApplicationError} record and its semantic factory methods.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("ApplicationError Unit Tests")
class ApplicationErrorTest {

    @Test
    @DisplayName("Should enforce invariants on compact constructor")
    void shouldEnforceInvariantsOnConstructor() {
        assertThatThrownBy(() -> new ApplicationError(null, "Message", List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Error code cannot be null");

        assertThatThrownBy(() -> new ApplicationError("CODE", null, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Error message cannot be null");

        // Null details should be defaulted to empty list
        ApplicationError error = new ApplicationError("CODE", "Message", null);
        assertThat(error.details()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Should ensure immutability and defensive copying of details list")
    void shouldEnsureImmutabilityAndDefensiveCopyOfDetails() {
        List<String> mutableList = new java.util.ArrayList<>(List.of("Detail 1"));
        ApplicationError error = new ApplicationError("CODE", "Message", mutableList);

        // Modifying external mutable list should not mutate error.details()
        mutableList.add("Detail 2");
        assertThat(error.details()).containsExactly("Detail 1");

        // Details collection must be strictly unmodifiable
        assertThatThrownBy(() -> error.details().add("Detail 3"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should create NOT_FOUND error with resource and identifier")
    void shouldCreateNotFoundWithResourceAndId() {
        UUID id = UUID.randomUUID();
        ApplicationError error = ApplicationError.notFound("WorkOrder", id);

        assertThat(error.code()).isEqualTo("NOT_FOUND");
        assertThat(error.message()).isEqualTo(String.format("WorkOrder with identifier %s was not found", id));
        assertThat(error.details()).isEmpty();
    }

    @Test
    @DisplayName("Should create NOT_FOUND error with custom message")
    void shouldCreateNotFoundWithMessage() {
        ApplicationError error = ApplicationError.notFound("Customer account not located");

        assertThat(error.code()).isEqualTo("NOT_FOUND");
        assertThat(error.message()).isEqualTo("Customer account not located");
        assertThat(error.details()).isEmpty();
    }

    @Test
    @DisplayName("Should create CONFLICT error")
    void shouldCreateConflictError() {
        ApplicationError error = ApplicationError.conflict("TaxId already registered in active tenant");

        assertThat(error.code()).isEqualTo("CONFLICT");
        assertThat(error.message()).isEqualTo("TaxId already registered in active tenant");
        assertThat(error.details()).isEmpty();
    }

    @Test
    @DisplayName("Should create BAD_REQUEST error without details")
    void shouldCreateBadRequestWithoutDetails() {
        ApplicationError error = ApplicationError.badRequest("Invalid input parameters");

        assertThat(error.code()).isEqualTo("BAD_REQUEST");
        assertThat(error.message()).isEqualTo("Invalid input parameters");
        assertThat(error.details()).isEmpty();
    }

    @Test
    @DisplayName("Should create BAD_REQUEST error with details")
    void shouldCreateBadRequestWithDetails() {
        List<String> details = List.of("field 'email' is invalid", "field 'amount' must be positive");
        ApplicationError error = ApplicationError.badRequest("Validation failed", details);

        assertThat(error.code()).isEqualTo("BAD_REQUEST");
        assertThat(error.message()).isEqualTo("Validation failed");
        assertThat(error.details()).containsExactlyElementsOf(details);
    }

    @Test
    @DisplayName("Should create UNAUTHORIZED error")
    void shouldCreateUnauthorizedError() {
        ApplicationError error = ApplicationError.unauthorized("Expired authentication token");

        assertThat(error.code()).isEqualTo("UNAUTHORIZED");
        assertThat(error.message()).isEqualTo("Expired authentication token");
        assertThat(error.details()).isEmpty();
    }

    @Test
    @DisplayName("Should create FORBIDDEN error")
    void shouldCreateForbiddenError() {
        ApplicationError error = ApplicationError.forbidden("User lacks required permission");

        assertThat(error.code()).isEqualTo("FORBIDDEN");
        assertThat(error.message()).isEqualTo("User lacks required permission");
        assertThat(error.details()).isEmpty();
    }

    @Test
    @DisplayName("Should create UNPROCESSABLE_ENTITY error")
    void shouldCreateUnprocessableEntityError() {
        ApplicationError error = ApplicationError.unprocessableEntity("Cannot transition completed order");

        assertThat(error.code()).isEqualTo("UNPROCESSABLE_ENTITY");
        assertThat(error.message()).isEqualTo("Cannot transition completed order");
        assertThat(error.details()).isEmpty();
    }

    @Test
    @DisplayName("Should create INTERNAL_ERROR error")
    void shouldCreateInternalError() {
        ApplicationError error = ApplicationError.internalError("Database connection timed out");

        assertThat(error.code()).isEqualTo("INTERNAL_ERROR");
        assertThat(error.message()).isEqualTo("Database connection timed out");
        assertThat(error.details()).isEmpty();
    }
}
