package poc04;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * IMPL 05 — Predicate<T>: The Boolean-Returning Function
 *
 * A Predicate<T> is a functional interface from java.util.function.
 * Its single abstract method: boolean test(T t)
 *
 * You've already used Predicates without naming them:
 *   .filter(n -> n > 5)   is the same as   .filter(Predicate that returns n > 5)
 *
 * Why store a Predicate in a variable?
 *   - Reuse: define once, use in multiple places.
 *   - Combine: and(), or(), negate() let you compose complex conditions cleanly.
 *   - Name it: "isAdult" is clearer than "age >= 18" repeated 5 times in your code.
 *
 * Predicate<T> composition methods:
 *   predA.and(predB)    → true only if BOTH predA and predB are true
 *   predA.or(predB)     → true if EITHER predA or predB is true
 *   predA.negate()      → flips the result (true → false, false → true)
 *   Predicate.not(pred) → same as negate(), static version (Java 11+)
 */
public class Impl05 {

    public static void main(String[] args) {

        // --- Define named Predicates ---

        // Predicate<Integer>: takes an Integer, returns boolean
        Predicate<Integer> isEven = n -> n % 2 == 0;
        Predicate<Integer> isGreaterThan5 = n -> n > 5;
        Predicate<Integer> isNegative = n -> n < 0;

        // --- test() method: check a single value ---
        System.out.println("--- test() on single values ---");
        System.out.println("isEven.test(4):  " + isEven.test(4));   // true
        System.out.println("isEven.test(7):  " + isEven.test(7));   // false
        System.out.println("isGreaterThan5.test(3): " + isGreaterThan5.test(3)); // false

        List<Integer> numbers = List.of(-3, -1, 0, 1, 2, 4, 5, 6, 8, 10, 11);

        // --- Use named Predicate in filter ---
        System.out.println("\n--- Even numbers (named Predicate) ---");
        numbers.stream()
            .filter(isEven) // pass the named predicate — same as filter(n -> n % 2 == 0)
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // --- and(): BOTH conditions must be true ---
        Predicate<Integer> isEvenAndGreaterThan5 = isEven.and(isGreaterThan5);
        System.out.println("\n--- Even AND > 5 ---");
        numbers.stream()
            .filter(isEvenAndGreaterThan5)
            .forEach(n -> System.out.print(n + " ")); // 6, 8, 10
        System.out.println();

        // --- or(): EITHER condition can be true ---
        Predicate<Integer> isEvenOrNegative = isEven.or(isNegative);
        System.out.println("\n--- Even OR negative ---");
        numbers.stream()
            .filter(isEvenOrNegative)
            .forEach(n -> System.out.print(n + " ")); // -3, -1, 0, 2, 4, 6, 8, 10
        System.out.println();

        // --- negate(): flip the result ---
        Predicate<Integer> isOdd = isEven.negate(); // true when isEven is false
        System.out.println("\n--- Odd numbers (isEven.negate()) ---");
        numbers.stream()
            .filter(isOdd)
            .forEach(n -> System.out.print(n + " ")); // -3, -1, 1, 5, 11
        System.out.println();

        // --- Predicate.not() static version (Java 11+) ---
        // Predicate.not(pred) is identical to pred.negate().
        // Useful when you can't call .negate() directly (e.g., method references).
        Predicate<Integer> isPositive = n -> n > 0;
        System.out.println("\n--- Non-positive numbers (Predicate.not) ---");
        numbers.stream()
            .filter(Predicate.not(isPositive)) // same as isPositive.negate()
            .forEach(n -> System.out.print(n + " ")); // -3, -1, 0
        System.out.println();

        // --- String predicates ---
        Predicate<String> startsWithA = s -> s.startsWith("A");
        Predicate<String> longerThan4 = s -> s.length() > 4;

        List<String> names = List.of("Alice", "Bob", "Anna", "Charlie", "Art", "Eve");

        System.out.println("\n--- Names starting with 'A' AND length > 4 ---");
        names.stream()
            .filter(startsWithA.and(longerThan4)) // Alice, Anna pass startsWith; only Alice passes length > 4
            .collect(Collectors.toList())
            .forEach(System.out::println);

        // --- Composing more than two predicates ---
        Predicate<Integer> isBetween1And10 = n -> n >= 1 && n <= 10;
        Predicate<Integer> complexFilter = isEven.and(isBetween1And10).and(isGreaterThan5);
        System.out.println("\n--- Even AND in [1,10] AND > 5 ---");
        numbers.stream()
            .filter(complexFilter)
            .forEach(n -> System.out.print(n + " ")); // 6, 8, 10
        System.out.println();
    }
}
