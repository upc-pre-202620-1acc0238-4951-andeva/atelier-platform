package com.andeva.atelier.platform.iam.domain.model.valueobjects;

import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keeps the RBAC catalog, the factory role matrix and the {@code @PreAuthorize} expressions in sync with
 * {@code docs/backend-documentation/atelier-roles.md}, the source of truth for roles and permissions.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Permission Catalog Tests")
class PermissionCatalogTest {

    private static final Path ROLES_DOC = Path.of("docs", "backend-documentation", "atelier-roles.md");

    /**
     * Authority reserved to platform operators; it is never grantable to workshop roles.
     */
    private static final String PLATFORM_ONLY_PERMISSION = "billing:plans:write";
    /**
     * Customer self-service endpoint scoped to the caller, the only one that just requires authentication.
     */
    private static final String SELF_SERVICE_ENDPOINT = "VehiclesController.getMyVehicles";

    private static final Pattern PERMISSION_ROW = Pattern.compile("^\\| `([a-z_]+:[a-z_]+:[a-z_]+)`\\s*\\|(.*)$", Pattern.MULTILINE);
    private static final Pattern AUTHORITY_ONLY = Pattern.compile(
            "has(?:Any)?Authority\\('[a-z_]+:[a-z_]+:[a-z_]+'(?:, '[a-z_]+:[a-z_]+:[a-z_]+')*\\)");
    private static final Pattern PERMISSION_LITERAL = Pattern.compile("'([a-z_]+:[a-z_]+:[a-z_]+)'");
    private static final List<RoleTemplate> MATRIX_COLUMNS = List.of(
            RoleTemplate.ROLE_WORKSHOP_OWNER, RoleTemplate.ROLE_WORKSHOP_ADMINISTRATOR, RoleTemplate.ROLE_CHIEF_MECHANIC,
            RoleTemplate.ROLE_SERVICE_ADVISOR, RoleTemplate.ROLE_RECEPTIONIST, RoleTemplate.ROLE_MECHANIC,
            RoleTemplate.ROLE_INVENTORY_MANAGER, RoleTemplate.ROLE_CASHIER);

    private static Map<String, String> expressions;
    private static Map<String, String> documentedEndpoints;
    private static Map<RoleTemplate, Set<String>> documentedMatrix;

