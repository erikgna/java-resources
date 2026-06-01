package poc04;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IMPL 07 — sorted(), distinct(), limit(), skip()
 *
 * These are all INTERMEDIATE operations that control the shape/order/size of the stream.
 *
 * sorted()         → sorts elements in natural order (numbers: ascending, strings: alphabetical)
 * sorted(comparator) → sorts using a custom ordering rule
 * distinct()       → removes duplicate elements (uses .equals() to detect duplicates)
 * limit(n)         → keeps only the first n elements, discards the rest
 * skip(n)          → skips the first n elements, keeps the rest
 *
 * Combining limit() and skip() is how you do pagination:
 *   page 0: skip(0).limit(pageSize)
 *   page 1: skip(pageSize).limit(pageSize)
 *   page 2: skip(pageSize * 2).limit(pageSize)
 */
public class Impl07 {

    public static void main(String[] args) {

        List<Integer> numbers = List.of(5, 3, 8, 1, 9, 2, 7, 4, 6, 3, 1, 8);
        List<String> fruits = List.of("banana", "apple", "cherry", "apple", "date", "banana");

        // --- sorted() natural order ---
        // Integers: ascending (1, 2, 3 ...)
        // Strings:  alphabetical (a, b, c ...)
        System.out.println("--- Sorted numbers (natural) ---");
        numbers.stream()
            .sorted()
            .forEach(n -> System.out.print(n + " "));
        System.out.println(); // 1 1 2 3 3 4 5 6 7 8 8 9

        System.out.println("\n--- Sorted fruits (natural) ---");
        fruits.stream()
            .sorted()
            .forEach(f -> System.out.print(f + " "));
        System.out.println(); // apple apple banana banana cherry date

        // --- sorted() reverse order ---
        // Comparator.reverseOrder() is a built-in Comparator that reverses natural ordering.
        System.out.println("\n--- Sorted numbers descending ---");
        numbers.stream()
            .sorted(Comparator.reverseOrder()) // pass a Comparator for custom order
            .forEach(n -> System.out.print(n + " "));
        System.out.println(); // 9 8 8 7 6 5 4 3 3 2 1 1

        // --- sorted() by string length ---
        // Comparator.comparingInt extracts an int key from each element for comparison.
        System.out.println("\n--- Fruits sorted by name length ---");
        fruits.stream()
            .distinct()                                          // remove dupes first for clarity
            .sorted(Comparator.comparingInt(f -> f.length()))    // sort by length, ascending
            .forEach(f -> System.out.print(f + " "));
        System.out.println(); // date apple banana cherry

        // --- distinct() ---
        // Removes duplicates. Keeps first occurrence, discards subsequent ones.
        System.out.println("\n--- Distinct numbers ---");
        numbers.stream()
            .distinct()
            .forEach(n -> System.out.print(n + " "));
        System.out.println(); // 5 3 8 1 9 2 7 4 6 (order from original, no repeats)

        // --- limit(n) ---
        // Stops the stream after n elements.
        // Very useful when combined with sorted(): get the top-N.
        System.out.println("\n--- First 3 numbers ---");
        numbers.stream()
            .limit(3)
            .forEach(n -> System.out.print(n + " ")); // 5 3 8
        System.out.println();

        System.out.println("\n--- Top 3 numbers (sorted + limit) ---");
        numbers.stream()
            .sorted(Comparator.reverseOrder()) // biggest first
            .limit(3)                           // keep only top 3
            .forEach(n -> System.out.print(n + " ")); // 9 8 8
        System.out.println();

        // --- skip(n) ---
        // Discards the first n elements, then passes the rest downstream.
        System.out.println("\n--- Skip first 3, then take next 4 ---");
        numbers.stream()
            .skip(3)   // discard 5, 3, 8 → remaining: 1 9 2 7 4 6 3 1 8
            .limit(4)  // take 1 9 2 7
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // --- Pagination simulation ---
        List<String> items = List.of("A","B","C","D","E","F","G","H","I","J");
        int pageSize = 3;

        System.out.println("\n--- Page 0 (skip 0, limit 3) ---");
        items.stream().skip(0).limit(pageSize).forEach(i -> System.out.print(i + " ")); // A B C
        System.out.println();

        System.out.println("\n--- Page 1 (skip 3, limit 3) ---");
        items.stream().skip(pageSize).limit(pageSize).forEach(i -> System.out.print(i + " ")); // D E F
        System.out.println();

        System.out.println("\n--- Page 2 (skip 6, limit 3) ---");
        items.stream().skip(pageSize * 2).limit(pageSize).forEach(i -> System.out.print(i + " ")); // G H I
        System.out.println();
    }
}
