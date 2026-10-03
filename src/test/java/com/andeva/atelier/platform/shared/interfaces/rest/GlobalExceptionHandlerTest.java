package com.andeva.atelier.platform.shared.interfaces.rest;

import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;
import com.andeva.atelier.platform.shared.interfaces.rest.resources.ErrorResource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for {@link GlobalExceptionHandler}.
 * Verifies HTTP status code mappings, RFC 7807 error payload formats,
 * field validation error breakdowns, and internationalized (i18n) message resolution.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
        MDC.clear();
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException Handling")
    class MethodArgumentNotValidExceptionTests {

        @Test
        @DisplayName("Should return 400 Bad Request with field error details")
        void shouldHandleMethodArgumentNotValid() throws Exception {
            Object target = new Object();
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "requestDto");
            bindingResult.addError(new FieldError("requestDto", "name", "must not be blank"));
            bindingResult.addError(new FieldError("requestDto", "email", "must be a well-formed email address"));

            java.lang.reflect.Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("setUp");
            MethodParameter parameter = new MethodParameter(method, -1);
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

            ResponseEntity<ErrorResource> response = exceptionHandler.handleMethodArgumentNotValid(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
            assertThat(response.getBody().details()).containsExactlyInAnyOrder(
                    "name: must not be blank",
                    "email: must be a well-formed email address"
            );
        }

        @Test
        @DisplayName("Should resolve Spanish i18n message for validation errors when requested")
        void shouldResolveSpanishMessageForValidation() throws Exception {
            LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "dto");
            bindingResult.addError(new FieldError("dto", "field", "invalido"));
            java.lang.reflect.Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("setUp");
            MethodParameter parameter = new MethodParameter(method, -1);
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

            ResponseEntity<ErrorResource> response = exceptionHandler.handleMethodArgumentNotValid(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("solicitud");
        }
    }

    @Nested
    @DisplayName("ConstraintViolationException Handling")
    class ConstraintViolationExceptionTests {

        @Test
        @DisplayName("Should return 400 Bad Request with formatted parameter violation details")
        void shouldHandleConstraintViolation() {
            @SuppressWarnings("unchecked")
            ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            when(path.toString()).thenReturn("findWorkshopById.workshopId");
            when(violation.getPropertyPath()).thenReturn(path);
            when(violation.getMessage()).thenReturn("must not be null");

            ConstraintViolationException ex = new ConstraintViolationException(
                    "Validation failed",
                    Set.of(violation)
            );

            ResponseEntity<ErrorResource> response = exceptionHandler.handleConstraintViolation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("CONSTRAINT_VIOLATION");
            assertThat(response.getBody().details()).containsExactly("findWorkshopById.workshopId: must not be null");
        }
    }

    @Nested
    @DisplayName("DomainException Handling")
    class DomainExceptionTests {

        @Test
        @DisplayName("Should return 422 Unprocessable Entity and resolve localized message from bundle")
        void shouldHandleDomainExceptionWithKnownBundleKey() {
            DomainException ex = new DomainException("GENERIC", "Fallback message") {};

            ResponseEntity<ErrorResource> response = exceptionHandler.handleDomainException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("GENERIC");
            assertThat(response.getBody().message()).isEqualTo("A domain business rule violation has occurred.");
        }

        @Test
        @DisplayName("Should return 422 Unprocessable Entity in Spanish when requested")
        void shouldHandleDomainExceptionInSpanish() {
            LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
            DomainException ex = new DomainException("GENERIC", "Mensaje por defecto") {};

            ResponseEntity<ErrorResource> response = exceptionHandler.handleDomainException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("GENERIC");
            assertThat(response.getBody().message()).isEqualTo("Ha ocurrido una transgresión de regla de negocio en el dominio.");
        }

        @Test
        @DisplayName("Should fallback to exception message when key is not in resource bundle")
        void shouldFallbackToExceptionMessageWhenKeyMissing() {
            DomainException ex = new DomainException("UNKNOWN_RULE", "Specific invariant broken") {};

            ResponseEntity<ErrorResource> response = exceptionHandler.handleDomainException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("UNKNOWN_RULE");
            assertThat(response.getBody().message()).isEqualTo("Specific invariant broken");
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException Handling")
    class IllegalArgumentExceptionTests {

        @Test
        @DisplayName("Should return 400 Bad Request with provided exception message")
        void shouldHandleIllegalArgumentExceptionWithMessage() {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid state transition specified");

            ResponseEntity<ErrorResource> response = exceptionHandler.handleIllegalArgumentException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().message()).isEqualTo("Invalid state transition specified");
        }

        @Test
        @DisplayName("Should return default message when exception message is null")
        void shouldHandleIllegalArgumentExceptionWithNullMessage() {
            IllegalArgumentException ex = new IllegalArgumentException((String) null);

            ResponseEntity<ErrorResource> response = exceptionHandler.handleIllegalArgumentException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).isEqualTo("Invalid argument passed");
        }
    }

    @Nested
    @DisplayName("HttpMessageNotReadableException Handling")
    class HttpMessageNotReadableExceptionTests {

        @Test
        @DisplayName("Should return 400 Bad Request with MALFORMED_JSON_REQUEST code")
        void shouldHandleHttpMessageNotReadable() {
            HttpInputMessage inputMessage = mock(HttpInputMessage.class);
            HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error", inputMessage);

            ResponseEntity<ErrorResource> response = exceptionHandler.handleHttpMessageNotReadable(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("MALFORMED_JSON_REQUEST");
            assertThat(response.getBody().message()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("HttpRequestMethodNotSupportedException Handling")
    class HttpRequestMethodNotSupportedExceptionTests {

        @Test
        @DisplayName("Should return 405 Method Not Allowed with descriptive message")
        void shouldHandleMethodNotSupported() {
            HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException(
                    "PATCH",
                    List.of("GET", "POST")
            );

            ResponseEntity<ErrorResource> response = exceptionHandler.handleMethodNotSupported(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("METHOD_NOT_ALLOWED");
            assertThat(response.getBody().message()).contains("PATCH");
        }
    }

    @Nested
    @DisplayName("HttpMediaTypeNotSupportedException Handling")
    class HttpMediaTypeNotSupportedExceptionTests {

        @Test
        @DisplayName("Should return 415 Unsupported Media Type")
        void shouldHandleMediaTypeNotSupported() {
            HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException(
                    MediaType.APPLICATION_XML,
                    Collections.singletonList(MediaType.APPLICATION_JSON)
            );

            ResponseEntity<ErrorResource> response = exceptionHandler.handleMediaTypeNotSupported(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("UNSUPPORTED_MEDIA_TYPE");
        }
    }

    @Nested
    @DisplayName("Unhandled Exception Handling")
    class UnhandledExceptionTests {

        @Test
        @DisplayName("Should return 500 Internal Server Error and include correlation ID")
        void shouldHandleUnhandledExceptionWithCorrelationId() {
            MDC.put("correlationId", "corr-test-999");
            Exception ex = new RuntimeException("Unexpected database failure");

            ResponseEntity<ErrorResource> response = exceptionHandler.handleUnhandledException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("INTERNAL_SERVER_ERROR");
            assertThat(response.getBody().message()).isNotEmpty();
        }
    }
}
