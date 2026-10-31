package com.andeva.atelier.platform.inventory.domain.model.aggregates;

import com.andeva.atelier.platform.inventory.application.commandservices.InventoryItemCommandService;
import com.andeva.atelier.platform.inventory.application.queryservices.InventoryItemQueryService;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemsByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers.InventoryItemPersistenceAssembler;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.InventoryItemPersistenceEntity;
import com.andeva.atelier.platform.inventory.interfaces.rest.controllers.PartsCatalogController;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.PartResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("H35: spare parts record their brand and the catalog filters by it")
class InventoryItemBrandTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());

    private InventoryItem part(String name, String sku, String brand) {
        return InventoryItem.create(tenantId, name, Sku.of(sku), ItemCategory.BRAKES,
                Money.of(new BigDecimal("110.00"), Currency.PEN), Quantity.of(new BigDecimal("2")), "SET", brand);
    }

    @Test
    @DisplayName("The brand is trimmed, a blank brand is no brand and an over-long one is refused")
    void shouldNormalizeBrand() {
        assertThat(part("Pastillas", "BRK-1", "  Bosch ").getBrand()).contains("Bosch");
        assertThat(part("Pastillas", "BRK-2", "   ").getBrand()).isEmpty();
        assertThat(part("Pastillas", "BRK-3", null).getBrand()).isEmpty();
        assertThatThrownBy(() -> part("Pastillas", "BRK-4", "x".repeat(InventoryItem.MAX_BRAND_LENGTH + 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Updating without a brand keeps it; an empty brand clears it")
    void shouldKeepOrClearBrandOnUpdate() {
        InventoryItem item = part("Pastillas", "BRK-1", "Bosch");

        item.updateDetails("Pastillas delanteras", null, null, null, null, null);
        assertThat(item.getBrand()).contains("Bosch");

        item.updateDetails(null, null, null, null, null, "Brembo");
        assertThat(item.getBrand()).contains("Brembo");

        item.updateDetails(null, null, null, null, null, "");
        assertThat(item.getBrand()).isEmpty();
    }

    @Test
    @DisplayName("The brand survives persistence, including an update of a stored row")
    void shouldPersistBrand() {
        InventoryItem item = part("Pastillas", "BRK-1", "Bosch");

        InventoryItemPersistenceEntity entity = InventoryItemPersistenceAssembler.toEntity(item);
        assertThat(entity.getBrand()).isEqualTo("Bosch");
        assertThat(InventoryItemPersistenceAssembler.toDomain(entity).getBrand()).contains("Bosch");

        item.updateDetails(null, null, null, null, null, "Brembo");
        InventoryItemPersistenceAssembler.updateEntity(entity, item);
        assertThat(entity.getBrand()).isEqualTo("Brembo");
    }

    @Test
    @DisplayName("The catalog filters by brand ignoring case, and the text search also matches the brand")
    void shouldFilterCatalogByBrand() {
        InventoryItemQueryService queries = mock(InventoryItemQueryService.class);
        PartsCatalogController controller = new PartsCatalogController(mock(InventoryItemCommandService.class), queries);
        when(queries.handle(any(GetInventoryItemsByTenantIdQuery.class))).thenReturn(List.of(
                part("Pastillas delanteras", "BRK-1", "Bosch"),
                part("Disco de freno", "BRK-2", "Brembo"),
                part("Liquido de frenos", "BRK-3", null)));

        assertThat(skus(controller.getParts(null, tenantId.value(), null, null, "bosch", null, false, 0, 20).getBody()))
                .containsExactly("BRK-1");
        assertThat(skus(controller.getParts(null, tenantId.value(), "brem", null, null, null, false, 0, 20).getBody()))
                .containsExactly("BRK-2");
        assertThat(skus(controller.getParts(null, tenantId.value(), null, null, null, null, false, 0, 20).getBody()))
                .containsExactly("BRK-1", "BRK-2", "BRK-3");
    }

    private static List<String> skus(Object body) {
        return ((List<?>) body).stream().map(p -> ((PartResource) p).sku()).toList();
    }
}
