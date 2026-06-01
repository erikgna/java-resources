package poc03;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all 10 Functional Interface implementations.
 *
 * Each test group corresponds to one Impl class, covering:
 *   - Happy path (normal, expected usage)
 *   - Edge cases (empty, null, boundary values)
 *   - Composition and chaining
 *   - Error/exception conditions
 */
class FunctionalInterfacesTest {

    // =========================================================
    // IMPL 01 — Custom @FunctionalInterface
    // =========================================================

    @FunctionalInterface
    interface MathOperation {
        int operate(int a, int b);
    }

    @Test
    void impl01_lambdaAddition() {
        MathOperation add = (a, b) -> a + b;
        assertEquals(15, add.operate(10, 5));
    }

    @Test
    void impl01_lambdaSubtraction() {
        MathOperation sub = (a, b) -> a - b;
        assertEquals(5, sub.operate(10, 5));
        assertEquals(-5, sub.operate(5, 10)); // negative result
    }

    @Test
    void impl01_lambdaMultiplication() {
        MathOperation mul = (a, b) -> a * b;
        assertEquals(50, mul.operate(10, 5));
        assertEquals(0, mul.operate(0, 100));   // multiply by zero
        assertEquals(-20, mul.operate(-4, 5));  // negative operand
    }

    @Test
    void impl01_passFunctionAsArgument() {
        // Passing lambdas as method arguments (first-class functions)
        MathOperation add = Integer::sum; // method reference for addition
        assertEquals(7, applyOp(add, 3, 4));
    }

    int applyOp(MathOperation op, int a, int b) {
        return op.operate(a, b);
    }

    // =========================================================
    // IMPL 02 — Function<T, R>
    // =========================================================

    @Test
    void impl02_functionApply() {
        Function<String, Integer> length = String::length;
        assertEquals(5, length.apply("hello"));
        assertEquals(0, length.apply(""));       // edge: empty string
    }

    @Test
    void impl02_andThen_composesLeftToRight() {
        Function<String, Integer> length = String::length;
        Function<Integer, String> label  = n -> "len=" + n;

        Function<String, String> composed = length.andThen(label);
        assertEquals("len=5", composed.apply("hello"));
        assertEquals("len=0", composed.apply(""));
    }

    @Test
    void impl02_compose_composesRightToLeft() {
        Function<Integer, String> label  = n -> "n=" + n;
        Function<String, Integer> length = String::length;

        // label.compose(length) means: apply length first, then label
        Function<String, String> composed = label.compose(length);
        assertEquals("n=5", composed.apply("hello"));
    }

    @Test
    void impl02_identity_returnsInput() {
        Function<String, String> id = Function.identity();
        assertEquals("hello", id.apply("hello"));
        assertEquals("", id.apply(""));
    }

    @ParameterizedTest
    @CsvSource({"hello,5", "java,4", ",0", "functional,10"})
    void impl02_lengthParameterized(String input, int expected) {
        Function<String, Integer> length = s -> s == null ? 0 : s.length();
        assertEquals(expected, length.apply(input == null ? "" : input));
    }

    // =========================================================
    // IMPL 03 — Predicate<T>
    // =========================================================

    @Test
    void impl03_predicateTest() {
        Predicate<String> isEmpty = String::isEmpty;
        assertTrue(isEmpty.test(""));
        assertFalse(isEmpty.test("hello"));
    }

    @Test
    void impl03_and_bothMustBeTrue() {
        Predicate<Integer> isEven     = n -> n % 2 == 0;
        Predicate<Integer> isPositive = n -> n > 0;
        Predicate<Integer> both       = isEven.and(isPositive);

        assertTrue(both.test(4));   // even and positive
        assertFalse(both.test(-4)); // even but not positive
        assertFalse(both.test(3));  // positive but not even
        assertFalse(both.test(-3)); // neither
    }

    @Test
    void impl03_or_atLeastOneMustBeTrue() {
        Predicate<Integer> isEven     = n -> n % 2 == 0;
        Predicate<Integer> isPositive = n -> n > 0;
        Predicate<Integer> either     = isEven.or(isPositive);

        assertTrue(either.test(4));    // both true
        assertTrue(either.test(-4));   // even only
        assertTrue(either.test(3));    // positive only
        assertFalse(either.test(-3));  // neither
    }

