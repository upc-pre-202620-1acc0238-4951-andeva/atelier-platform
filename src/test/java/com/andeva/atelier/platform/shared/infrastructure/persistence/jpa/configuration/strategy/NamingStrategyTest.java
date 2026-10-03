package com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for {@link SnakeCaseWithPluralizedTablePhysicalNamingStrategy}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Hibernate Physical Naming Strategy Unit Tests")
class NamingStrategyTest {

    private final SnakeCaseWithPluralizedTablePhysicalNamingStrategy strategy =
            new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();

    @ParameterizedTest(name = "Entity {0} should map to table {1}")
    @CsvSource({
            "Tenant, tenants",
            "Vehicle, vehicles",
            "Customer, customers",
            "User, users",
            "WorkOrder, work_orders",
            "Branch, branches",
            "Box, boxes",
            "Tax, taxes",
            "Address, addresses",
            "Category, categories",
            "Company, companies",
            "Quote, quotes",
            "Voucher, vouchers"
    })
    @DisplayName("Should transform entity identifiers to pluralized snake_case table names")
    void shouldTransformEntityToPluralizedSnakeCaseTable(String entityName, String expectedTable) {
        Identifier identifier = Identifier.toIdentifier(entityName);
        Identifier physicalTable = strategy.toPhysicalTableName(identifier, null);

        assertThat(physicalTable).isNotNull();
        assertThat(physicalTable.getText()).isEqualTo(expectedTable);
    }

    @Test
    @DisplayName("Should handle null and other PhysicalNamingStrategy methods")
    void shouldHandleOtherNamingStrategyMethods() {
        assertThat(strategy.toPhysicalCatalogName(Identifier.toIdentifier("cat"), null)).isNull();
        assertThat(strategy.toPhysicalTableName(null, null)).isNull();
        assertThat(strategy.toPhysicalColumnName(null, null)).isNull();
        assertThat(strategy.toPhysicalSchemaName(null, null)).isNull();
        assertThat(strategy.toPhysicalSequenceName(null, null)).isNull();

        Identifier schema = strategy.toPhysicalSchemaName(Identifier.toIdentifier("workshopSchema"), null);
        assertThat(schema.getText()).isEqualTo("workshop_schema");

        Identifier sequence = strategy.toPhysicalSequenceName(Identifier.toIdentifier("orderSeq"), null);
        assertThat(sequence.getText()).isEqualTo("order_seq");
    }

    @Test
    @DisplayName("Should transform camelCase attributes to snake_case column names")
    void shouldTransformAttributeNameToSnakeCase() {
        Identifier camelCaseCol = Identifier.toIdentifier("createdAt");
        Identifier physicalCol = strategy.toPhysicalColumnName(camelCaseCol, null);
        assertThat(physicalCol.getText()).isEqualTo("created_at");

        Identifier taxIdCol = Identifier.toIdentifier("taxId");
        Identifier physicalTaxId = strategy.toPhysicalColumnName(taxIdCol, null);
        assertThat(physicalTaxId.getText()).isEqualTo("tax_id");
    }
}
