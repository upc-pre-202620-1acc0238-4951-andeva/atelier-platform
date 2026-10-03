package com.andeva.atelier.platform.shared.application.pagination;

/**
 * Immutable model for paginated query requests in the application layer.
 * Completely decoupled from persistence framework classes (such as Spring Data Pageable).
 *
 * @param page          requested zero-based page index
 * @param size          page size or element limit
 * @param sortBy        field name to sort by (defaults to "id" if null or blank)
 * @param sortDirection direction of the sort order (defaults to ASC if null)
 * @author Joel Huamani Estefanero
 */
public record PagedQuery(
        int page,
        int size,
        String sortBy,
        SortDirection sortDirection
) {

    /**
     * Compact constructor enforcing validation rules and sensible defaults.
     */
    public PagedQuery {
        if (page < 0) {
            throw new IllegalArgumentException("Page index cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be strictly positive");
        }
        sortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
        sortDirection = sortDirection != null ? sortDirection : SortDirection.ASC;
    }

    /**
     * Creates a PagedQuery with default sort by "id" in ascending order.
     *
     * @param page zero-based page index
     * @param size page size
     * @return PagedQuery instance
     */
    public static PagedQuery of(int page, int size) {
        return new PagedQuery(page, size, "id", SortDirection.ASC);
    }

    /**
     * Creates a PagedQuery with explicit sort field and direction.
     *
     * @param page      zero-based page index
     * @param size      page size
     * @param sortBy    sort field
     * @param direction sort direction
     * @return PagedQuery instance
     */
    public static PagedQuery of(int page, int size, String sortBy, SortDirection direction) {
        return new PagedQuery(page, size, sortBy, direction);
    }
}