    @Test
    void impl03_negate_flipsResult() {
        Predicate<Integer> isEven = n -> n % 2 == 0;
        Predicate<Integer> isOdd  = isEven.negate();

        assertTrue(isOdd.test(3));
        assertFalse(isOdd.test(4));
        assertTrue(isOdd.test(-1));
    }

    @Test
    void impl03_predicateNot_java11() {
        Predicate<String> isEmpty    = String::isEmpty;
        Predicate<String> isNotEmpty = Predicate.not(isEmpty);

        assertTrue(isNotEmpty.test("hello"));
        assertFalse(isNotEmpty.test(""));
    }

    // =========================================================
    // IMPL 04 — Consumer<T>
    // =========================================================

    @Test
    void impl04_consumerSideEffect() {
        List<String> collected = new ArrayList<>();
        Consumer<String> collect = collected::add; // method ref adds to list

        collect.accept("alpha");
        collect.accept("beta");

        assertEquals(List.of("alpha", "beta"), collected);
    }

    @Test
    void impl04_andThen_chainsConsumers() {
        List<String> log1 = new ArrayList<>();
        List<String> log2 = new ArrayList<>();

        Consumer<String> c1 = log1::add;
        Consumer<String> c2 = s -> log2.add(s.toUpperCase());
        Consumer<String> both = c1.andThen(c2);

        both.accept("hello");

        assertEquals(List.of("hello"), log1);
        assertEquals(List.of("HELLO"), log2);
    }

    @Test
    void impl04_biConsumer_twoInputs() {
        List<String> results = new ArrayList<>();
        BiConsumer<String, Integer> repeatCollect = (s, n) -> {
            for (int i = 0; i < n; i++) results.add(s);
        };

        repeatCollect.accept("x", 3);
        assertEquals(List.of("x", "x", "x"), results);
    }

    // =========================================================
    // IMPL 05 — Supplier<T>
    // =========================================================

    @Test
    void impl05_supplierProducesValue() {
        Supplier<String> greeting = () -> "Hello";
        assertEquals("Hello", greeting.get());
    }

    @Test
    void impl05_supplierIsLazy() {
        int[] callCount = {0};
        // The lambda body only runs when get() is called
        Supplier<String> lazy = () -> {
            callCount[0]++;
            return "value";
        };

        assertEquals(0, callCount[0]);  // not called yet
        lazy.get();
        assertEquals(1, callCount[0]);  // called once
        lazy.get();
        assertEquals(2, callCount[0]);  // called again — each get() reruns
    }

    @Test
    void impl05_supplierAsFactory() {
        Supplier<ArrayList<String>> factory = ArrayList::new;
        ArrayList<String> a = factory.get();
        ArrayList<String> b = factory.get();

        assertNotSame(a, b); // different objects each time
    }

    @Test
    void impl05_defaultValuePattern() {
        Supplier<String> fallback = () -> "default";

        String result1 = getOrDefault(null, fallback);
        String result2 = getOrDefault("actual", fallback);

        assertEquals("default", result1);
        assertEquals("actual", result2);
    }

    String getOrDefault(String value, Supplier<String> fallback) {
        return value != null ? value : fallback.get();
    }

    // =========================================================
    // IMPL 06 — BiFunction, UnaryOperator, BinaryOperator
    // =========================================================

    @Test
    void impl06_biFunction_twoInputs() {
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        assertEquals("abab", repeat.apply("ab", 2));
        assertEquals("", repeat.apply("x", 0));     // edge: repeat 0 times
    }

    @Test
    void impl06_biFunction_andThen() {
        BiFunction<Integer, Integer, Integer> multiply = (a, b) -> a * b;
        BiFunction<Integer, Integer, String> multiplyLabel = multiply.andThen(n -> "=" + n);
        assertEquals("=42", multiplyLabel.apply(6, 7));
    }

    @Test
    void impl06_unaryOperator_sameTypeInOut() {
        UnaryOperator<String> trim  = String::trim;
        UnaryOperator<String> upper = String::toUpperCase;

        assertEquals("hello", trim.apply("  hello  "));
        assertEquals("WORLD", upper.apply("world"));
    }

