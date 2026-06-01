package poc04;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IMPL 04 — map(): Transforming Every Element
 *
 * map() is an INTERMEDIATE operation.
 * It applies a function to EVERY element and produces a new stream of transformed values.
 *
 * Signature: Stream<R> map(Function<T, R> mapper)
 *   - Input:  Stream<T>   (stream of type T)
 *   - Output: Stream<R>   (stream of type R, possibly different)
 *
 * map() does NOT filter — every element goes in, every element comes out (transformed).
 * The output stream always has the same number of elements as the input stream.
 *
 * Difference from filter():
 *   filter() keeps or discards — same type, fewer elements.
 *   map()    transforms — possibly different type, same count.
 */
public class Impl04 {

    public static void main(String[] args) {

        List<String> fruits = List.of("apple", "banana", "cherry", "date");

        // --- map String → String (same type, different value) ---
        // toUpperCase() is called on each element; the stream type stays Stream<String>
        System.out.println("--- Uppercase fruits ---");
        fruits.stream()
            .map(f -> f.toUpperCase()) // "apple" → "APPLE"
            .forEach(System.out::println);

        // --- map String → Integer (type changes) ---
        // .length() returns int; the stream becomes Stream<Integer>
        System.out.println("\n--- Length of each fruit name ---");
        fruits.stream()
            .map(f -> f.length()) // "apple" (String) → 5 (Integer)
            .forEach(l -> System.out.println(l));

        // --- map Integer → Integer ---
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        System.out.println("\n--- Squares ---");
        numbers.stream()
            .map(n -> n * n) // n → n²
            .forEach(System.out::println); // 1, 4, 9, 16, 25

        // --- map Integer → String (type change again) ---
        // Useful for formatting numbers as text.
        System.out.println("\n--- Numbers formatted as strings ---");
        numbers.stream()
            .map(n -> "Number: " + n) // 1 → "Number: 1"
            .forEach(System.out::println);

        // --- Chaining filter + map ---
        // Order matters: filter first, then map. Only surviving elements get transformed.
        System.out.println("\n--- Even numbers, doubled ---");
        numbers.stream()
            .filter(n -> n % 2 == 0) // keep evens: 2, 4
            .map(n -> n * 2)          // double each: 4, 8
            .forEach(System.out::println);

        // --- Collecting mapped results ---
        List<String> upperFruits = fruits.stream()
            .map(String::toUpperCase) // method reference: same as f -> f.toUpperCase()
            .collect(Collectors.toList());

        System.out.println("\n--- Collected uppercase list ---");
        System.out.println(upperFruits);

        // --- map with a custom object ---
        // Suppose you have a list of full names and want just the first names.
        List<String> fullNames = List.of("Alice Smith", "Bob Jones", "Charlie Brown");

        System.out.println("\n--- First names only ---");
        fullNames.stream()
            .map(name -> name.split(" ")[0]) // "Alice Smith" → ["Alice","Smith"] → "Alice"
            .forEach(System.out::println);

        // --- mapToInt(): map to primitive int (no boxing) ---
        // mapToInt returns IntStream instead of Stream<Integer>.
        // Use when you need numeric operations like sum(), average().
        System.out.println("\n--- Sum of fruit name lengths (mapToInt) ---");
        int totalLength = fruits.stream()
            .mapToInt(f -> f.length()) // Stream<String> → IntStream
            .sum();                     // IntStream terminal operation
        System.out.println("Total length: " + totalLength);
    }
}
