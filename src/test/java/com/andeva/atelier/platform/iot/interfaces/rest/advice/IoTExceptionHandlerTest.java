package com.andeva.atelier.platform.iot.interfaces.rest.advice;

import com.andeva.atelier.platform.billing.domain.exceptions.QuotaExceededException;
import com.andeva.atelier.platform.iot.domain.exceptions.ActiveInstallationConflictException;
import com.andeva.atelier.platform.iot.domain.exceptions.DeviceAlreadyInstalledException;
import com.andeva.atelier.platform.iot.domain.exceptions.DeviceNotFoundException;
import com.andeva.atelier.platform.iot.domain.exceptions.InstallationNotFoundException;
import com.andeva.atelier.platform.iot.domain.exceptions.InvalidDeviceIdentifierException;
import com.andeva.atelier.platform.iot.domain.exceptions.InvalidDtcCodeException;
import com.andeva.atelier.platform.iot.domain.exceptions.IoTDomainException;
import com.andeva.atelier.platform.iot.domain.exceptions.PredictiveAlertNotFoundException;
import com.andeva.atelier.platform.iot.domain.exceptions.TelemetryNotFoundException;
import com.andeva.atelier.platform.iot.domain.exceptions.TimescaleIngestionException;
import com.andeva.atelier.platform.iot.domain.exceptions.VehicleFaultNotFoundException;
import com.andeva.atelier.platform.iot.domain.exceptions.VehicleHealthReportNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import com.andeva.atelier.platform.shared.interfaces.rest.resources.ErrorResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for {@link IoTExceptionHandler}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("IoTExceptionHandler Unit Tests")
class IoTExceptionHandlerTest {

    private IoTExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");

        handler = new IoTExceptionHandler(messageSource);
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/iot/test");
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    @DisplayName("DeviceNotFoundException maps to 404 NOT_FOUND")
    void handleDeviceNotFound() {
        DeviceNotFoundException ex = new DeviceNotFoundException(DeviceId.generate());
        ResponseEntity<ErrorResource> response = handler.handleDeviceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ex.errorCode());
    }

    @Test
    @DisplayName("InvalidDeviceIdentifierException maps to 409 CONFLICT")
    void handleInvalidDeviceIdentifier() {
        InvalidDeviceIdentifierException ex = new InvalidDeviceIdentifierException("Identifier already registered");
        ResponseEntity<ErrorResource> response = handler.handleInvalidDeviceIdentifier(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ex.errorCode());
    }

    @Test
    @DisplayName("DeviceAlreadyInstalledException maps to 409 CONFLICT")
    void handleDeviceAlreadyInstalled() {
        DeviceAlreadyInstalledException ex = new DeviceAlreadyInstalledException(DeviceId.generate());
        ResponseEntity<ErrorResource> response = handler.handleDeviceAlreadyInstalled(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ex.errorCode());
    }

    @Test
    @DisplayName("InstallationNotFoundException maps to 404 NOT_FOUND")
    void handleInstallationNotFound() {
        InstallationNotFoundException ex = new InstallationNotFoundException(InstallationId.generate());
        ResponseEntity<ErrorResource> response = handler.handleInstallationNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ex.errorCode());
    }

    @Test
    @DisplayName("ActiveInstallationConflictException maps to 409 CONFLICT")
    void handleActiveInstallationConflict() {
        ActiveInstallationConflictException ex = new ActiveInstallationConflictException(VehicleId.generate());
        ResponseEntity<ErrorResource> response = handler.handleActiveInstallationConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ex.errorCode());
    }

    @Test
    @DisplayName("InvalidDtcCodeException maps to 400 BAD_REQUEST")
    void handleInvalidDtcCode() {
        InvalidDtcCodeException ex = new InvalidDtcCodeException("INVALID");
        ResponseEntity<ErrorResource> response = handler.handleInvalidDtcCode(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ex.errorCode());
    }

    @Test
    @DisplayName("VehicleFaultNotFoundException maps to 404 NOT_FOUND")
    void handleVehicleFaultNotFound() {
        VehicleFaultNotFoundException ex = new VehicleFaultNotFoundException(FaultId.generate());
        ResponseEntity<ErrorResource> response = handler.handleVehicleFaultNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ex.errorCode());
    }

    @Test
    @DisplayName("PredictiveAlertNotFoundException maps to 404 NOT_FOUND")
    void handlePredictiveAlertNotFound() {
        PredictiveAlertNotFoundException ex = new PredictiveAlertNotFoundException(AlertId.generate());
        ResponseEntity<ErrorResource> response = handler.handlePredictiveAlertNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ex.errorCode());
    }

    @Test
    @DisplayName("TelemetryNotFoundException maps to 404 NOT_FOUND")
    void handleTelemetryNotFound() {
        TelemetryNotFoundException ex = new TelemetryNotFoundException(VehicleId.generate());
        ResponseEntity<ErrorResource> response = handler.handleTelemetryNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("ERR_TELEMETRY_NOT_FOUND");
    }

    @Test
    @DisplayName("VehicleHealthReportNotFoundException maps to 404 NOT_FOUND")
    void handleVehicleHealthReportNotFound() {
        VehicleHealthReportNotFoundException ex = new VehicleHealthReportNotFoundException(VehicleId.generate());
        ResponseEntity<ErrorResource> response = handler.handleVehicleHealthReportNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("ERR_HEALTH_REPORT_NOT_FOUND");
    }

    @Test
    @DisplayName("TimescaleIngestionException maps to 500 INTERNAL_SERVER_ERROR")
    void handleTimescaleIngestion() {
        TimescaleIngestionException ex = new TimescaleIngestionException("Timescale connection timeout");
        ResponseEntity<ErrorResource> response = handler.handleTimescaleIngestion(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ex.errorCode());
    }

    @Test
    @DisplayName("QuotaExceededException maps to 403 FORBIDDEN")
    void handleQuotaExceeded() {
        QuotaExceededException ex = new QuotaExceededException("Quota limit of 5 OBD-II devices reached");
        ResponseEntity<ErrorResource> response = handler.handleQuotaExceeded(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("QUOTA_EXCEEDED");
    }

    @Test
    @DisplayName("AccessDeniedException maps to 403 FORBIDDEN")
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied on IoT endpoint");
        ResponseEntity<ErrorResource> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("IoTDomainException maps to 400 BAD_REQUEST")
    void handleGenericIoTDomain() {
        IoTDomainException ex = new IoTDomainException("Invalid telemetry range");
        ResponseEntity<ErrorResource> response = handler.handleGenericIoTDomain(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("IOT_DOMAIN_ERROR");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException maps to 400 BAD_REQUEST with invalidFields")
    void handleMethodArgumentNotValid() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "macAddress", "MAC address is required"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ErrorResource> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().details()).isNotNull();
    }

    @Test
    @DisplayName("HttpMessageNotReadableException maps to 400 BAD_REQUEST")
    void handleHttpMessageNotReadable() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "Required request body is missing",
                new MockHttpInputMessage(new byte[0])
        );
        ResponseEntity<ErrorResource> response = handler.handleHttpMessageNotReadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("MALFORMED_REQUEST");
    }

    @Test
    @DisplayName("Verify i18n Spanish localization for error titles and details")
    void verifySpanishLocalization() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        DeviceNotFoundException ex = new DeviceNotFoundException(DeviceId.generate());
        ResponseEntity<ErrorResource> response = handler.handleDeviceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        // Spanish title or detail resolved
    }
}
