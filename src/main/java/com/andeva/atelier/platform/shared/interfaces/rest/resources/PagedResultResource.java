package com.andeva.atelier.platform.shared.interfaces.rest.resources;

import java.util.List;
import java.util.Objects;

/**
 * Universal immutable container for paginated REST API responses.
 * Standardizes the pagination response envelope across CRM, MRO, Inventory, Invoicing, and Telemetry.
 *
 * @param <T>           type of resource elements contained in the page
 * @param items         immutable list of elements in the current page
 * @param page          zero-based current page index
 * @param size          requested page dimension limit
 * @param totalElements total count of matching records across the entire dataset
 * @param totalPages    computed total count of available pages
 * @param first         true if the current page is the first page
 * @param last          true if the current page is the last page
 * @author Joel Huamani Estefanero
 */
public record PagedResultResource<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public PagedResultResource {
        Objects.requireNonNull(items, "Items list cannot be null");
        items = List.copyOf(items);
        if (page < 0) {
            throw new IllegalArgumentException("Page index cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be strictly positive");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total elements cannot be negative");
        }
    }

    /**
     * Static factory computing total pages and first/last flags automatically.
     *
     * @param <T>           resource type
     * @param items         page items
     * @param page          current page index
     * @param size          page size limit
     * @param totalElements total matching records
     * @return initialized PagedResultResource
     */
    public static <T> PagedResultResource<T> of(List<T> items, int page, int size, long totalElements) {
        int calculatedTotalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        boolean isFirst = page == 0;
        boolean isLast = calculatedTotalPages == 0 || page >= calculatedTotalPages - 1;
        return new PagedResultResource<>(items, page, size, totalElements, calculatedTotalPages, isFirst, isLast);
    }
}