    @Test
    void impl06_binaryOperator_twoSameTypeInputs() {
        BinaryOperator<Integer> add = Integer::sum;
        BinaryOperator<String> concat = (a, b) -> a + b;

        assertEquals(10, add.apply(3, 7));
        assertEquals("helloworld", concat.apply("hello", "world"));
    }

    @Test
    void impl06_binaryOperator_minMax() {
        BinaryOperator<Integer> min = BinaryOperator.minBy(Integer::compareTo);
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Integer::compareTo);

        assertEquals(3, min.apply(10, 3));
        assertEquals(10, max.apply(10, 3));
    }

    // =========================================================
    // IMPL 07 — Function Composition
    // =========================================================

    @Test
    void impl07_andThen_leftToRight() {
        Function<String, String> trim    = String::trim;
        Function<String, String> upper   = String::toUpperCase;
        Function<String, String> exclaim = s -> s + "!";

        Function<String, String> pipeline = trim.andThen(upper).andThen(exclaim);
        assertEquals("HELLO WORLD!", pipeline.apply("  hello world  "));
    }

    @Test
    void impl07_compose_rightToLeft() {
        Function<String, String> trim  = String::trim;
        Function<String, String> upper = String::toUpperCase;

        // upper.compose(trim) means: trim first, then upper
        Function<String, String> composed = upper.compose(trim);
        assertEquals("HELLO", composed.apply("  hello  "));
    }

    @Test
    void impl07_andThen_and_compose_equivalent() {
        Function<Integer, Integer> times2 = n -> n * 2;
        Function<Integer, Integer> plus10 = n -> n + 10;

        // times2.andThen(plus10) == plus10.compose(times2)
        Function<Integer, Integer> via_andThen = times2.andThen(plus10);
        Function<Integer, Integer> via_compose = plus10.compose(times2);

        assertEquals(via_andThen.apply(5), via_compose.apply(5)); // both: 5*2+10=20
        assertEquals(20, via_andThen.apply(5));
    }

    @Test
    void impl07_composedPipelineReusable() {
        Function<String, String> normalize = ((Function<String, String>) String::trim)
            .andThen(String::toUpperCase);

        assertEquals("JAVA", normalize.apply("  java  "));
        assertEquals("SPRING", normalize.apply("  spring  "));
        // Same normalize function reused, different inputs
    }

    // =========================================================
    // IMPL 08 — Method References
    // =========================================================

    @Test
    void impl08_staticMethodRef() {
        Function<String, Integer> parse = Integer::parseInt;
        assertEquals(42, parse.apply("42"));
        assertEquals(-1, parse.apply("-1"));
    }

    @Test
    void impl08_staticMethodRef_throwsOnBadInput() {
        Function<String, Integer> parse = Integer::parseInt;
        assertThrows(NumberFormatException.class, () -> parse.apply("not-a-number"));
    }

    @Test
    void impl08_particularInstanceRef() {
        String prefix = "PREFIX: ";
        Function<String, String> addPrefix = prefix::concat;
        assertEquals("PREFIX: hello", addPrefix.apply("hello"));
        assertEquals("PREFIX: ", addPrefix.apply(""));  // empty input
    }

    @Test
    void impl08_arbitraryInstanceRef() {
        Function<String, String> lower = String::toLowerCase;
        assertEquals("hello", lower.apply("HELLO"));
        assertEquals("java", lower.apply("JAVA"));
    }

    @Test
    void impl08_constructorRef_noArgs() {
        Supplier<ArrayList<String>> factory = ArrayList::new;
        ArrayList<String> list = factory.get();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void impl08_constructorRef_withArg() {
        // StringBuilder has a constructor that takes a String
        Function<String, StringBuilder> factory = StringBuilder::new;
        StringBuilder sb = factory.apply("hello");
        assertEquals("hello", sb.toString());
    }

    // =========================================================
    // IMPL 09 — ThrowingFunction and checked exceptions
    // =========================================================

    @FunctionalInterface
    interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }

    static <T, R> Function<T, R> wrap(ThrowingFunction<T, R> fn) {
        return t -> {
            try {
                return fn.apply(t);
            } catch (Exception e) {
                throw new RuntimeException("Wrapped", e);
            }
        };
    }

    @Test
    void impl09_throwingFunction_happyPath() throws Exception {
        ThrowingFunction<String, Integer> parse = s -> Integer.parseInt(s);
        assertEquals(42, parse.apply("42"));
    }

    @Test
    void impl09_throwingFunction_throwsOnBadInput() {
        ThrowingFunction<String, Integer> parse = s -> {
            if (s.isBlank()) throw new Exception("blank");
            return Integer.parseInt(s);
        };

        assertThrows(Exception.class, () -> parse.apply(""));
    }

    @Test
    void impl09_wrappedFunction_wrapsCheckedAsRuntime() {
        Function<String, Integer> safe = wrap(s -> {
            if (s.isBlank()) throw new Exception("blank input");
            return Integer.parseInt(s);
        });

        // Happy path
        assertEquals(10, safe.apply("10"));

        // Bad input → RuntimeException wrapping the original Exception
        RuntimeException ex = assertThrows(RuntimeException.class, () -> safe.apply(""));
        assertEquals("Wrapped", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("blank input", ex.getCause().getMessage());
    }

    // =========================================================
    // IMPL 10 — Pipeline (integration test)
    // =========================================================

    static class User {
        final String name;
        final int age;
        final boolean active;

        User(String name, int age, boolean active) {
            this.name = name; this.age = age; this.active = active;
        }

        boolean isActive() { return active; }
        int getAge()       { return age; }
        String getName()   { return name; }
    }

    static <T, R> List<R> pipeline(
        List<T> input,
        Predicate<T> filter,
        Function<T, R> transform,
        Consumer<R> output
    ) {
        List<R> results = new ArrayList<>();
        for (T item : input) {
            if (!filter.test(item)) continue;
            R transformed = transform.apply(item);
            output.accept(transformed);
            results.add(transformed);
        }
        return results;
    }

    @Test
    void impl10_pipeline_filtersAndTransforms() {
        List<User> users = List.of(
            new User("Alice", 30, true),
            new User("Bob",   16, true),   // minor — filtered out
            new User("Carol", 25, false),  // inactive — filtered out
            new User("Dave",  40, true)
        );

        Predicate<User> eligible = u -> u.getAge() >= 18 && u.isActive();
        Function<User, String> toName = User::getName;
        List<String> collected = new ArrayList<>();

        List<String> results = pipeline(users, eligible, toName, collected::add);

        assertEquals(List.of("Alice", "Dave"), results);
        assertEquals(List.of("Alice", "Dave"), collected); // consumer was called too
    }

    @Test
    void impl10_pipeline_emptyInput() {
        List<String> results = pipeline(
            new ArrayList<>(),
            s -> true,
            Function.identity(),
            s -> {}
        );
        assertTrue(results.isEmpty());
    }

    @Test
    void impl10_pipeline_allFiltered() {
        List<Integer> nums = List.of(1, 2, 3, 4, 5);
        Predicate<Integer> nonePass = n -> false; // nothing passes

        List<Integer> results = pipeline(nums, nonePass, Function.identity(), n -> {});
        assertTrue(results.isEmpty());
    }

    @Test
    void impl10_pipeline_composedTransform() {
        List<String> words = List.of("  hello  ", "  world  ", "  java  ");

        Function<String, String> trim  = String::trim;
        Function<String, String> upper = String::toUpperCase;
        Function<String, String> transform = trim.andThen(upper); // compose in pipeline

        List<String> results = pipeline(words, w -> true, transform, s -> {});
        assertEquals(List.of("HELLO", "WORLD", "JAVA"), results);
    }

    @Test
    void impl10_pipeline_consumerCalledForEachResult() {
        List<Integer> nums = List.of(1, 2, 3, 4, 5);
        int[] callCount = {0};

        pipeline(nums, n -> n % 2 == 0, n -> n * 10, n -> callCount[0]++);

        assertEquals(2, callCount[0]); // only 2 and 4 pass the even filter
    }
}
