package poc05;

import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * IMPL 08 — BinaryOperator<T> with Stream.reduce()
 *
 * reduce() is a TERMINAL operation that collapses a stream into ONE value
 * by repeatedly applying a BinaryOperator to accumulate all elements.
 *
 * Imagine folding a paper strip — each fold takes two halves and makes one:
 *   [1, 2, 3, 4, 5] with operator "+"
 *   Step 1: apply(1, 2) = 3
 *   Step 2: apply(3, 3) = 6
 *   Step 3: apply(6, 4) = 10
 *   Step 4: apply(10, 5) = 15
 *   Result: 15
 *
 * TWO forms of reduce():
 *
 *   Form 1: T reduce(T identity, BinaryOperator<T> accumulator)
 *     → always returns T (never null, never empty)
 *     → identity is the "starting value":
 *         0 for sum, 1 for product, "" for concatenation, Integer.MIN_VALUE for max
 *     → empty stream returns identity
 *
 *   Form 2: Optional<T> reduce(BinaryOperator<T> accumulator)
 *     → returns Optional<T> because an empty stream has NO result
 *     → Optional.empty()  if stream is empty
 *     → Optional.of(val)  if stream has elements
 *
 * Why Optional in Form 2?
 *   Without an identity, we can't return anything meaningful for an empty stream.
 *   Java forces you to handle this case explicitly via Optional.
 */
public class Impl08 {

    public static void main(String[] args) {

        // ─── Form 1: reduce(identity, BinaryOperator) → always returns T ─────────

        // Sum: identity = 0 (adding nothing gives 0)
        int sum = Stream.of(1, 2, 3, 4, 5).reduce(0, Integer::sum);
        System.out.println("Sum: " + sum); // 15

        // Product: identity = 1 (multiplying by nothing gives 1)
        int product = Stream.of(1, 2, 3, 4, 5).reduce(1, (a, b) -> a * b);
        System.out.println("Product: " + product); // 120

        // Max: identity = Integer.MIN_VALUE (any real value beats "negative infinity")
        int max = Stream.of(5, 3, 9, 1, 7).reduce(Integer.MIN_VALUE, Integer::max);
        System.out.println("Max: " + max); // 9

        // Empty stream → returns identity, never throws
        int emptySum = Stream.<Integer>of().reduce(0, Integer::sum);
        System.out.println("Empty sum: " + emptySum); // 0

        // ─── Form 2: reduce(BinaryOperator) → Optional ────────────────────────────

        // No identity needed — but must handle empty case via Optional.
        Optional<Integer> maxOpt = Stream.of(3, 1, 4, 1, 5, 9, 2, 6)
            .reduce(Integer::max);
        maxOpt.ifPresent(m -> System.out.println("Max via Optional: " + m)); // 9

        Optional<Integer> emptyOpt = Stream.<Integer>of().reduce(Integer::sum);
        System.out.println("Empty Optional present: " + emptyOpt.isPresent()); // false

        // ─── BinaryOperator.minBy / maxBy inside reduce ────────────────────────────

        // minBy and maxBy build a BinaryOperator from a Comparator.
        // Great for finding min/max of complex objects.
        java.util.Comparator<String> byLength = java.util.Comparator.comparingInt(String::length);
        BinaryOperator<String> longestOp = BinaryOperator.maxBy(byLength);

        Optional<String> longest = Stream.of("cat", "elephant", "ox", "butterfly")
            .reduce(longestOp);
        longest.ifPresent(s -> System.out.println("Longest: " + s)); // butterfly

        Optional<String> shortest = Stream.of("cat", "elephant", "ox", "butterfly")
            .reduce(BinaryOperator.minBy(byLength));
        shortest.ifPresent(s -> System.out.println("Shortest: " + s)); // ox

        // ─── String concatenation via reduce ──────────────────────────────────────

        // "" is the identity for string concatenation.
        String sentence = Stream.of("Java", "is", "fun")
            .reduce("", (acc, word) -> acc.isEmpty() ? word : acc + " " + word);
        System.out.println("Sentence: " + sentence); // Java is fun

        // ─── reduce in a full pipeline ─────────────────────────────────────────────

        // Sum of squares of even numbers: filter → map → reduce
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        int sumOfSquaresOfEvens = numbers.stream()
            .filter(n -> n % 2 == 0)   // 2, 4, 6, 8
            .map(n -> n * n)            // 4, 16, 36, 64
            .reduce(0, Integer::sum);   // 120
        System.out.println("Sum of squares of evens: " + sumOfSquaresOfEvens); // 120
    }
}
