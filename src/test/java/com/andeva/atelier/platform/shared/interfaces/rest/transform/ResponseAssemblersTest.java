package com.andeva.atelier.platform.shared.interfaces.rest.transform;

import com.andeva.atelier.platform.shared.application.pagination.PagedResult;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.interfaces.rest.resources.ErrorResource;
import com.andeva.atelier.platform.shared.interfaces.rest.resources.PagedResultResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for {@link ErrorResponseAssembler} and {@link ResponseEntityAssembler}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("REST Response Assemblers Unit Tests")
class ResponseAssemblersTest {

    record SampleDto(String id, String name) {}
    record SampleResource(String id, String name) {}

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Nested
    @DisplayName("ErrorResponseAssembler Tests")
    class ErrorResponseAssemblerTests {

        @Test
        @DisplayName("Should map standard semantic error codes to appropriate HTTP status codes")
        void shouldMapErrorCodesToHttpStatus() {
            assertThat(ErrorResponseAssembler.toStatusFromErrorCode("NOT_FOUND")).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ErrorResponseAssembler.toStatusFromErrorCode("TENANT_NOT_FOUND")).isEqualTo(HttpStatus.NOT_FOUND);

            assertThat(ErrorResponseAssembler.toStatusFromErrorCode("CONFLICT")).isEqualTo(HttpStatus.CONFLICT);
            assertThat(ErrorResponseAssembler.toStatusFromErrorCode("EMAIL_CONFLICT")).isEqualTo(HttpStatus.CONFLICT);

            assertThat(ErrorResponseAssembler.toStatusFromErrorCode("BAD_REQUEST")).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ErrorResponseAssembler.toStatusFromErrorCode("VALIDATION_FAILED")).isEqualTo(HttpStatus.BAD_REQUEST);

            assertThat(ErrorResponseAssembler.toStatusFromErrorCode("UNAUTHORIZED")).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ErrorResponseAssembler.toStatusFromErrorCode("FORBIDDEN")).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(ErrorResponseAssembler.toStatusFromErrorCode("UNPROCESSABLE_ENTITY")).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(ErrorResponseAssembler.toStatusFromErrorCode("UNKNOWN_ERROR")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(ErrorResponseAssembler.toStatusFromErrorCode(null)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Should assemble ResponseEntity with ErrorResource and localized message in English")
        void shouldAssembleResponseWithEnglishLocalization() {
            LocaleContextHolder.setLocale(Locale.ENGLISH);

            ApplicationError error = ApplicationError.badRequest("Invalid input");
            ResponseEntity<ErrorResource> response = ErrorResponseAssembler.toErrorResponseFromApplicationError(error);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().message()).isEqualTo("Invalid request parameters or payload.");
        }

        @Test
        @DisplayName("Should assemble ResponseEntity with ErrorResource and localized message in Spanish")
        void shouldAssembleResponseWithSpanishLocalization() {
            LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

            ApplicationError error = ApplicationError.badRequest("Invalid input");
            ResponseEntity<ErrorResource> response = ErrorResponseAssembler.toErrorResponseFromApplicationError(error);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().message()).contains("Parámetros");
        }

        @Test
        @DisplayName("Should throw NullPointerException when error is null")
        void shouldThrowWhenErrorIsNull() {
            assertThatThrownBy(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("ResponseEntityAssembler Tests")
    class ResponseEntityAssemblerTests {

        @Test
        @DisplayName("toResponseEntityFromResult should return success resource with designated status")
        void toResponseEntityFromResultSuccess() {
            SampleDto dto = new SampleDto("1", "Alpha");
            Result<SampleDto, ApplicationError> result = Result.success(dto);

            ResponseEntity<?> response = ResponseEntityAssembler.toResponseEntityFromResult(
                    result,
                    d -> new SampleResource(d.id(), d.name()),
                    HttpStatus.CREATED
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isInstanceOf(SampleResource.class);
            SampleResource resource = (SampleResource) response.getBody();
            assertThat(resource.name()).isEqualTo("Alpha");
        }

        @Test
        @DisplayName("toResponseEntityFromResult should return ErrorResource upon Failure")
        void toResponseEntityFromResultFailure() {
            Result<SampleDto, ApplicationError> result = Result.failure(ApplicationError.notFound("Resource", "1"));

            ResponseEntity<?> response = ResponseEntityAssembler.toResponseEntityFromResult(
                    result,
                    d -> new SampleResource(d.id(), d.name()),
                    HttpStatus.OK
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isInstanceOf(ErrorResource.class);
        }

        @Test
        @DisplayName("toResponseEntityFromListResult should map element list or return ErrorResource")
        void toResponseEntityFromListResult() {
            List<SampleDto> dtos = List.of(new SampleDto("1", "A"), new SampleDto("2", "B"));
            Result<List<SampleDto>, ApplicationError> success = Result.success(dtos);

            ResponseEntity<?> successResp = ResponseEntityAssembler.toResponseEntityFromListResult(
                    success,
                    d -> new SampleResource(d.id(), d.name()),
                    HttpStatus.OK
            );

            assertThat(successResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(successResp.getBody()).isInstanceOf(List.class);

            Result<List<SampleDto>, ApplicationError> failure = Result.failure(ApplicationError.forbidden("Access denied"));
            ResponseEntity<?> failureResp = ResponseEntityAssembler.toResponseEntityFromListResult(
                    failure,
                    d -> new SampleResource(d.id(), d.name()),
                    HttpStatus.OK
            );

            assertThat(failureResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("toResponseEntityFromPagedResult should return PagedResultResource or ErrorResource")
        @SuppressWarnings("unchecked")
        void toResponseEntityFromPagedResult() {
            PagedResult<SampleDto> pagedResult = PagedResult.of(List.of(new SampleDto("1", "A")), 0, 10, 1L);
            Result<PagedResult<SampleDto>, ApplicationError> success = Result.success(pagedResult);

            ResponseEntity<?> successResp = ResponseEntityAssembler.toResponseEntityFromPagedResult(
                    success,
                    d -> new SampleResource(d.id(), d.name()),
                    HttpStatus.OK
            );

            assertThat(successResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(successResp.getBody()).isInstanceOf(PagedResultResource.class);
            PagedResultResource<SampleResource> body = (PagedResultResource<SampleResource>) successResp.getBody();
            assertThat(body.totalElements()).isEqualTo(1L);
            assertThat(body.items().get(0).name()).isEqualTo("A");

            Result<PagedResult<SampleDto>, ApplicationError> failure = Result.failure(ApplicationError.badRequest("Invalid page"));
            ResponseEntity<?> failureResp = ResponseEntityAssembler.toResponseEntityFromPagedResult(
                    failure,
                    d -> new SampleResource(d.id(), d.name()),
                    HttpStatus.OK
            );
            assertThat(failureResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("toResponseEntityFromEmptyResult should return empty response with status or ErrorResource")
        void toResponseEntityFromEmptyResult() {
            Result<Void, ApplicationError> success = Result.empty();
            ResponseEntity<?> successResp = ResponseEntityAssembler.toResponseEntityFromEmptyResult(success, HttpStatus.NO_CONTENT);

            assertThat(successResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(successResp.getBody()).isNull();

            Result<Void, ApplicationError> failure = Result.failure(ApplicationError.conflict("Cannot delete active item"));
            ResponseEntity<?> failureResp = ResponseEntityAssembler.toResponseEntityFromEmptyResult(failure, HttpStatus.NO_CONTENT);

            assertThat(failureResp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(failureResp.getBody()).isInstanceOf(ErrorResource.class);
        }
    }
}
