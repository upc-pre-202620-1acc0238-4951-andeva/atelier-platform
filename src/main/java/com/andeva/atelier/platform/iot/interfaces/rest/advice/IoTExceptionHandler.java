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
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Controller advice translating IoT Telemetry & Predictive Maintenance domain exceptions
 * into standardized RFC 7807 ProblemDetail responses with dynamic internationalization (i18n).
 *
 * @author Joel Huamani Estefanero
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.andeva.atelier.platform.iot.interfaces.rest")
public class IoTExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(IoTExceptionHandler.class);
    private static final String BASE_TYPE_URL = "https://api.atelier.andeva.com/errors/";
    private static final String MESSAGES_BASENAME = "messages";

    private final MessageSource messageSource;

    public IoTExceptionHandler() {
        this(null);
    }

    @Autowired
    public IoTExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleDeviceNotFound(DeviceNotFoundException ex, HttpServletRequest request) {
        log.warn("OBD-II device not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "device-not-found", "Device Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(InvalidDeviceIdentifierException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDeviceIdentifier(InvalidDeviceIdentifierException ex, HttpServletRequest request) {
        log.warn("Invalid or conflicting device identifier: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "device-already-exists", "Device Identifier Conflict", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(DeviceAlreadyInstalledException.class)
    public ResponseEntity<ProblemDetail> handleDeviceAlreadyInstalled(DeviceAlreadyInstalledException ex, HttpServletRequest request) {
        log.warn("Device already installed conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "device-already-installed", "Device Currently Installed", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(InstallationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleInstallationNotFound(InstallationNotFoundException ex, HttpServletRequest request) {
        log.warn("Device installation session not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "installation-not-found", "Installation Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(ActiveInstallationConflictException.class)
    public ResponseEntity<ProblemDetail> handleActiveInstallationConflict(ActiveInstallationConflictException ex, HttpServletRequest request) {
        log.warn("Active installation conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "active-installation-conflict", "Active Installation Conflict", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(InvalidDtcCodeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDtcCode(InvalidDtcCodeException ex, HttpServletRequest request) {
        log.warn("Invalid DTC code format: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "invalid-dtc-code", "Invalid DTC Format", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(VehicleFaultNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleVehicleFaultNotFound(VehicleFaultNotFoundException ex, HttpServletRequest request) {
        log.warn("Vehicle fault not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "fault-not-found", "Vehicle Fault Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(PredictiveAlertNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePredictiveAlertNotFound(PredictiveAlertNotFoundException ex, HttpServletRequest request) {
        log.warn("Predictive alert not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "alert-not-found", "Predictive Alert Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(TelemetryNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTelemetryNotFound(TelemetryNotFoundException ex, HttpServletRequest request) {
        log.warn("Telemetry records not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "telemetry-not-found", "Telemetry Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(VehicleHealthReportNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleVehicleHealthReportNotFound(VehicleHealthReportNotFoundException ex, HttpServletRequest request) {
        log.warn("Vehicle health report not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "health-report-not-found", "Health Report Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(TimescaleIngestionException.class)
    public ResponseEntity<ProblemDetail> handleTimescaleIngestion(TimescaleIngestionException ex, HttpServletRequest request) {
        log.error("TimescaleDB ingestion error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "timescale-ingestion-error", "TimescaleDB Ingestion Failed", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ProblemDetail> handleQuotaExceeded(QuotaExceededException ex, HttpServletRequest request) {
        log.warn("IoT subscription quota exceeded: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "quota-exceeded", "Quota Exceeded", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on IoT endpoint: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "access-denied", "Access Denied", ex.getMessage(), "ACCESS_DENIED", request);
    }

    @ExceptionHandler(IoTDomainException.class)
    public ResponseEntity<ProblemDetail> handleGenericIoTDomain(IoTDomainException ex, HttpServletRequest request) {
        log.warn("Generic IoT domain exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "iot-domain-error", "IoT Domain Error", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failure in IoT REST request: {} errors", ex.getBindingResult().getErrorCount());
        String detailMessage = resolveMessageOrDefault("VALIDATION_FAILED", "Validation failed for one or more fields");
        String title = resolveTitleOrDefault("validation-failed", "Validation Failed");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detailMessage);
        problem.setType(URI.create(BASE_TYPE_URL + "validation-failed"));
        problem.setTitle(title);
        problem.setProperty("errorCode", "VALIDATION_FAILED");
        problem.setProperty("code", "VALIDATION_FAILED");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("invalidFields", ex.getBindingResult().getFieldErrors().stream()
                .map(f -> java.util.Map.of("field", f.getField(), "message", f.getDefaultMessage() != null ? f.getDefaultMessage() : ""))
                .toList());
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request body: {}", ex.getMessage());
        String defaultMsg = "Malformed or unreadable JSON payload";
        String resolvedMsg = resolveMessageOrDefault("MALFORMED_REQUEST", defaultMsg);
        return buildResponse(HttpStatus.BAD_REQUEST, "malformed-request", "Malformed Request", resolvedMsg, "MALFORMED_REQUEST", request);
    }

    private ResponseEntity<ProblemDetail> buildResponse(
            HttpStatus status,
            String typeSuffix,
            String defaultTitle,
            String rawMessage,
            String errorCode,
            HttpServletRequest request
    ) {
        String detailMessage = resolveMessageOrDefault(errorCode, rawMessage);
        String title = resolveTitleOrDefault(typeSuffix, defaultTitle);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detailMessage);
        problem.setType(URI.create(BASE_TYPE_URL + typeSuffix));
        problem.setTitle(title);
        problem.setProperty("errorCode", errorCode != null ? errorCode : "IOT_ERROR");
        problem.setProperty("code", errorCode != null ? errorCode : "IOT_ERROR");
        problem.setProperty("timestamp", Instant.now());
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }

        return ResponseEntity.status(status).body(problem);
    }

    private String resolveTitleOrDefault(String typeSuffix, String defaultTitle) {
        if (typeSuffix == null) return defaultTitle;
        String key = "title." + typeSuffix.replace('-', '_');
        return resolveMessageOrDefault(key, defaultTitle);
    }

    private String resolveMessageOrDefault(String errorCode, String defaultMessage, Object... args) {
        if (errorCode == null || errorCode.isBlank()) {
            return defaultMessage;
        }

        Locale currentLocale = LocaleContextHolder.getLocale();
        String codeLower = errorCode.toLowerCase(Locale.ROOT);
        List<String> candidates = List.of(
                errorCode,
                "error.iot." + codeLower,
                "error.domain." + codeLower,
                "error." + codeLower.replace('_', '.')
        );

        if (messageSource != null) {
            for (String key : candidates) {
                try {
                    return messageSource.getMessage(key, args, currentLocale);
                } catch (Exception ignored) {
                }
            }
        }

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, currentLocale);
            for (String key : candidates) {
                if (bundle.containsKey(key)) {
                    return MessageFormat.format(bundle.getString(key), args);
                }
            }
        } catch (MissingResourceException ignored) {
            // Fallback to default message
        }

        return defaultMessage;
    }
}
