package com.andeva.atelier.platform.shared.application.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for pagination models: {@link PagedQuery}, {@link PagedResult}, and {@link SortDirection}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Pagination Models Unit Tests")
class PaginationTest {

    @Nested
    @DisplayName("PagedQuery Tests")
    class PagedQueryTests {

        @Test
        @DisplayName("Should create PagedQuery with valid arguments")
        void shouldCreatePagedQueryWithValidArguments() {
            PagedQuery query = new PagedQuery(2, 20, "createdAt", SortDirection.DESC);

            assertThat(query.page()).isEqualTo(2);
            assertThat(query.size()).isEqualTo(20);
            assertThat(query.sortBy()).isEqualTo("createdAt");
            assertThat(query.sortDirection()).isEqualTo(SortDirection.DESC);
        }

        @Test
        @DisplayName("Should apply default values when sortBy or sortDirection are null/blank")
        void shouldApplyDefaults() {
            PagedQuery queryWithNulls = new PagedQuery(0, 10, null, null);
            assertThat(queryWithNulls.sortBy()).isEqualTo("id");
            assertThat(queryWithNulls.sortDirection()).isEqualTo(SortDirection.ASC);

            PagedQuery queryWithBlank = new PagedQuery(0, 10, "   ", null);
            assertThat(queryWithBlank.sortBy()).isEqualTo("id");
            assertThat(queryWithBlank.sortDirection()).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when page is negative")
        void shouldThrowWhenPageIsNegative() {
            assertThatThrownBy(() -> new PagedQuery(-1, 10, "id", SortDirection.ASC))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page index cannot be negative");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when size is zero or negative")
        void shouldThrowWhenSizeIsZeroOrNegative() {
            assertThatThrownBy(() -> new PagedQuery(0, 0, "id", SortDirection.ASC))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page size must be strictly positive");

            assertThatThrownBy(() -> new PagedQuery(0, -5, "id", SortDirection.ASC))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page size must be strictly positive");
        }

        @Test
        @DisplayName("Should create PagedQuery using static factories")
        void shouldCreateUsingStaticFactories() {
            PagedQuery simple = PagedQuery.of(1, 15);
            assertThat(simple.page()).isEqualTo(1);
            assertThat(simple.size()).isEqualTo(15);
            assertThat(simple.sortBy()).isEqualTo("id");
            assertThat(simple.sortDirection()).isEqualTo(SortDirection.ASC);

            PagedQuery explicit = PagedQuery.of(3, 50, "name", SortDirection.DESC);
            assertThat(explicit.page()).isEqualTo(3);
            assertThat(explicit.size()).isEqualTo(50);
            assertThat(explicit.sortBy()).isEqualTo("name");
            assertThat(explicit.sortDirection()).isEqualTo(SortDirection.DESC);
        }
    }

    @Nested
    @DisplayName("PagedResult Tests")
    class PagedResultTests {

        @Test
        @DisplayName("Should create PagedResult and enforce defensive copy")
        void shouldCreatePagedResultAndEnforceDefensiveCopy() {
            List<String> mutableList = new java.util.ArrayList<>(List.of("Item A", "Item B"));
            PagedResult<String> result = new PagedResult<>(mutableList, 0, 10, 2L, 1);

            assertThat(result.content()).containsExactly("Item A", "Item B");
            assertThat(result.page()).isEqualTo(0);
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(2L);
            assertThat(result.totalPages()).isEqualTo(1);

            // Modifying original list should not affect PagedResult content
            mutableList.add("Item C");
            assertThat(result.content()).hasSize(2);

            // Returned list should be unmodifiable
            assertThatThrownBy(() -> result.content().add("Item D"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException when content is null")
        void shouldThrowWhenContentIsNull() {
            assertThatThrownBy(() -> new PagedResult<>(null, 0, 10, 0L, 0))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Page content cannot be null");
        }

        @Test
        @DisplayName("Should validate page, size, and totalElements bounds")
        void shouldValidateBounds() {
            List<String> items = List.of("A");
            assertThatThrownBy(() -> new PagedResult<>(items, -1, 10, 1L, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page index cannot be negative");

            assertThatThrownBy(() -> new PagedResult<>(items, 0, 0, 1L, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page size must be strictly positive");

            assertThatThrownBy(() -> new PagedResult<>(items, 0, 10, -1L, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Total elements cannot be negative");
        }

        @Test
        @DisplayName("Should compute totalPages correctly via of factory")
        void shouldComputeTotalPagesCorrectly() {
            // 0 elements
            PagedResult<String> res0 = PagedResult.of(List.of(), 0, 10, 0L);
            assertThat(res0.totalPages()).isEqualTo(0);

            // 1 element, size 10 -> 1 page
            PagedResult<String> res1 = PagedResult.of(List.of("A"), 0, 10, 1L);
            assertThat(res1.totalPages()).isEqualTo(1);

            // 10 elements, size 10 -> 1 page
            PagedResult<String> res10 = PagedResult.of(List.of("A"), 0, 10, 10L);
            assertThat(res10.totalPages()).isEqualTo(1);

            // 11 elements, size 10 -> 2 pages
            PagedResult<String> res11 = PagedResult.of(List.of("A"), 0, 10, 11L);
            assertThat(res11.totalPages()).isEqualTo(2);

            // 25 elements, size 10 -> 3 pages
            PagedResult<String> res25 = PagedResult.of(List.of("A"), 2, 10, 25L);
            assertThat(res25.totalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should functionally map elements preserving pagination metadata")
        void shouldMapElementsPreservingMetadata() {
            PagedResult<Integer> original = PagedResult.of(List.of(1, 2, 3), 1, 10, 23L);
            PagedResult<String> mapped = original.map(i -> "ID-" + i);

            assertThat(mapped.content()).containsExactly("ID-1", "ID-2", "ID-3");
            assertThat(mapped.page()).isEqualTo(1);
            assertThat(mapped.size()).isEqualTo(10);
            assertThat(mapped.totalElements()).isEqualTo(23L);
            assertThat(mapped.totalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should throw NullPointerException if mapper is null")
        void shouldThrowWhenMapperIsNull() {
            PagedResult<Integer> original = PagedResult.of(List.of(1), 0, 10, 1L);
            assertThatThrownBy(() -> original.map(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Mapping function cannot be null");
        }
    }
}
