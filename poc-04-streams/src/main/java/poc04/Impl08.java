package poc04;

import java.util.List;
import java.util.Optional;

/**
 * IMPL 08 — reduce(): Collapsing a Stream into a Single Value
 *
 * reduce() is a TERMINAL operation.
 * It combines all stream elements into a single result using an accumulator function.
 *
 * Think of it as a rolling calculation:
 *   Start with an initial value (identity).
 *   For each element, call: result = combine(result, element)
 *   Return the final result after all elements are processed.
 *
 * Two common signatures:
 *
 *   T reduce(T identity, BinaryOperator<T> accumulator)
 *     - identity: the starting value (also returned if stream is empty)
 *     - accumulator: a function (a, b) -> result  where a=running total, b=next element
 *     - Always returns T (never null)
 *
 *   Optional<T> reduce(BinaryOperator<T> accumulator)
 *     - No identity value.
 *     - Returns Optional<T> because if the stream is empty, there's no result.
 *
 * Sum, product, min, max, string concatenation — all are just different reduce() calls.
 * (In practice, use IntStream.sum(), max(), min() for numbers — they're cleaner.)
 * reduce() is powerful for custom aggregations that Collectors doesn't cover.
 */
public class Impl08 {

    public static void main(String[] args) {

        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        // --- Sum using reduce (with identity) ---
        // identity = 0 (neutral element for addition: 0 + x = x)
        // Step by step: 0 + 1 = 1, 1 + 2 = 3, 3 + 3 = 6, 6 + 4 = 10, 10 + 5 = 15
        int sum = numbers.stream()
            .reduce(0, (accumulator, element) -> accumulator + element);
        System.out.println("Sum: " + sum); // 15

        // Same thing with a method reference — Integer::sum is (a, b) -> a + b
        int sumMethodRef = numbers.stream().reduce(0, Integer::sum);
        System.out.println("Sum (method ref): " + sumMethodRef); // 15

        // --- Product using reduce ---
        // identity = 1 (neutral for multiplication: 1 * x = x)
        int product = numbers.stream()
            .reduce(1, (acc, el) -> acc * el);
        System.out.println("Product: " + product); // 120 (1*2*3*4*5)

        // --- Max using reduce ---
        // identity = Integer.MIN_VALUE (smallest possible — any real value will beat it)
        int max = numbers.stream()
            .reduce(Integer.MIN_VALUE, (acc, el) -> acc > el ? acc : el);
        System.out.println("Max: " + max); // 5

        // --- reduce without identity returns Optional<T> ---
        // If the stream is empty, there's nothing to return, so Optional wraps the uncertainty.
        Optional<Integer> optMin = numbers.stream()
            .reduce((acc, el) -> acc < el ? acc : el); // keep the smaller
        optMin.ifPresent(m -> System.out.println("Min (Optional): " + m)); // 1

        // --- Empty stream: no identity means empty Optional ---
        Optional<Integer> emptyResult = List.<Integer>of().stream()
            .reduce((a, b) -> a + b);
        System.out.println("Empty stream result present: " + emptyResult.isPresent()); // false

        // Empty stream WITH identity returns the identity itself:
        int emptySum = List.<Integer>of().stream().reduce(0, Integer::sum);
        System.out.println("Empty stream sum (identity=0): " + emptySum); // 0

        // --- String concatenation via reduce ---
        List<String> words = List.of("Java", "Streams", "are", "powerful");
        String sentence = words.stream()
            .reduce("", (acc, word) -> acc.isEmpty() ? word : acc + " " + word);
        // "" → "" (empty acc, first word="Java" → "Java")
        // "Java" + " " + "Streams" = "Java Streams"
        // "Java Streams" + " " + "are" = "Java Streams are"
        // etc.
        System.out.println("Sentence: " + sentence);

        // --- reduce for custom logic: longest word ---
        List<String> fruits = List.of("fig", "banana", "apple", "cherry", "date");
        Optional<String> longest = fruits.stream()
            .reduce((a, b) -> a.length() >= b.length() ? a : b); // keep the longer string
        System.out.println("Longest fruit: " + longest.orElse("none")); // cherry

        // --- When to use reduce vs Collectors ---
        // reduce: when you need a single aggregated VALUE (sum, max, concatenated string).
        // collect: when you need a COLLECTION (List, Set, Map) as the result.
    }
}
