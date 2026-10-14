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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keeps the RBAC catalog, the factory template matrix and the {@code @PreAuthorize} expressions in sync.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Permission Catalog Tests")
class PermissionCatalogTest {

    /**
     * Authorities reserved to platform operators; they are never grantable to workshop roles.
     */
    private static final Set<String> PLATFORM_ONLY_PERMISSIONS = Set.of("billing:plans:write");
    private static final Set<String> PLATFORM_ONLY_ROLES = Set.of("SUPER_ADMIN");

    private static final Pattern AUTHORITY_CALL = Pattern.compile("has(?:Any)?Authority\\(([^)]*)\\)");
    private static final Pattern ROLE_CALL = Pattern.compile("has(?:Any)?Role\\(([^)]*)\\)");

    private static final Set<String> MECHANIC_ONLY = Set.of(
            "operations:tasks:track_time", "operations:tasks:upload_photos", "operations:tasks:complete");
    private static final Set<String> STAFF_ATTENDANCE = Set.of("hr:attendance:clock_in", "hr:attendance:clock_out");
    private static final Set<String> OWNER_ONLY = Set.of(
            "iam:tenants:update", "iam:members:compensate",
            "invoicing:cashflow:export_pdf", "invoicing:fiscal_config:manage",
            "billing:subscriptions:manage_stripe", "billing:invoices:read");

    private static List<String> expressions;

