package poc04;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IMPL 09 — flatMap(): Flattening Nested Structures
 *
 * Problem: sometimes each element in your stream is itself a list (or array).
 * map() would give you a Stream<List<T>> — a stream of lists.
 * flatMap() "flattens" that into a single Stream<T> — one level removed.
 *
 * map()     → one input element → one output element  (1-to-1)
 * flatMap() → one input element → MANY output elements (1-to-many, then flattened)
 *
 * Signature: Stream<R> flatMap(Function<T, Stream<R>> mapper)
 *   - Your mapper must return a Stream<R> for each element.
 *   - flatMap collects all those streams and flattens them into one.
 *
 * Common use cases:
 *   - List of sentences → flat list of words
 *   - List of orders → flat list of items across all orders
 *   - List of arrays → flat stream of all values
 */
public class Impl09 {

    public static void main(String[] args) {

        // --- Problem: List of Lists ---
        List<List<Integer>> matrix = List.of(
            List.of(1, 2, 3),
            List.of(4, 5, 6),
            List.of(7, 8, 9)
        );

        // With map(): you get Stream<List<Integer>> — a stream of lists, NOT a stream of integers
        System.out.println("--- map() gives Stream<List<Integer>> (3 elements, each is a list) ---");
        matrix.stream()
            .map(row -> row)            // each element is still a List
            .forEach(row -> System.out.println("row: " + row));
        // row: [1, 2, 3]
        // row: [4, 5, 6]
        // row: [7, 8, 9]

        // With flatMap(): each inner list is expanded, results merged into one stream
        System.out.println("\n--- flatMap() gives Stream<Integer> (9 individual elements) ---");
        matrix.stream()
            .flatMap(row -> row.stream()) // List.stream() returns Stream<Integer> for each row
            .forEach(n -> System.out.print(n + " "));
        System.out.println(); // 1 2 3 4 5 6 7 8 9

        // --- Sentences → words ---
        List<String> sentences = List.of(
            "Java streams are cool",
            "flatMap is powerful",
            "practice makes perfect"
        );

        // Split each sentence into words → each produces String[] → convert to stream → flatten
        List<String> words = sentences.stream()
            .flatMap(sentence -> Arrays.stream(sentence.split(" "))) // String[] → Stream<String>
            .collect(Collectors.toList());
        System.out.println("\n--- All words from all sentences ---");
        System.out.println(words);
        // [Java, streams, are, cool, flatMap, is, powerful, practice, makes, perfect]

        // --- Distinct words ---
        System.out.println("\n--- Unique words (flatMap + distinct) ---");
        sentences.stream()
            .flatMap(s -> Arrays.stream(s.split(" ")))
            .distinct()
            .sorted()
            .forEach(w -> System.out.print(w + " "));
        System.out.println();

        // --- flatMap with arrays of integers ---
        int[][] arrays = {{1, 2}, {3, 4}, {5, 6}};
        System.out.println("\n--- int[][] flattened with flatMapToInt ---");
        Arrays.stream(arrays)
            .flatMapToInt(arr -> Arrays.stream(arr)) // int[] → IntStream
            .forEach(n -> System.out.print(n + " ")); // 1 2 3 4 5 6
        System.out.println();

        // --- Realistic example: orders and their items ---
        // Each "order" is a List<String> of product names.
        List<List<String>> orders = List.of(
            List.of("laptop", "mouse"),
            List.of("keyboard"),
            List.of("monitor", "headset", "webcam")
        );

        System.out.println("\n--- All ordered items (from all orders) ---");
        List<String> allItems = orders.stream()
            .flatMap(order -> order.stream()) // expand each order into its items
            .collect(Collectors.toList());
        System.out.println(allItems);
        // [laptop, mouse, keyboard, monitor, headset, webcam]

        System.out.println("Total items across all orders: " + allItems.size()); // 6

        // --- Counting unique items ---
        long uniqueCount = orders.stream()
            .flatMap(order -> order.stream())
            .distinct()
            .count();
        System.out.println("Unique items: " + uniqueCount);
    }
}
