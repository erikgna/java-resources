package poc05;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IMPL 02 — Supplier<T> with Stream.generate()
 *
 * Stream.generate(Supplier<T>) creates an INFINITE stream.
 * Each element is produced by calling the Supplier's get() method.
 *
 * Why "infinite"?
 *   The Supplier has no counter or end condition built in.
 *   Every time the stream pipeline needs another element, it calls get() again.
 *   This will go on forever unless you stop it.
 *
 * ALWAYS pair Stream.generate() with a stopping mechanism:
 *   - .limit(n)          — stop after n elements
 *   - .findFirst()       — stop after the first match
 *   - .takeWhile(pred)   — stop when predicate becomes false (Java 9+)
 *
 * Stream.generate() vs Stream.iterate():
 *   generate() — each element is INDEPENDENT (just calls Supplier.get())
 *   iterate()  — each element DEPENDS on the previous (covered in Impl06)
 *
 * How it works internally:
 *   The stream pipeline calls supplier.get() on demand (lazy).
 *   No elements are pre-computed. limit() tells the pipeline when to stop requesting.
 */
public class Impl02 {

    public static void main(String[] args) {

        // --- Generate a stream of constant values ---

        // The lambda () -> "hello" is called 5 times because of limit(5).
        List<String> hellos = Stream.generate(() -> "hello")
            .limit(5)
            .collect(Collectors.toList());
        System.out.println(hellos); // [hello, hello, hello, hello, hello]

        // --- Generate random numbers ---

        // Math::random is a method reference. It matches Supplier<Double>
        // because Math.random() takes no arguments and returns a double.
        List<Double> randoms = Stream.generate(Math::random)
            .limit(4)
            .collect(Collectors.toList());
        System.out.println("Randoms: " + randoms); // 4 random doubles

        // --- Stateful Supplier: a counter ---

        // Lambdas cannot use variables that change (non-final).
        // Trick: wrap mutable state inside a single-element array.
        // Arrays are objects — we CAN modify their contents inside a lambda.
        int[] counter = {0};
        Supplier<Integer> incrementing = () -> ++counter[0];

        List<Integer> sequence = Stream.generate(incrementing)
            .limit(5)
            .collect(Collectors.toList());
        System.out.println("Counting: " + sequence); // [1, 2, 3, 4, 5]

        // Note: stateful Suppliers are OK in sequential streams.
        // In parallel streams they produce non-deterministic results —
        // multiple threads call get() at the same time with no synchronization.

        // --- generate() vs iterate() side-by-side ---

        System.out.println("\n--- generate (independent values) ---");
        Stream.generate(() -> "X")
            .limit(3)
            .forEach(System.out::println); // X, X, X — no connection between elements

        System.out.println("\n--- iterate (sequence: each depends on previous) ---");
        Stream.iterate(1, n -> n * 2)     // 1, 2, 4, 8...
            .limit(5)
            .forEach(System.out::println);

        // --- takeWhile() to stop on condition (Java 9+) ---

        // Stop generating when we get a value >= 0.7 for the first time.
        // (This is non-deterministic; shown to illustrate the concept.)
        long countBeforeLarge = Stream.generate(Math::random)
            .limit(1000)                      // safety cap
            .takeWhile(d -> d < 0.7)
            .count();
        System.out.println("\nValues below 0.7 before first >=0.7: " + countBeforeLarge);
    }
}
