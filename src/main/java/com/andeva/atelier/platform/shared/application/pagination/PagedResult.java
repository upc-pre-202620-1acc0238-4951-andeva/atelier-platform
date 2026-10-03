package com.andeva.atelier.platform.shared.application.pagination;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Immutable container for paginated query results in the application layer.
 *
 * @param <T>           type of the page content elements
 * @param content       immutable list of page elements
 * @param page          current zero-based page index
 * @param size          requested page dimension
 * @param totalElements total count of existing matching records
 * @param totalPages    computed total page count
 * @author Joel Huamani Estefanero
 */
public record PagedResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * Compact constructor validating pagination boundaries and taking defensive copy of content.
     */
    public PagedResult {
        Objects.requireNonNull(content, "Page content cannot be null");
        content = List.copyOf(content);
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
     * Factory computing totalPages automatically based on totalElements and size.
     *
     * @param <T>           element type
     * @param content       page elements
     * @param page          current page index
     * @param size          page size
     * @param totalElements total matching records
     * @return PagedResult instance
     */
    public static <T> PagedResult<T> of(List<T> content, int page, int size, long totalElements) {
        int calculatedTotalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PagedResult<>(content, page, size, totalElements, calculatedTotalPages);
    }

    /**
     * Applies a mapping function over each element in the page, preserving pagination metadata.
     *
     * @param <R>    the transformed element type
     * @param mapper mapping function
     * @return transformed PagedResult instance
     */
    public <R> PagedResult<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "Mapping function cannot be null");
        List<R> transformed = this.content.stream().<R>map(mapper).toList();
        return new PagedResult<>(transformed, this.page, this.size, this.totalElements, this.totalPages);
    }
}
