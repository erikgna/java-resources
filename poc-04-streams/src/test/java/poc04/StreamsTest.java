package poc04;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for poc04 — Streams: Lists, Arrays, Map, Filter, Predicates.
 *
 * Each test class section maps to one Impl file.
 * Tests verify both the happy path and edge cases:
 *   - Empty streams
 *   - Single-element streams
 *   - Streams where no element passes the filter
 *   - Boundary values for limit/skip
 *   - Optional.empty() vs Optional.of()
 */
class StreamsTest {

    // ═══════════════════════════════════════════════════════
    // IMPL 01 — What is a Stream?
    // ═══════════════════════════════════════════════════════

    @Test
    void stream_count_returns_size_of_source_list() {
        List<String> fruits = List.of("apple", "banana", "cherry");
        long count = fruits.stream().count();
        assertEquals(3, count);
    }

    @Test
    void stream_of_produces_expected_count() {
        long count = Stream.of(10, 20, 30, 40).count();
        assertEquals(4, count);
    }

    @Test
    void empty_list_stream_count_is_zero() {
        long count = List.of().stream().count();
        assertEquals(0, count);
    }

    @Test
    void stream_is_consumed_after_terminal_operation() {
        Stream<String> stream = Stream.of("a", "b", "c");
        stream.count(); // terminal — stream is now closed
        assertThrows(IllegalStateException.class, () -> stream.count());
    }

    @Test
    void filter_then_forEach_does_not_modify_original_list() {
        List<Integer> original = List.of(1, 2, 3, 4, 5);
        original.stream().filter(n -> n > 3).forEach(n -> {});
        assertEquals(5, original.size()); // original unchanged
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 02 — Streams from Arrays
    // ═══════════════════════════════════════════════════════

    @Test
    void arrays_stream_string_array_count() {
        String[] colors = {"red", "green", "blue"};
        long count = Arrays.stream(colors).count();
        assertEquals(3, count);
    }

    @Test
    void intstream_sum_from_array() {
        int[] scores = {10, 20, 30, 40};
        int sum = Arrays.stream(scores).sum();
        assertEquals(100, sum);
    }

    @Test
    void intstream_average_from_array() {
        int[] values = {4, 8, 12};
        OptionalDouble avg = Arrays.stream(values).average();
        assertTrue(avg.isPresent());
        assertEquals(8.0, avg.getAsDouble());
    }

    @Test
    void intstream_average_empty_array_is_empty() {
        OptionalDouble avg = Arrays.stream(new int[]{}).average();
        assertFalse(avg.isPresent());
    }

    @Test
    void intstream_range_count() {
        // range(1, 6) → 1,2,3,4,5 → 5 elements
        long count = IntStream.range(1, 6).count();
        assertEquals(5, count);
    }

    @Test
    void intstream_rangeClosed_count() {
        // rangeClosed(1, 6) → 1,2,3,4,5,6 → 6 elements
        long count = IntStream.rangeClosed(1, 6).count();
        assertEquals(6, count);
    }

    @Test
    void intstream_boxed_converts_to_list() {
        List<Integer> list = IntStream.rangeClosed(1, 3).boxed().collect(Collectors.toList());
        assertEquals(List.of(1, 2, 3), list);
    }

    @Test
    void intstream_max_from_array() {
        int[] values = {5, 1, 9, 3, 7};
        OptionalInt max = Arrays.stream(values).max();
        assertTrue(max.isPresent());
        assertEquals(9, max.getAsInt());
    }

    @Test
    void intstream_min_from_array() {
        int[] values = {5, 1, 9, 3, 7};
        OptionalInt min = Arrays.stream(values).min();
        assertTrue(min.isPresent());
        assertEquals(1, min.getAsInt());
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 03 — filter()
    // ═══════════════════════════════════════════════════════

    @Test
    void filter_even_numbers() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);
        List<Integer> evens = numbers.stream()
            .filter(n -> n % 2 == 0)
            .collect(Collectors.toList());
        assertEquals(List.of(2, 4, 6), evens);
    }

