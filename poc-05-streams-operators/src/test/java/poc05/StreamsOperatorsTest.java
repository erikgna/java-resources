package poc05;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for poc05 — Streams: Supplier, Consumer, BinaryOperator, UnaryOperator.
 *
 * Each section maps to one Impl file.
 * Tests cover happy path AND edge cases:
 *   - Empty streams / empty inputs
 *   - Single-element cases
 *   - Null-safe behavior (where applicable)
 *   - Composition / chaining
 *   - BoundaryValues
 */
class StreamsOperatorsTest {

    // ═══════════════════════════════════════════════════════
    // IMPL 01 — Supplier<T>
    // ═══════════════════════════════════════════════════════

    @Test
    void supplier_get_returns_expected_string() {
        Supplier<String> s = () -> "hello";
        assertEquals("hello", s.get());
    }

    @Test
    void supplier_get_can_be_called_multiple_times() {
        Supplier<String> s = () -> "hello";
        assertEquals("hello", s.get());
        assertEquals("hello", s.get()); // second call works fine
    }

    @Test
    void supplier_as_factory_produces_distinct_instances() {
        Supplier<ArrayList<String>> factory = ArrayList::new;
        ArrayList<String> a = factory.get();
        ArrayList<String> b = factory.get();
        // Same type, but different objects — factory creates a new instance each call
        assertNotSame(a, b);
    }

    @Test
    void supplier_is_lazy_does_not_run_until_get() {
        boolean[] ran = {false};
        Supplier<String> lazy = () -> {
            ran[0] = true;
            return "done";
        };
        assertFalse(ran[0], "Supplier should not have run yet");
        lazy.get();
        assertTrue(ran[0], "Supplier should have run after get()");
    }

    @Test
    void supplier_returns_different_values_each_call_when_stateful() {
        int[] counter = {0};
        Supplier<Integer> incrementing = () -> ++counter[0];
        assertEquals(1, incrementing.get());
        assertEquals(2, incrementing.get());
        assertEquals(3, incrementing.get());
    }

