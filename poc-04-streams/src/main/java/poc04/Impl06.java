package poc04;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IMPL 06 — collect(): Gathering Stream Results
 *
 * collect() is a TERMINAL operation.
 * It gathers all stream elements into a collection (List, Set, Map, etc).
 *
 * collect() takes a Collector argument. Collectors is a utility class with
 * pre-built collectors for the most common gathering operations.
 *
 * Most used collectors:
 *   Collectors.toList()              → gathers into an ArrayList
 *   Collectors.toSet()               → gathers into a HashSet (removes duplicates, unordered)
 *   Collectors.toUnmodifiableList()  → same as toList() but you can't add/remove later (Java 10+)
 *   Collectors.joining()             → concatenates strings
 *   Collectors.joining(delimiter)    → concatenates with a separator
 *   Collectors.groupingBy(classifier)→ groups elements into a Map by a key function
 *   Collectors.counting()            → counts elements (usually used inside groupingBy)
 *
 * Note: Collectors.toList() (Java 8+) vs List.copyOf() vs Stream.toList() (Java 16+)
 *   Stream.toList() is the modern shorthand — returns unmodifiable list.
 *   We use Collectors.toList() here because we're on Java 11.
 */
public class Impl06 {

    public static void main(String[] args) {

        List<String> words = List.of("banana", "apple", "cherry", "apple", "date", "banana", "apple");

        // --- toList() ---
        // Collects into a new mutable ArrayList.
        List<String> wordList = words.stream()
            .filter(w -> w.length() > 4) // keep long words
            .collect(Collectors.toList());
        System.out.println("--- toList() ---");
        System.out.println(wordList); // [banana, apple, cherry, apple, banana, apple]

        // --- toSet() ---
        // Collects into a HashSet: no duplicates, no guaranteed order.
        Set<String> wordSet = words.stream()
            .collect(Collectors.toSet());
        System.out.println("\n--- toSet() (no duplicates) ---");
        System.out.println(wordSet); // [banana, apple, cherry, date]  (order may vary)

        // --- joining() ---
        // Only works on Stream<String>. Concatenates all strings.
        String joined = words.stream()
            .distinct()                    // remove duplicates first
            .collect(Collectors.joining(", ")); // join with ", " between each
        System.out.println("\n--- joining() ---");
        System.out.println(joined); // banana, apple, cherry, date

        // joining with prefix and suffix:
        String csv = words.stream()
            .distinct()
            .collect(Collectors.joining(", ", "[", "]")); // delimiter, prefix, suffix
        System.out.println("joined with brackets: " + csv); // [banana, apple, cherry, date]

        // --- groupingBy() ---
        // Groups elements into a Map<K, List<V>>.
        // The key is produced by the classifier function.
        // Elements that produce the same key are grouped together in a List.
        Map<Integer, List<String>> byLength = words.stream()
            .collect(Collectors.groupingBy(w -> w.length())); // key = word length
        System.out.println("\n--- groupingBy(length) ---");
        byLength.forEach((len, group) -> System.out.println("length " + len + ": " + group));
        // length 5: [apple, apple, apple]
        // length 6: [banana, cherry, banana]
        // length 4: [date]

        // --- groupingBy() + counting() ---
        // Count how many times each word appears.
        Map<String, Long> wordCount = words.stream()
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        System.out.println("\n--- groupingBy(word) + counting() ---");
        wordCount.forEach((word, count) -> System.out.println(word + ": " + count));
        // apple: 3, banana: 2, cherry: 1, date: 1

        // --- groupingBy() on a custom property ---
        List<String> names = List.of("Alice", "Bob", "Anna", "Brian", "Charlie", "Beth");

        Map<Character, List<String>> byFirstLetter = names.stream()
            .collect(Collectors.groupingBy(name -> name.charAt(0))); // key = first char
        System.out.println("\n--- Names grouped by first letter ---");
        byFirstLetter.forEach((letter, group) -> System.out.println(letter + ": " + group));
        // A: [Alice, Anna], B: [Bob, Brian, Beth], C: [Charlie]

        // --- toUnmodifiableList() ---
        // Like toList() but calling .add() later will throw UnsupportedOperationException.
        List<String> locked = words.stream()
            .distinct()
            .collect(Collectors.toUnmodifiableList());
        System.out.println("\n--- toUnmodifiableList() ---");
        System.out.println(locked);
        // Trying to modify it:
        try {
            locked.add("fig"); // this will throw
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify unmodifiable list: " + e.getClass().getSimpleName());
        }
    }
}
