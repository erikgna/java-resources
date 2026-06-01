package poc04;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IMPL 03 — filter(): Keeping Only What Matches
 *
 * filter() is an INTERMEDIATE operation.
 * It takes a Predicate<T> (a function that returns true or false)
 * and keeps only elements where the predicate returns true.
 *
 * Signature: Stream<T> filter(Predicate<T> predicate)
 *   - Input:  Stream<T>
 *   - Output: Stream<T>  (same type, fewer elements)
 *
 * The lambda you pass to filter() must return boolean.
 * Each element is tested: true = keep, false = discard.
 *
 * filter() does NOT modify the original list.
 * It produces a new (reduced) stream.
 */
public class Impl03 {

    public static void main(String[] args) {

        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // --- Filter: keep only even numbers ---
        // n % 2 == 0 is true when n divides evenly by 2
        System.out.println("--- Even numbers ---");
        numbers.stream()
            .filter(n -> n % 2 == 0)         // predicate: is n even?
            .forEach(n -> System.out.print(n + " ")); // terminal: print each
        System.out.println();

        // --- Filter: keep only numbers greater than 5 ---
        System.out.println("\n--- Numbers > 5 ---");
        numbers.stream()
            .filter(n -> n > 5)
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // --- Chaining multiple filters ---
        // Each filter is applied in sequence.
        // An element must pass ALL filters to survive.
        System.out.println("\n--- Even AND > 4 ---");
        numbers.stream()
            .filter(n -> n % 2 == 0) // first: keep evens (2,4,6,8,10)
            .filter(n -> n > 4)       // second: keep > 4 (6,8,10)
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // --- Filter on strings ---
        List<String> names = List.of("Alice", "Bob", "Charlie", "Anna", "Brian", "Eve");

        System.out.println("\n--- Names starting with 'A' ---");
        names.stream()
            .filter(name -> name.startsWith("A")) // startsWith() is a String method
            .forEach(System.out::println);

        System.out.println("\n--- Names with more than 3 characters ---");
        names.stream()
            .filter(name -> name.length() > 3) // length() returns int
            .forEach(System.out::println);

        // --- Collecting filtered results into a new List ---
        // .collect(Collectors.toList()) is a terminal operation that gathers stream elements into a List.
        // The original 'names' list is unchanged.
        List<String> shortNames = names.stream()
            .filter(name -> name.length() <= 3) // keep names with 3 or fewer characters
            .collect(Collectors.toList());       // gather results into a new List<String>

        System.out.println("\n--- Short names (length <= 3), collected to list ---");
        System.out.println(shortNames); // [Bob, Eve]

        // --- Filter that matches nothing ---
        // Result is an empty stream, not an error.
        System.out.println("\n--- Filter that matches nothing ---");
        long count = numbers.stream()
            .filter(n -> n > 100) // no number > 100 exists
            .count();             // count() is terminal: returns number of surviving elements
        System.out.println("Count of numbers > 100: " + count); // 0
    }
}
