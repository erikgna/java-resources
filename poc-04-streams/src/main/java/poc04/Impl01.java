package poc04;

import java.util.List;
import java.util.stream.Stream;

/**
 * IMPL 01 — What is a Stream?
 *
 * A Stream is NOT a data structure. It does NOT store data.
 * A Stream is a pipeline of operations over a source of data (List, Array, etc).
 *
 * Think of it like an assembly line in a factory:
 *   - Raw materials enter on one side (the data source).
 *   - Each station transforms or filters the item (intermediate operations).
 *   - At the end, something is produced or consumed (terminal operation).
 *
 * Key rules:
 *   1. A stream can only be consumed ONCE. After the terminal operation runs, it's done.
 *   2. Streams are LAZY: intermediate operations don't execute until a terminal operation runs.
 *   3. The original list is NEVER modified. The stream produces new values.
 *
 * Two types of stream operations:
 *   - Intermediate: return another Stream (lazy). Examples: filter(), map(), sorted()
 *   - Terminal:     produce a result or side effect (eager). Examples: forEach(), count(), collect()
 */
public class Impl01 {

    public static void main(String[] args) {

        // --- Creating a stream from a List ---

        List<String> fruits = List.of("apple", "banana", "cherry", "date", "elderberry");

        // .stream() creates a Stream<String> from the list.
        // The list is the SOURCE. No data is moved yet — the stream is just a plan.
        Stream<String> fruitStream = fruits.stream();

        // forEach() is a TERMINAL operation.
        // It triggers all the lazy work and consumes the stream.
        // After this line, fruitStream is "used up" — calling anything else on it throws an error.
        System.out.println("--- All fruits (forEach) ---");
        fruitStream.forEach(fruit -> System.out.println(fruit));

        // To iterate again, create a NEW stream from the same list.
        // Lists are reusable; streams are not.
        System.out.println("\n--- All fruits again (new stream) ---");
        fruits.stream().forEach(System.out::println); // method reference: same as fruit -> System.out.println(fruit)

        // --- count() terminal operation ---
        // count() returns how many elements are in the stream (as a long).
        long total = fruits.stream().count();
        System.out.println("\nTotal fruits: " + total); // 5

        // --- Creating a stream directly from values (no list needed) ---
        Stream<Integer> numbers = Stream.of(10, 20, 30, 40, 50);
        System.out.println("\n--- Stream.of() numbers ---");
        numbers.forEach(n -> System.out.println(n));

        // --- What LAZY means ---
        // This stream has filter and map defined (intermediate), but nothing runs yet
        // because there is no terminal operation at the end.
        // Uncomment the block below to see: adding forEach() at the end triggers execution.

        /*
        fruits.stream()
            .filter(f -> {
                System.out.println("  [filter checking] " + f);
                return f.length() > 4;
            })
            .map(f -> {
                System.out.println("  [map transforming] " + f);
                return f.toUpperCase();
            });
        // Nothing printed above — no terminal operation, so nothing ran.
        */

        // With terminal operation added, now it runs:
        System.out.println("\n--- Lazy evaluation demo (filter + map + forEach) ---");
        fruits.stream()
            .filter(f -> {
                System.out.println("  [filter?] " + f);
                return f.length() > 4; // keep only fruits with more than 4 characters
            })
            .map(f -> {
                System.out.println("  [map!]    " + f);
                return f.toUpperCase(); // uppercase each surviving fruit
            })
            .forEach(f -> System.out.println("  [result]  " + f));
        // Notice: filter and map are called element-by-element, NOT list-by-list.
        // Java processes each element through the full pipeline before moving to the next.
    }
}
