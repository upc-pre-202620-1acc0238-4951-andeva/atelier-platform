package com.andeva.atelier.platform.shared.interfaces.rest.resources;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for REST API resource DTOs: {@link ErrorResource}, {@link MessageResource}, and {@link PagedResultResource}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("REST Resources Unit Tests")
class RestResourcesTest {

    @Nested
    @DisplayName("ErrorResource Tests")
    class ErrorResourceTests {

        @Test
        @DisplayName("Should create ErrorResource enforcing invariants and defensive copying")
        void shouldCreateErrorResource() {
            List<String> mutableDetails = new ArrayList<>(List.of("field1: invalid"));
            Instant now = Instant.now();
            ErrorResource error = new ErrorResource("BAD_REQUEST", "Invalid input", mutableDetails, now);

            assertThat(error.code()).isEqualTo("BAD_REQUEST");
            assertThat(error.message()).isEqualTo("Invalid input");
            assertThat(error.details()).containsExactly("field1: invalid");
            assertThat(error.timestamp()).isEqualTo(now);

            mutableDetails.add("field2: required");
            assertThat(error.details()).hasSize(1);
            assertThatThrownBy(() -> error.details().add("mutation"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException when code or message is null")
        void shouldThrowWhenCodeOrMessageIsNull() {
            assertThatThrownBy(() -> new ErrorResource(null, "Message", List.of(), Instant.now()))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new ErrorResource("CODE", null, List.of(), Instant.now()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should create ErrorResource using static factories")
        void shouldCreateUsingStaticFactories() {
            ErrorResource simple = ErrorResource.of("NOT_FOUND", "Resource not located");
            assertThat(simple.code()).isEqualTo("NOT_FOUND");
            assertThat(simple.message()).isEqualTo("Resource not located");
            assertThat(simple.details()).isEmpty();
            assertThat(simple.timestamp()).isNotNull();

            ErrorResource withDetails = ErrorResource.of("VALIDATION_ERROR", "Validation failed", List.of("error1"));
            assertThat(withDetails.details()).containsExactly("error1");
        }
    }

    @Nested
    @DisplayName("MessageResource Tests")
    class MessageResourceTests {

        @Test
        @DisplayName("Should create MessageResource and reject null message")
        void shouldCreateMessageResource() {
            MessageResource resource = MessageResource.of("Password reset link sent");
            assertThat(resource.message()).isEqualTo("Password reset link sent");
            assertThat(resource.timestamp()).isNotNull();

            assertThatThrownBy(() -> new MessageResource(null, Instant.now()))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("PagedResultResource Tests")
    class PagedResultResourceTests {

        @Test
        @DisplayName("Should create PagedResultResource enforcing defensive copy and boundaries")
        void shouldCreatePagedResultResource() {
            List<String> mutableItems = new ArrayList<>(List.of("Item A", "Item B"));
            PagedResultResource<String> resource = new PagedResultResource<>(mutableItems, 0, 10, 2L, 1, true, true);

            assertThat(resource.items()).containsExactly("Item A", "Item B");
            assertThat(resource.first()).isTrue();
            assertThat(resource.last()).isTrue();

            mutableItems.add("Item C");
            assertThat(resource.items()).hasSize(2);
            assertThatThrownBy(() -> resource.items().add("Item D"))
                    .isInstanceOf(UnsupportedOperationException.class);

            assertThatThrownBy(() -> new PagedResultResource<>(null, 0, 10, 0L, 0, true, true))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new PagedResultResource<>(List.of(), -1, 10, 0L, 0, true, true))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new PagedResultResource<>(List.of(), 0, 0, 0L, 0, true, true))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new PagedResultResource<>(List.of(), 0, 10, -1L, 0, true, true))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should compute pagination metadata accurately via of factory")
        void shouldComputeMetadataAccurately() {
            // First and only page
            PagedResultResource<String> singlePage = PagedResultResource.of(List.of("A", "B"), 0, 10, 2L);
            assertThat(singlePage.totalPages()).isEqualTo(1);
            assertThat(singlePage.first()).isTrue();
            assertThat(singlePage.last()).isTrue();

            // First of multiple pages
            PagedResultResource<String> firstPage = PagedResultResource.of(List.of("A"), 0, 10, 25L);
            assertThat(firstPage.totalPages()).isEqualTo(3);
            assertThat(firstPage.first()).isTrue();
            assertThat(firstPage.last()).isFalse();

            // Middle page
            PagedResultResource<String> middlePage = PagedResultResource.of(List.of("B"), 1, 10, 25L);
            assertThat(middlePage.totalPages()).isEqualTo(3);
            assertThat(middlePage.first()).isFalse();
            assertThat(middlePage.last()).isFalse();

            // Last page
            PagedResultResource<String> lastPage = PagedResultResource.of(List.of("C"), 2, 10, 25L);
            assertThat(lastPage.totalPages()).isEqualTo(3);
            assertThat(lastPage.first()).isFalse();
            assertThat(lastPage.last()).isTrue();

            // Empty dataset
            PagedResultResource<String> empty = PagedResultResource.of(List.of(), 0, 10, 0L);
            assertThat(empty.totalPages()).isEqualTo(0);
            assertThat(empty.first()).isTrue();
            assertThat(empty.last()).isTrue();
        }
    }
}