    @Test
    void supplier_of_integer_returns_correct_type() {
        Supplier<Integer> s = () -> 42;
        assertEquals(Integer.class, s.get().getClass());
        assertEquals(42, s.get());
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 02 — Supplier<T> with Stream.generate()
    // ═══════════════════════════════════════════════════════

    @Test
    void stream_generate_with_constant_supplier_produces_n_copies() {
        List<String> result = Stream.generate(() -> "x")
            .limit(5)
            .collect(Collectors.toList());
        assertEquals(5, result.size());
        assertTrue(result.stream().allMatch(s -> s.equals("x")));
    }

    @Test
    void stream_generate_stateful_counter_increments_correctly() {
        int[] n = {0};
        List<Integer> result = Stream.generate(() -> ++n[0])
            .limit(4)
            .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 3, 4), result);
    }

    @Test
    void stream_generate_method_reference_supplier() {
        // Math::random matches Supplier<Double>
        List<Double> randoms = Stream.generate(Math::random)
            .limit(10)
            .collect(Collectors.toList());
        assertEquals(10, randoms.size());
        randoms.forEach(d -> assertTrue(d >= 0.0 && d < 1.0));
    }

    @Test
    void stream_generate_limit_zero_returns_empty() {
        List<String> result = Stream.generate(() -> "hello")
            .limit(0)
            .collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    @Test
    void stream_generate_limit_one_returns_single_element() {
        List<String> result = Stream.generate(() -> "only")
            .limit(1)
            .collect(Collectors.toList());
        assertEquals(List.of("only"), result);
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 03 — Consumer<T>
    // ═══════════════════════════════════════════════════════

    @Test
    void consumer_accept_executes_side_effect() {
        List<String> log = new ArrayList<>();
        Consumer<String> consumer = s -> log.add(s);
        consumer.accept("hello");
        assertEquals(List.of("hello"), log);
    }

    @Test
    void consumer_accept_called_multiple_times_accumulates() {
        List<Integer> collected = new ArrayList<>();
        Consumer<Integer> consumer = collected::add;
        consumer.accept(1);
        consumer.accept(2);
        consumer.accept(3);
        assertEquals(List.of(1, 2, 3), collected);
    }

    @Test
    void consumer_andThen_both_consumers_receive_same_input() {
        List<String> first  = new ArrayList<>();
        List<String> second = new ArrayList<>();

        Consumer<String> c1 = s -> first.add("c1:" + s);
        Consumer<String> c2 = s -> second.add("c2:" + s);

        c1.andThen(c2).accept("test");

        assertEquals(List.of("c1:test"), first);
        assertEquals(List.of("c2:test"), second);
    }

    @Test
    void consumer_andThen_executes_in_order() {
        List<String> order = new ArrayList<>();
        Consumer<String> first  = s -> order.add("first");
        Consumer<String> second = s -> order.add("second");
        Consumer<String> third  = s -> order.add("third");

        first.andThen(second).andThen(third).accept("ignored");

        assertEquals(List.of("first", "second", "third"), order);
    }

    @Test
    void consumer_used_with_foreach_processes_all_elements() {
        List<Integer> doubled = new ArrayList<>();
        Consumer<Integer> doubler = n -> doubled.add(n * 2);
        List.of(1, 2, 3).forEach(doubler);
        assertEquals(List.of(2, 4, 6), doubled);
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 04 — BiConsumer<T, U>
    // ═══════════════════════════════════════════════════════

    @Test
    void biconsumer_accept_receives_both_arguments() {
        List<String> log = new ArrayList<>();
        BiConsumer<String, Integer> consumer = (name, score) ->
            log.add(name + "=" + score);
        consumer.accept("Alice", 90);
        assertEquals(List.of("Alice=90"), log);
    }

    @Test
    void biconsumer_andThen_both_run_with_same_inputs() {
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();

        BiConsumer<String, Integer> bc1 = (k, v) -> a.add(k + ":" + v);
        BiConsumer<String, Integer> bc2 = (k, v) -> b.add(k + "=" + v);

        bc1.andThen(bc2).accept("score", 42);

        assertEquals(List.of("score:42"), a);
        assertEquals(List.of("score=42"), b);
    }

    @Test
    void biconsumer_map_foreach_iterates_all_entries() {
        java.util.Map<String, Integer> map = java.util.Map.of("a", 1, "b", 2, "c", 3);
        List<String> result = new ArrayList<>();
        BiConsumer<String, Integer> consumer = (k, v) -> result.add(k + v);
        map.forEach(consumer);
        assertEquals(3, result.size()); // all three entries visited
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 05 — UnaryOperator<T>
    // ═══════════════════════════════════════════════════════

    @Test
    void unary_operator_apply_transforms_string() {
        UnaryOperator<String> upper = String::toUpperCase;
        assertEquals("HELLO", upper.apply("hello"));
    }

    @Test
    void unary_operator_apply_transforms_integer() {
        UnaryOperator<Integer> doubler = n -> n * 2;
        assertEquals(10, doubler.apply(5));
        assertEquals(0, doubler.apply(0));
        assertEquals(-4, doubler.apply(-2));
    }

    @Test
    void unary_operator_identity_returns_input_unchanged() {
        UnaryOperator<String> id = UnaryOperator.identity();
        assertEquals("hello", id.apply("hello"));
        assertEquals("",      id.apply(""));
    }

    @Test
    void unary_operator_identity_same_reference() {
        UnaryOperator<List<String>> id = UnaryOperator.identity();
        List<String> list = List.of("a", "b");
        assertSame(list, id.apply(list)); // identity returns the EXACT same object
    }

    @Test
    void list_replaceAll_applies_operator_in_place() {
        List<String> words = new ArrayList<>(List.of("hello", "world"));
        words.replaceAll(String::toUpperCase);
        assertEquals(List.of("HELLO", "WORLD"), words);
    }

    @Test
    void list_replaceAll_squares_integers() {
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        nums.replaceAll(n -> n * n);
        assertEquals(List.of(1, 4, 9, 16, 25), nums);
    }

    @Test
    void unary_operator_andThen_composition() {
        UnaryOperator<String> trim  = String::trim;
        UnaryOperator<String> upper = String::toUpperCase;
        Function<String, String> pipeline = trim.andThen(upper);
        assertEquals("HELLO", pipeline.apply("  hello  "));
    }

    @Test
    void unary_operator_compose_runs_before() {
        // f.compose(g) = f(g(x)): g runs first, then f
        UnaryOperator<Integer> addThree  = n -> n + 3;
        UnaryOperator<Integer> timesTwo  = n -> n * 2;
        Function<Integer, Integer> composed = addThree.compose(timesTwo); // timesTwo first, then addThree
        // input=5: timesTwo(5)=10, addThree(10)=13
        assertEquals(13, composed.apply(5));
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 06 — UnaryOperator with Stream.iterate()
    // ═══════════════════════════════════════════════════════

    @Test
    void stream_iterate_powers_of_two() {
        List<Integer> result = Stream.iterate(1, n -> n * 2)
            .limit(5)
            .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 4, 8, 16), result);
    }

    @Test
    void stream_iterate_counting_from_zero() {
        List<Integer> result = Stream.iterate(0, n -> n + 1)
            .limit(5)
            .collect(Collectors.toList());
        assertEquals(List.of(0, 1, 2, 3, 4), result);
    }

    @Test
    void stream_iterate_limit_one_returns_seed_only() {
        List<Integer> result = Stream.iterate(99, n -> n + 1)
            .limit(1)
            .collect(Collectors.toList());
        assertEquals(List.of(99), result);
    }

    @Test
    void stream_iterate_string_growing() {
        List<String> result = Stream.iterate("a", s -> s + "a")
            .limit(4)
            .collect(Collectors.toList());
        assertEquals(List.of("a", "aa", "aaa", "aaaa"), result);
    }

    @Test
    void stream_iterate_java9_bounded_form() {
        // iterate(seed, predicate, op) — like: for (int i=0; i<10; i+=2)
        List<Integer> result = Stream.iterate(0, n -> n < 10, n -> n + 2)
            .collect(Collectors.toList());
        assertEquals(List.of(0, 2, 4, 6, 8), result);
    }

    @Test
    void stream_iterate_fibonacci_first_ten() {
        List<Integer> fib = Stream.iterate(
                new int[]{0, 1}, pair -> new int[]{pair[1], pair[0] + pair[1]}
            )
            .limit(10)
            .map(pair -> pair[0])
            .collect(Collectors.toList());
        assertEquals(List.of(0, 1, 1, 2, 3, 5, 8, 13, 21, 34), fib);
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 07 — BinaryOperator<T>
    // ═══════════════════════════════════════════════════════

    @Test
    void binary_operator_add_two_integers() {
        BinaryOperator<Integer> add = Integer::sum;
        assertEquals(7, add.apply(3, 4));
    }

    @Test
    void binary_operator_max_returns_larger() {
        BinaryOperator<Integer> max = Integer::max;
        assertEquals(9, max.apply(3, 9));
        assertEquals(9, max.apply(9, 3));
    }

    @Test
    void binary_operator_min_returns_smaller() {
        BinaryOperator<Integer> min = Integer::min;
        assertEquals(3, min.apply(3, 9));
        assertEquals(3, min.apply(9, 3));
    }

    @Test
    void binary_operator_string_longer() {
        BinaryOperator<String> longer = (a, b) -> a.length() >= b.length() ? a : b;
        assertEquals("elephant", longer.apply("cat", "elephant"));
        assertEquals("elephant", longer.apply("elephant", "ox"));
    }

    @Test
    void binary_operator_minBy_picks_shortest_string() {
        BinaryOperator<String> shortest =
            BinaryOperator.minBy(Comparator.comparingInt(String::length));
        assertEquals("cat", shortest.apply("cat", "elephant"));
        assertEquals("ox",  shortest.apply("elephant", "ox"));
    }

    @Test
    void binary_operator_maxBy_picks_longest_string() {
        BinaryOperator<String> longest =
            BinaryOperator.maxBy(Comparator.comparingInt(String::length));
        assertEquals("elephant", longest.apply("cat", "elephant"));
        assertEquals("elephant", longest.apply("elephant", "ox"));
    }

    @Test
    void binary_operator_equal_length_strings_picks_first_with_minBy() {
        BinaryOperator<String> byLen =
            BinaryOperator.minBy(Comparator.comparingInt(String::length));
        // Both length 3: Comparator.compare returns 0, minBy returns first
        assertEquals("cat", byLen.apply("cat", "dog"));
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 08 — BinaryOperator with reduce()
    // ═══════════════════════════════════════════════════════

    @Test
    void reduce_sum_with_identity_returns_correct_total() {
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
    void reduce_single_element_returns_that_element() {
        int result = Stream.of(42).reduce(0, Integer::sum);
        assertEquals(42, result);
    }

    @Test
    void reduce_no_identity_returns_optional_with_max() {
        Optional<Integer> result = Stream.of(3, 1, 9, 4, 7).reduce(Integer::max);
        assertTrue(result.isPresent());
        assertEquals(9, result.get());
    }

    @Test
    void reduce_no_identity_empty_stream_returns_empty_optional() {
        Optional<Integer> result = Stream.<Integer>of().reduce(Integer::sum);
        assertFalse(result.isPresent());
    }

    @Test
    void reduce_longest_string_via_maxBy_inside_reduce() {
        Optional<String> longest = Stream.of("cat", "elephant", "ox")
            .reduce(BinaryOperator.maxBy(Comparator.comparingInt(String::length)));
        assertTrue(longest.isPresent());
        assertEquals("elephant", longest.get());
    }

    @Test
    void reduce_string_concatenation() {
        String result = Stream.of("Java", "is", "fun")
            .reduce("", (acc, w) -> acc.isEmpty() ? w : acc + " " + w);
        assertEquals("Java is fun", result);
    }

    @Test
    void reduce_in_full_pipeline_sum_squares_of_evens() {
        int result = List.of(1, 2, 3, 4, 5, 6).stream()
            .filter(n -> n % 2 == 0)
            .map(n -> n * n)
            .reduce(0, Integer::sum);
        assertEquals(56, result); // 4 + 16 + 36
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 09 — Composing operators
    // ═══════════════════════════════════════════════════════

    @Test
    void function_andThen_runs_first_then_second() {
        Function<Integer, Integer> addThree = n -> n + 3;
        Function<Integer, Integer> timesTwo = n -> n * 2;
        // andThen: addThree first, then timesTwo
        Function<Integer, Integer> combined = addThree.andThen(timesTwo);
        assertEquals(16, combined.apply(5)); // (5+3)*2 = 16
    }

    @Test
    void function_compose_runs_second_first() {
        Function<Integer, Integer> addThree = n -> n + 3;
        Function<Integer, Integer> timesTwo = n -> n * 2;
        // compose: timesTwo first, then addThree
        Function<Integer, Integer> combined = addThree.compose(timesTwo);
        assertEquals(13, combined.apply(5)); // (5*2)+3 = 13
    }

    @Test
    void unary_operator_andThen_triple_chain() {
        UnaryOperator<String> trim  = String::trim;
        UnaryOperator<String> upper = String::toUpperCase;
        UnaryOperator<String> bang  = s -> s + "!";
        Function<String, String> pipeline = trim.andThen(upper).andThen(bang);
        assertEquals("HELLO!", pipeline.apply("  hello  "));
    }

    @Test
    void consumer_andThen_chain_of_three_runs_in_order() {
        List<Integer> order = new ArrayList<>();
        Consumer<String> c1 = s -> order.add(1);
        Consumer<String> c2 = s -> order.add(2);
        Consumer<String> c3 = s -> order.add(3);
        c1.andThen(c2).andThen(c3).accept("ignored");
        assertEquals(List.of(1, 2, 3), order);
    }

    @Test
    void consumer_andThen_all_receive_original_input() {
        List<String> received = new ArrayList<>();
        Consumer<String> c1 = received::add;
        Consumer<String> c2 = received::add;
        c1.andThen(c2).accept("hello");
        // Both consumers receive the same original "hello" — not each other's output
        assertEquals(List.of("hello", "hello"), received);
    }

    // ═══════════════════════════════════════════════════════
    // IMPL 10 — Integrated pipeline
    // ═══════════════════════════════════════════════════════

    @Test
    void supplier_feeds_stream_pipeline() {
        Supplier<List<Integer>> source = () -> List.of(1, 2, 3, 4, 5);
        int sum = source.get().stream().reduce(0, Integer::sum);
        assertEquals(15, sum);
    }

    @Test
    void unary_operator_in_stream_map() {
        UnaryOperator<String> normalize = s -> s.trim().toUpperCase();
        List<String> result = Stream.of("  hello  ", "  world  ")
            .map(normalize)
            .collect(Collectors.toList());
        assertEquals(List.of("HELLO", "WORLD"), result);
    }

    @Test
    void consumer_as_foreach_accumulates_side_effects() {
        List<String> log = new ArrayList<>();
        Consumer<Integer> logger = n -> log.add("processed:" + n);
        Stream.of(1, 2, 3).forEach(logger);
        assertEquals(List.of("processed:1", "processed:2", "processed:3"), log);
    }

    @Test
    void binary_operator_reduce_aggregates_values_from_mapped_stream() {
        BinaryOperator<Double> sumOp = Double::sum;
        List<String> words = List.of("hello", "world", "java");
        double totalLength = words.stream()
            .map(s -> (double) s.length())
            .reduce(0.0, sumOp);
        assertEquals(14.0, totalLength); // 5 + 5 + 4
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 10})
    void stream_generate_produces_exactly_n_elements(int n) {
        long count = Stream.generate(() -> 0).limit(n).count();
        assertEquals(n, count);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "world", "java", "stream"})
    void unary_operator_uppercase_every_word(String word) {
        UnaryOperator<String> upper = String::toUpperCase;
        assertEquals(word.toUpperCase(), upper.apply(word));
    }
}