    @BeforeAll
    static void loadSources() throws ClassNotFoundException, IOException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        expressions = new LinkedHashMap<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("com.andeva.atelier.platform")) {
            Class<?> controller = ClassUtils.forName(definition.getBeanClassName(), PermissionCatalogTest.class.getClassLoader());
            for (Method method : controller.getDeclaredMethods()) {
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                if (annotation != null) {
                    expressions.put(controller.getSimpleName() + "." + method.getName(), annotation.value());
                }
            }
        }
        assertThat(expressions).as("@PreAuthorize expressions found in REST controllers").hasSizeGreaterThan(150);

        String doc = Files.readString(ROLES_DOC, StandardCharsets.UTF_8).replace("\r\n", "\n");
        String catalogSection = doc.substring(doc.indexOf("## 4. "), doc.indexOf("## 5. "));
        String matrixSection = doc.substring(doc.indexOf("## 5. "), doc.indexOf("## 6. "));

        documentedEndpoints = new LinkedHashMap<>();
        Matcher row = PERMISSION_ROW.matcher(catalogSection);
        while (row.find()) {
            String[] cells = row.group(2).trim().split("\\|");
            documentedEndpoints.put(row.group(1), cells[cells.length - 1].trim());
        }

        documentedMatrix = new EnumMap<>(RoleTemplate.class);
        MATRIX_COLUMNS.forEach(template -> documentedMatrix.put(template, new TreeSet<>()));
        row = PERMISSION_ROW.matcher(matrixSection);
        while (row.find()) {
            String[] cells = Arrays.stream(row.group(2).trim().split("\\|")).map(String::trim)
                    .filter(cell -> !cell.isEmpty()).toArray(String[]::new);
            assertThat(cells).as("matrix row of %s", row.group(1)).hasSize(MATRIX_COLUMNS.size());
            for (int i = 0; i < cells.length; i++) {
                if (cells[i].equals("Sí")) {
                    documentedMatrix.get(MATRIX_COLUMNS.get(i)).add(row.group(1));
                }
            }
        }
    }

    @Test
    @DisplayName("Factory templates should be exactly the eight roles of the document")
    void templatesShouldMatchDocumentedRoles() {
        assertThat(Arrays.stream(RoleTemplate.values()).map(RoleTemplate::code)).containsExactly(
                "ROLE_WORKSHOP_OWNER", "ROLE_WORKSHOP_ADMINISTRATOR", "ROLE_CHIEF_MECHANIC", "ROLE_SERVICE_ADVISOR",
                "ROLE_RECEPTIONIST", "ROLE_MECHANIC", "ROLE_INVENTORY_MANAGER", "ROLE_CASHIER");
    }

    @Test
    @DisplayName("Catalog should contain exactly the permissions documented in section 4")
    void catalogShouldMatchDocumentedPermissions() {
        assertThat(PermissionCatalog.names()).containsExactlyInAnyOrderElementsOf(documentedEndpoints.keySet());
    }

    @Test
    @DisplayName("Template defaults should be exactly the matrix of section 5")
    void templateDefaultsShouldMatchDocumentedMatrix() {
        for (RoleTemplate template : RoleTemplate.values()) {
            assertThat(template.defaultPermissionNames())
                    .as("default permissions of %s", template)
                    .containsExactlyInAnyOrderElementsOf(documentedMatrix.get(template));
        }
    }

    @Test
    @DisplayName("Endpoints should authorize exclusively with atomic permissions")
    void endpointsShouldOnlyCheckPermissions() {
        expressions.forEach((endpoint, expression) -> {
            if (endpoint.equals(SELF_SERVICE_ENDPOINT)) {
                assertThat(expression).isEqualTo("isAuthenticated()");
            } else {
                assertThat(expression).as(endpoint).matches(AUTHORITY_ONLY);
            }
        });
    }

    @Test
    @DisplayName("Every permission required by an endpoint should belong to the catalog")
    void requiredPermissionsShouldBelongToCatalog() {
        Set<String> required = requiredPermissions();
        required.remove(PLATFORM_ONLY_PERMISSION);

        assertThat(PermissionCatalog.names()).containsAll(required);
        assertThat(PermissionCatalog.names()).doesNotContain(PLATFORM_ONLY_PERMISSION);
    }

    @Test
    @DisplayName("Permissions without endpoint should be the ones flagged in the document")
    void unusedPermissionsShouldBeFlaggedInDocument() {
        Set<String> required = requiredPermissions();
        Set<String> withoutEndpoint = PermissionCatalog.names().stream()
                .filter(name -> !required.contains(name))
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> flagged = documentedEndpoints.entrySet().stream()
                .filter(entry -> entry.getValue().contains("Sin endpoint implementado")
                        || entry.getValue().contains("públicos"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(withoutEndpoint).containsExactlyInAnyOrderElementsOf(flagged);
    }

    @Test
    @DisplayName("Catalog entries should expose their bounded context as category")
    void entriesShouldExposeCategory() {
        assertThat(PermissionCatalog.entries())
                .allSatisfy(entry -> assertThat(entry.name()).startsWith(entry.category().toLowerCase() + ":"));
    }

    private static Set<String> requiredPermissions() {
        Set<String> required = new HashSet<>();
        for (String expression : expressions.values()) {
            Matcher matcher = PERMISSION_LITERAL.matcher(expression);
            while (matcher.find()) {
                required.add(matcher.group(1));
            }
        }
        return required;
    }
}