    @Test
    void filter_greater_than_five() {
        List<Integer> numbers = List.of(1, 3, 5, 7, 9);
        List<Integer> result = numbers.stream()
            .filter(n -> n > 5)
            .collect(Collectors.toList());
        assertEquals(List.of(7, 9), result);
    }

    @Test
    void filter_chained_even_and_greater_than_four() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        List<Integer> result = numbers.stream()
            .filter(n -> n % 2 == 0)
            .filter(n -> n > 4)
            .collect(Collectors.toList());
        assertEquals(List.of(6, 8), result);
    }

    @Test
    void filter_strings_starting_with_a() {
        List<String> names = List.of("Alice", "Bob", "Anna", "Charlie");
        List<String> result = names.stream()
            .filter(s -> s.startsWith("A"))
            .collect(Collectors.toList());
        assertEquals(List.of("Alice", "Anna"), result);
    }

    @Test
    void filter_no_match_returns_empty_list() {
        List<Integer> numbers = List.of(1, 2, 3);
        List<Integer> result = numbers.stream()
            .filter(n -> n > 100)
            .collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    @Test
    void filter_all_match_returns_full_list() {
        List<Integer> numbers = List.of(2, 4, 6);
        List<Integer> result = numbers.stream()
            .filter(n -> n % 2 == 0)
            .collect(Collectors.toList());
        assertEquals(List.of(2, 4, 6), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bob", "Eve", "Al"})
    void filter_short_names_length_3_or_less(String shortName) {
        List<String> names = List.of(shortName, "Alice", "Charlie");
        List<String> result = names.stream()
            .filter(n -> n.length() <= 3)
            .collect(Collectors.toList());
        assertTrue(result.contains(shortName));
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 04 — map()
    // ═══════════════════════════════════════════════════════

    @Test
    void map_to_uppercase() {
        List<String> fruits = List.of("apple", "banana");
        List<String> result = fruits.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
        assertEquals(List.of("APPLE", "BANANA"), result);
    }

    @Test
    void map_string_to_length() {
        List<String> words = List.of("hi", "hello", "hey");
        List<Integer> lengths = words.stream()
            .map(String::length)
            .collect(Collectors.toList());
        assertEquals(List.of(2, 5, 3), lengths);
    }

    @Test
    void map_squares() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        List<Integer> squares = numbers.stream()
            .map(n -> n * n)
            .collect(Collectors.toList());
        assertEquals(List.of(1, 4, 9, 16, 25), squares);
    }

    @Test
    void map_extract_first_name() {
        List<String> fullNames = List.of("Alice Smith", "Bob Jones");
        List<String> firstNames = fullNames.stream()
            .map(name -> name.split(" ")[0])
            .collect(Collectors.toList());
        assertEquals(List.of("Alice", "Bob"), firstNames);
    }

    @Test
    void mapToInt_sum_of_string_lengths() {
        List<String> words = List.of("cat", "elephant", "ox");
        int total = words.stream().mapToInt(String::length).sum();
        assertEquals(13, total); // 3 + 8 + 2
    }

    @Test
    void filter_then_map_pipeline() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);
        List<Integer> result = numbers.stream()
            .filter(n -> n % 2 == 0) // 2, 4, 6
            .map(n -> n * 3)          // 6, 12, 18
            .collect(Collectors.toList());
        assertEquals(List.of(6, 12, 18), result);
    }

    @Test
    void map_on_empty_stream_returns_empty_list() {
        List<Integer> result = List.<Integer>of().stream()
            .map(n -> n * 2)
            .collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 05 — Predicate<T>
    // ═══════════════════════════════════════════════════════

    @Test
    void predicate_test_method_returns_true_for_matching_value() {
        Predicate<Integer> isEven = n -> n % 2 == 0;
        assertTrue(isEven.test(4));
        assertFalse(isEven.test(7));
    }

    @Test
    void predicate_and_requires_both_true() {
        Predicate<Integer> isEven = n -> n % 2 == 0;
        Predicate<Integer> isPositive = n -> n > 0;
        Predicate<Integer> combined = isEven.and(isPositive);
        assertTrue(combined.test(4));
        assertFalse(combined.test(-4)); // even but not positive
        assertFalse(combined.test(3));  // positive but not even
    }

    @Test
    void predicate_or_passes_when_either_true() {
        Predicate<Integer> isZero = n -> n == 0;
        Predicate<Integer> isNegative = n -> n < 0;
        Predicate<Integer> combined = isZero.or(isNegative);
        assertTrue(combined.test(0));
        assertTrue(combined.test(-5));
        assertFalse(combined.test(3));
    }

    @Test
    void predicate_negate_flips_result() {
        Predicate<Integer> isEven = n -> n % 2 == 0;
        Predicate<Integer> isOdd = isEven.negate();
        assertTrue(isOdd.test(3));
        assertFalse(isOdd.test(4));
    }

    @Test
    void predicate_not_static_method() {
        Predicate<String> isEmpty = String::isEmpty;
        Predicate<String> isNotEmpty = Predicate.not(isEmpty);
        assertTrue(isNotEmpty.test("hello"));
        assertFalse(isNotEmpty.test(""));
    }

    @Test
    void predicate_used_in_filter() {
        Predicate<String> longerThan4 = s -> s.length() > 4;
        List<String> names = List.of("Alice", "Bob", "Charlie", "Ed");
        List<String> result = names.stream()
            .filter(longerThan4)
            .collect(Collectors.toList());
        assertEquals(List.of("Alice", "Charlie"), result);
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 06 — collect()
    // ═══════════════════════════════════════════════════════

    @Test
    void collect_toList() {
        List<Integer> result = Stream.of(3, 1, 2).collect(Collectors.toList());
        assertEquals(List.of(3, 1, 2), result);
    }

    @Test
    void collect_toSet_removes_duplicates() {
        Set<String> result = Stream.of("a", "b", "a", "c").collect(Collectors.toSet());
        assertEquals(Set.of("a", "b", "c"), result);
    }

    @Test
    void collect_joining_with_delimiter() {
        String result = Stream.of("one", "two", "three")
            .collect(Collectors.joining(", "));
        assertEquals("one, two, three", result);
    }

    @Test
    void collect_joining_with_prefix_and_suffix() {
        String result = Stream.of("a", "b", "c")
            .collect(Collectors.joining(", ", "[", "]"));
        assertEquals("[a, b, c]", result);
    }

    @Test
    void collect_groupingBy_groups_correctly() {
        List<String> words = List.of("cat", "dog", "ant", "bear", "fish");
        Map<Integer, List<String>> grouped = words.stream()
            .collect(Collectors.groupingBy(String::length));
        assertEquals(List.of("cat", "dog", "ant"), grouped.get(3));
        assertEquals(List.of("bear", "fish"), grouped.get(4));
    }

    @Test
    void collect_counting_per_group() {
        List<String> words = List.of("a", "b", "a", "c", "a", "b");
        Map<String, Long> counts = words.stream()
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        assertEquals(3L, counts.get("a"));
        assertEquals(2L, counts.get("b"));
        assertEquals(1L, counts.get("c"));
    }

    @Test
    void collect_toUnmodifiableList_throws_on_add() {
        List<String> locked = Stream.of("x", "y").collect(Collectors.toUnmodifiableList());
        assertThrows(UnsupportedOperationException.class, () -> locked.add("z"));
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 07 — sorted(), distinct(), limit(), skip()
    // ═══════════════════════════════════════════════════════

    @Test
    void sorted_natural_order_integers() {
        List<Integer> result = Stream.of(5, 2, 8, 1, 4)
            .sorted()
            .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 4, 5, 8), result);
    }

    @Test
    void sorted_reverse_order() {
        List<Integer> result = Stream.of(3, 1, 4, 1, 5)
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
        assertEquals(List.of(5, 4, 3, 1, 1), result);
    }

    @Test
    void sorted_by_string_length() {
        List<String> result = Stream.of("banana", "fig", "apple", "kiwi")
            .sorted(Comparator.comparingInt(String::length))
            .collect(Collectors.toList());
        assertEquals(List.of("fig", "kiwi", "apple", "banana"), result);
    }

    @Test
    void distinct_removes_duplicates() {
        List<Integer> result = Stream.of(1, 2, 2, 3, 3, 3, 4)
            .distinct()
            .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 3, 4), result);
    }

    @Test
    void limit_takes_first_n_elements() {
        List<Integer> result = Stream.of(10, 20, 30, 40, 50)
            .limit(3)
            .collect(Collectors.toList());
        assertEquals(List.of(10, 20, 30), result);
    }

    @Test
    void limit_larger_than_source_returns_all() {
        List<Integer> result = Stream.of(1, 2, 3)
            .limit(100)
            .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void skip_discards_first_n_elements() {
        List<Integer> result = Stream.of(1, 2, 3, 4, 5)
            .skip(2)
            .collect(Collectors.toList());
        assertEquals(List.of(3, 4, 5), result);
    }

    @Test
    void skip_larger_than_source_returns_empty() {
        List<Integer> result = Stream.of(1, 2, 3)
            .skip(100)
            .collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    @Test
    void sorted_distinct_limit_pipeline() {
        List<Integer> result = Stream.of(5, 3, 5, 1, 2, 3, 4)
            .sorted()
            .distinct()
            .limit(3)
            .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void pagination_page0() {
        List<String> items = List.of("A","B","C","D","E","F","G");
        List<String> page0 = items.stream().skip(0).limit(3).collect(Collectors.toList());
        assertEquals(List.of("A", "B", "C"), page0);
    }

    @Test
    void pagination_page1() {
        List<String> items = List.of("A","B","C","D","E","F","G");
        List<String> page1 = items.stream().skip(3).limit(3).collect(Collectors.toList());
        assertEquals(List.of("D", "E", "F"), page1);
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 08 — reduce()
    // ═══════════════════════════════════════════════════════

    @Test
    void reduce_sum_with_identity() {
        int result = Stream.of(1, 2, 3, 4, 5).reduce(0, Integer::sum);
        assertEquals(15, result);
    }

    @Test
    void reduce_product_with_identity() {
        int result = Stream.of(1, 2, 3, 4, 5).reduce(1, (a, b) -> a * b);
        assertEquals(120, result);
    }

    @Test
    void reduce_empty_stream_returns_identity() {
        int result = Stream.<Integer>of().reduce(0, Integer::sum);
        assertEquals(0, result);
    }

    @Test
    void reduce_without_identity_returns_optional() {
        Optional<Integer> result = Stream.of(3, 1, 4, 1, 5).reduce(Integer::max);
        assertTrue(result.isPresent());
        assertEquals(5, result.get());
    }

    @Test
    void reduce_empty_stream_no_identity_returns_empty_optional() {
        Optional<Integer> result = Stream.<Integer>of().reduce(Integer::sum);
        assertFalse(result.isPresent());
    }

    @Test
    void reduce_longest_string() {
        Optional<String> longest = Stream.of("cat", "elephant", "ox", "dog")
            .reduce((a, b) -> a.length() >= b.length() ? a : b);
        assertTrue(longest.isPresent());
        assertEquals("elephant", longest.get());
    }

    @Test
    void reduce_string_concatenation() {
        String result = Stream.of("Hello", "World", "!")
            .reduce("", (acc, s) -> acc.isEmpty() ? s : acc + " " + s);
        assertEquals("Hello World !", result);
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 09 — flatMap()
    // ═══════════════════════════════════════════════════════

    @Test
    void flatMap_flattens_list_of_lists() {
        List<List<Integer>> nested = List.of(
            List.of(1, 2),
            List.of(3, 4),
            List.of(5, 6)
        );
        List<Integer> flat = nested.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 3, 4, 5, 6), flat);
    }

    @Test
    void flatMap_sentences_to_words() {
        List<String> sentences = List.of("hello world", "foo bar");
        List<String> words = sentences.stream()
            .flatMap(s -> Arrays.stream(s.split(" ")))
            .collect(Collectors.toList());
        assertEquals(List.of("hello", "world", "foo", "bar"), words);
    }

    @Test
    void flatMap_empty_inner_lists_skipped() {
        List<List<String>> nested = List.of(
            List.of("a", "b"),
            List.of(),           // empty inner list
            List.of("c")
        );
        List<String> result = nested.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void flatMap_then_distinct_and_sorted() {
        List<List<Integer>> nested = List.of(List.of(3, 1, 2), List.of(2, 4, 1));
        List<Integer> result = nested.stream()
            .flatMap(List::stream)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 3, 4), result);
    }

    @Test
    void flatMap_count_total_elements() {
        List<List<String>> orders = List.of(
            List.of("laptop", "mouse"),
            List.of("keyboard"),
            List.of("monitor", "webcam", "headset")
        );
        long total = orders.stream().flatMap(List::stream).count();
        assertEquals(6, total);
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 10 — Integrated pipelines
    // ═══════════════════════════════════════════════════════

    static class Product {
        final String name;
        final String category;
        final double price;
        Product(String name, String category, double price) {
            this.name = name; this.category = category; this.price = price;
        }
    }

    private List<Product> catalog() {
        return List.of(
            new Product("Laptop",     "Electronics", 999.99),
            new Product("Phone",      "Electronics", 599.99),
            new Product("Headset",    "Electronics", 79.99),
            new Product("Desk",       "Furniture",   349.00),
            new Product("Chair",      "Furniture",   249.00),
            new Product("Lamp",       "Furniture",   39.99),
            new Product("Java Book",  "Books",       49.99),
            new Product("Clean Code", "Books",       35.00),
            new Product("Notebook",   "Books",       12.00)
        );
    }

    @Test
    void electronics_count() {
        long count = catalog().stream().filter(p -> p.category.equals("Electronics")).count();
        assertEquals(3, count);
    }

    @Test
    void total_electronics_price() {
        double total = catalog().stream()
            .filter(p -> p.category.equals("Electronics"))
            .mapToDouble(p -> p.price)
            .sum();
        assertEquals(999.99 + 599.99 + 79.99, total, 0.001);
    }

    @Test
    void most_expensive_product_is_laptop() {
        Optional<Product> priciest = catalog().stream()
            .max(Comparator.comparingDouble(p -> p.price));
        assertTrue(priciest.isPresent());
        assertEquals("Laptop", priciest.get().name);
    }

    @Test
    void cheapest_product_is_notebook() {
        Optional<Product> cheapest = catalog().stream()
            .min(Comparator.comparingDouble(p -> p.price));
        assertTrue(cheapest.isPresent());
        assertEquals("Notebook", cheapest.get().name);
    }

    @Test
    void group_by_category_has_three_categories() {
        Map<String, List<Product>> grouped = catalog().stream()
            .collect(Collectors.groupingBy(p -> p.category));
        assertEquals(3, grouped.size());
        assertTrue(grouped.containsKey("Electronics"));
        assertTrue(grouped.containsKey("Furniture"));
        assertTrue(grouped.containsKey("Books"));
    }

    @Test
    void books_joined_by_comma() {
        String result = catalog().stream()
            .filter(p -> p.category.equals("Books"))
            .map(p -> p.name)
            .collect(Collectors.joining(", "));
        assertEquals("Java Book, Clean Code, Notebook", result);
    }

    @Test
    void top3_cheapest_products() {
        List<String> names = catalog().stream()
            .sorted(Comparator.comparingDouble(p -> p.price))
            .limit(3)
            .map(p -> p.name)
            .collect(Collectors.toList());
        assertEquals(List.of("Notebook", "Clean Code", "Lamp"), names);
    }

    @Test
    void anyMatch_electronics_over_800() {
        boolean result = catalog().stream()
            .filter(p -> p.category.equals("Electronics"))
            .anyMatch(p -> p.price > 800);
        assertTrue(result);
    }

    @Test
    void allMatch_books_under_100() {
        boolean result = catalog().stream()
            .filter(p -> p.category.equals("Books"))
            .allMatch(p -> p.price < 100);
        assertTrue(result);
    }

    @Test
    void noneMatch_free_items() {
        boolean result = catalog().stream().noneMatch(p -> p.price == 0);
        assertTrue(result);
    }

    @Test
    void count_products_under_50() {
        long count = catalog().stream().filter(p -> p.price < 50.0).count();
        assertEquals(4, count); // Java Book 49.99, Lamp 39.99, Clean Code 35.00, Notebook 12.00
    }
}