    @BeforeAll
    static void scanControllers() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        expressions = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("com.andeva.atelier.platform")) {
            Class<?> controller = ClassUtils.forName(definition.getBeanClassName(), PermissionCatalogTest.class.getClassLoader());
            PreAuthorize classLevel = controller.getAnnotation(PreAuthorize.class);
            if (classLevel != null) {
                expressions.add(classLevel.value());
            }
            for (Method method : controller.getDeclaredMethods()) {
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                if (annotation != null) {
                    expressions.add(annotation.value());
                }
            }
        }
        assertThat(expressions).as("@PreAuthorize expressions found in REST controllers").hasSizeGreaterThan(100);
    }

    @Test
    @DisplayName("Catalog should contain exactly the tenant authorities required by @PreAuthorize")
    void catalogShouldMatchSecuredEndpoints() {
        Set<String> required = arguments(AUTHORITY_CALL);
        required.removeAll(PLATFORM_ONLY_PERMISSIONS);

        assertThat(new TreeSet<>(PermissionCatalog.names())).containsExactlyInAnyOrderElementsOf(required);
    }

    @Test
    @DisplayName("@PreAuthorize should only reference factory RoleTemplate codes without ROLE_ prefix")
    void securedEndpointsShouldOnlyReferenceTemplateRoles() {
        Set<String> allowed = Arrays.stream(RoleTemplate.values())
                .map(template -> template.code().substring("ROLE_".length()))
                .collect(Collectors.toCollection(HashSet::new));
        allowed.addAll(PLATFORM_ONLY_ROLES);

        assertThat(arguments(ROLE_CALL)).isSubsetOf(allowed);
    }

    @Test
    @DisplayName("Template defaults should only reference catalog permissions")
    void templateDefaultsShouldBelongToCatalog() {
        for (RoleTemplate template : RoleTemplate.values()) {
            assertThat(PermissionCatalog.names()).containsAll(template.defaultPermissionNames());
        }
    }

    @Test
    @DisplayName("Owner should hold every catalog permission except bay execution and attendance clocking")
    void ownerDefaults() {
        Set<String> expected = catalogExcept(MECHANIC_ONLY, STAFF_ATTENDANCE, Set.of());

        assertThat(RoleTemplate.ROLE_WORKSHOP_OWNER.defaultPermissionNames()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Administrator should hold the owner permissions minus governance, fiscal and SaaS billing")
    void administratorDefaults() {
        Set<String> expected = catalogExcept(MECHANIC_ONLY, STAFF_ATTENDANCE, OWNER_ONLY);
        expected.remove("iot:telemetry:ingest");

        assertThat(RoleTemplate.ROLE_WORKSHOP_ADMIN.defaultPermissionNames()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Service advisor defaults")
    void serviceAdvisorDefaults() {
        assertThat(RoleTemplate.ROLE_SERVICE_ADVISOR.defaultPermissionNames()).containsExactlyInAnyOrder(
                "iam:tenants:read", "iam:branches:read",
                "crm:customers:read", "crm:customers:create", "crm:customers:update", "crm:customers:manage",
                "crm:vehicles:read", "crm:vehicles:create", "crm:vehicles:update", "crm:fleets:manage",
                "crm:appointments:read", "crm:appointments:create", "crm:appointments:update", "crm:appointments:manage",
                "operations:work-orders:read", "operations:work-orders:write", "operations:services:read",
                "operations:tasks:read", "operations:tasks:update", "operations:bays:read",
                "inventory:parts:read",
                "hr:attendance:clock_in", "hr:attendance:clock_out", "hr:attendance:read_own", "hr:shifts:read",
                "invoicing:invoices:read",
                "iot:devices:read", "iot:installations:read", "iot:installations:manage", "iot:telemetry:read",
                "iot:faults:read", "iot:alerts:read", "iot:alerts:acknowledge", "iot:alerts:convert",
                "iot:health_reports:generate", "iot:health_reports:read"
        );
    }

    @Test
    @DisplayName("Mechanic defaults")
    void mechanicDefaults() {
        assertThat(RoleTemplate.ROLE_MECHANIC.defaultPermissionNames()).containsExactlyInAnyOrder(
                "iam:tenants:read", "iam:branches:read",
                "crm:vehicles:read",
                "operations:work-orders:read", "operations:services:read", "operations:tasks:read",
                "operations:tasks:track_time", "operations:tasks:upload_photos", "operations:tasks:complete",
                "operations:bays:read",
                "inventory:parts:read", "inventory:batches:dispatch_fifo",
                "hr:attendance:clock_in", "hr:attendance:clock_out", "hr:attendance:read_own", "hr:shifts:read",
                "iot:devices:read", "iot:installations:read", "iot:installations:manage",
                "iot:telemetry:ingest", "iot:telemetry:read",
                "iot:faults:read", "iot:faults:write", "iot:faults:resolve",
                "iot:alerts:read", "iot:alerts:acknowledge",
                "iot:health_reports:generate", "iot:health_reports:read"
        );
    }

    @Test
    @DisplayName("Inventory manager defaults")
    void inventoryManagerDefaults() {
        assertThat(RoleTemplate.ROLE_INVENTORY_MANAGER.defaultPermissionNames()).containsExactlyInAnyOrder(
                "iam:tenants:read", "iam:branches:read",
                "crm:vehicles:read",
                "operations:work-orders:read", "operations:tasks:read",
                "inventory:parts:read", "inventory:parts:manage",
                "inventory:suppliers:read", "inventory:suppliers:manage",
                "inventory:purchase_orders:read", "inventory:purchase_orders:create",
                "inventory:purchase_orders:receive", "inventory:purchase_orders:cancel",
                "inventory:purchase_orders:manage",
                "inventory:batches:read", "inventory:batches:receive", "inventory:batches:dispatch_fifo",
                "inventory:batches:restore", "inventory:batches:manage",
                "inventory:alerts:read", "inventory:alerts:evaluate", "inventory:alerts:resolve",
                "inventory:alerts:manage",
                "hr:attendance:clock_in", "hr:attendance:clock_out", "hr:attendance:read_own", "hr:shifts:read"
        );
    }

    @Test
    @DisplayName("Catalog entries should expose their bounded context as category")
    void entriesShouldExposeCategory() {
        assertThat(PermissionCatalog.entries())
                .allSatisfy(entry -> assertThat(entry.name()).startsWith(entry.category().toLowerCase() + ":"));
    }

    @SafeVarargs
    private static Set<String> catalogExcept(Set<String>... excluded) {
        Set<String> names = new HashSet<>(PermissionCatalog.names());
        for (Set<String> set : excluded) {
            names.removeAll(set);
        }
        return names;
    }

    private static Set<String> arguments(Pattern call) {
        Set<String> values = new TreeSet<>();
        for (String expression : expressions) {
            Matcher matcher = call.matcher(expression);
            while (matcher.find()) {
                for (String argument : matcher.group(1).split(",")) {
                    values.add(argument.trim().replace("'", ""));
                }
            }
        }
        return values;
    }
}
