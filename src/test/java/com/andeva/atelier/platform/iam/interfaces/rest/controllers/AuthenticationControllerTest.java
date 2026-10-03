package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.TenantCommandService;
import com.andeva.atelier.platform.iam.application.commandservices.UserCommandService;
import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateWithGoogleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateTenantCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.VerifyEmailTokenCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Profile;
import com.andeva.atelier.platform.iam.domain.model.enums.AuthProvider;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.CreateTenantResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.ForgotPasswordResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.GoogleSignInResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.ResetPasswordResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.SignInResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.VerifyEmailResource;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.CreateTenantCommandFromResourceAssembler;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.interfaces.rest.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test suite for {@link AuthenticationController}.
 * Verifies workshop onboarding, credentials authentication, Google OIDC token exchange,
 * email verification, and password reset workflows.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Unit Tests")
class AuthenticationControllerTest {

    @Mock
    private TenantCommandService tenantCommandService;

    @Mock
    private UserCommandService userCommandService;

    @Mock
    private TenantQueryService tenantQueryService;

    @Mock
    private CreateTenantCommandFromResourceAssembler createTenantCommandAssembler;

    @Mock
    private BCryptHashingService hashingService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        validator.afterPropertiesSet();

        AuthenticationController controller = new AuthenticationController(
                tenantCommandService,
                userCommandService,
                tenantQueryService,
                createTenantCommandAssembler,
                hashingService,
                messageSource
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/v1/auth/sign-up creates workshop tenant and returns 201 Created")
    void signUpSuccessfully() throws Exception {
        CreateTenantResource resource = new CreateTenantResource(
                "Precision Motors",
                "PRECISION MOTORS SAC",
                "20456789014",
                "admin@precision.pe",
                "StrongP@ssw0rd2026",
                "Carlos",
                "Mendoza",
                "+51987654321"
        );

        CreateTenantCommand command = new CreateTenantCommand(
                "Precision Motors",
                "PRECISION MOTORS SAC",
                TaxId.of("20456789014"),
                EmailAddress.of("admin@precision.pe"),
                Password.of("$2a$12$e80yqZ67G60m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8"),
                PersonName.of("Carlos", "Mendoza"),
                PhoneNumber.of("+51987654321")
        );

        Tenant tenant = new Tenant(
                TenantId.of(UUID.randomUUID()),
                "Precision Motors",
                "PRECISION MOTORS SAC",
                TaxId.of("20456789014"),
                TenantStatus.ACTIVE,
                null,
                Collections.emptyList()
        );

        when(createTenantCommandAssembler.toCommandFromResource(any(CreateTenantResource.class))).thenReturn(command);
        when(tenantCommandService.handle(command)).thenReturn(Result.success(tenant));

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Precision Motors"))
                .andExpect(jsonPath("$.taxId").value("20456789014"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(tenantCommandService).handle(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/sign-in authenticates user and returns 200 OK with token")
    void signInSuccessfully() throws Exception {
        SignInResource resource = new SignInResource("carlos@precision.pe", "Secret123!");

        TenantId tenantId = TenantId.of(UUID.randomUUID());
        User user = User.registerWithLocalCredentials(
                EmailAddress.of("carlos@precision.pe"),
                Password.of("$2a$12$e80yqZ67G60m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8"),
                PersonName.of("Carlos", "Mendoza"),
                PhoneNumber.of("+51987654321")
        );

        Tenant tenant = new Tenant(
                tenantId,
                "Precision Motors",
                "PRECISION MOTORS SAC",
                TaxId.of("20456789014"),
                TenantStatus.ACTIVE,
                null,
                Collections.emptyList()
        );

        AuthenticatedUser authUser = new AuthenticatedUser(
                user,
                "jwt.token.value",
                tenantId,
                Set.of("iam:users:read", "iam:roles:read")
        );

        when(userCommandService.handle(any(AuthenticateUserCommand.class))).thenReturn(Result.success(authUser));
        when(tenantQueryService.handle(new GetTenantByIdQuery(tenantId))).thenReturn(Optional.of(tenant));

        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.id().value().toString()))
                .andExpect(jsonPath("$.email").value("carlos@precision.pe"))
                .andExpect(jsonPath("$.token").value("jwt.token.value"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.activeTenant.id").value(tenantId.value().toString()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/google-sign-in exchanges idToken for 200 OK session")
    void googleSignInSuccessfully() throws Exception {
        GoogleSignInResource resource = new GoogleSignInResource("google.oauth2.idToken");

        User user = User.registerWithGoogle(
                EmailAddress.of("federated@gmail.com"),
                "sub-123456",
                PersonName.of("Google", "User")
        );

        AuthenticatedUser authUser = new AuthenticatedUser(
                user,
                "google.jwt.token",
                null,
                Set.of("crm:vehicles:read")
        );

        when(userCommandService.handle(any(AuthenticateWithGoogleCommand.class))).thenReturn(Result.success(authUser));

        mockMvc.perform(post("/api/v1/auth/google-sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.id().value().toString()))
                .andExpect(jsonPath("$.email").value("federated@gmail.com"))
                .andExpect(jsonPath("$.token").value("google.jwt.token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/verify-email returns 200 OK on token consumption")
    void verifyEmailSuccessfully() throws Exception {
        VerifyEmailResource resource = new VerifyEmailResource("user@atelier.pe", "849201");
        when(userCommandService.handle(any(VerifyEmailTokenCommand.class))).thenReturn(Result.success(null));

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email successfully verified"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password returns 200 OK on dispatching reset token")
    void forgotPasswordSuccessfully() throws Exception {
        ForgotPasswordResource resource = new ForgotPasswordResource("forgot@precision.pe");
        when(userCommandService.handle(any(RequestPasswordResetCommand.class))).thenReturn(Result.success(null));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password returns 200 OK on successful password reset")
    void resetPasswordSuccessfully() throws Exception {
        ResetPasswordResource resource = new ResetPasswordResource("valid-reset-token", "NewSecretPass2026*");
        when(hashingService.hash("NewSecretPass2026*")).thenReturn("$2a$12$e80yqZ67G60m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8");
        when(userCommandService.handle(any(ResetPasswordCommand.class))).thenReturn(Result.success(null));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password successfully reset"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/sign-in returns 401 Unauthorized when credentials invalid")
    void signInFailsWhenInvalidCredentials() throws Exception {
        SignInResource resource = new SignInResource("carlos@precision.pe", "WrongPass");
        when(userCommandService.handle(any(AuthenticateUserCommand.class)))
                .thenReturn(Result.failure(ApplicationError.unauthorized("Invalid credentials")));

        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password returns Spanish message when Accept-Language: es")
    void resetPasswordReturnsSpanishMessage() throws Exception {
        ResetPasswordResource resource = new ResetPasswordResource("valid-reset-token", "NewSecretPass2026*");
        when(hashingService.hash("NewSecretPass2026*")).thenReturn("$2a$12$e80yqZ67G60m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8");
        when(userCommandService.handle(any(ResetPasswordCommand.class))).thenReturn(Result.success(null));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña restablecida exitosamente"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/verify-email returns Spanish message when Accept-Language: es")
    void verifyEmailReturnsSpanishMessage() throws Exception {
        VerifyEmailResource resource = new VerifyEmailResource("user@example.pe", "valid-token");
        when(userCommandService.handle(any(VerifyEmailTokenCommand.class))).thenReturn(Result.success(null));

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Correo electrónico verificado exitosamente"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/sign-in with invalid fields returns localized Spanish validation errors")
    void signInValidationFailsWithSpanishMessages() throws Exception {
        SignInResource resource = new SignInResource("", "");

        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Parámetros o carga útil de la solicitud no válidos."))
                .andExpect(jsonPath("$.details").isArray());
    }
}
