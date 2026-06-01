package poc03;

import java.util.function.Predicate;

/**
 * IMPL 03 — Built-in: Predicate<T>
 *
 * Predicate<T>
 *   - T = input type
 *   - Returns: boolean
 *   - Abstract method: boolean test(T t)
 *   - "Ask a yes/no question about one thing."
 *
 * Predicates are heavily used for filtering.
 *
 * Built-in combining methods:
 *   - and(Predicate other)  → this AND other (both must be true)
 *   - or(Predicate other)   → this OR other (at least one must be true)
 *   - negate()              → flip the result (true → false, false → true)
 *   - Predicate.not(p)      → static version of negate (Java 11+)
 */
public class Impl03 {

    public static void main(String[] args) {

        // Simple predicates
        Predicate<String> isEmpty   = s -> s.isEmpty();
        Predicate<String> isLong    = s -> s.length() > 5;  // more than 5 chars
        Predicate<Integer> isEven   = n -> n % 2 == 0;
        Predicate<Integer> isPositive = n -> n > 0;

        System.out.println("isEmpty(\"\")    = " + isEmpty.test(""));      // true
        System.out.println("isEmpty(\"hi\") = " + isEmpty.test("hi"));     // false
        System.out.println("isLong(\"hi\")  = " + isLong.test("hi"));      // false
        System.out.println("isLong(\"functional\") = " + isLong.test("functional")); // true
        System.out.println("isEven(4) = " + isEven.test(4));               // true
        System.out.println("isEven(3) = " + isEven.test(3));               // false

        // --- Combining Predicates ---

        // and(): both must return true
        Predicate<Integer> isEvenAndPositive = isEven.and(isPositive);
        System.out.println("\neven AND positive:");
        System.out.println("  4  → " + isEvenAndPositive.test(4));   // true
        System.out.println(" -4  → " + isEvenAndPositive.test(-4));  // false (not positive)
        System.out.println("  3  → " + isEvenAndPositive.test(3));   // false (not even)

        // or(): at least one must return true
        Predicate<Integer> isEvenOrPositive = isEven.or(isPositive);
        System.out.println("\neven OR positive:");
        System.out.println("  4  → " + isEvenOrPositive.test(4));    // true (both)
        System.out.println(" -3  → " + isEvenOrPositive.test(-3));   // false (neither)
        System.out.println(" -4  → " + isEvenOrPositive.test(-4));   // true (even)
        System.out.println("  3  → " + isEvenOrPositive.test(3));    // true (positive)

        // negate(): flip the result
        Predicate<Integer> isOdd = isEven.negate();
        System.out.println("\nnegate (isOdd):");
        System.out.println("  3  → " + isOdd.test(3));  // true
        System.out.println("  4  → " + isOdd.test(4));  // false

        // Predicate.not() — Java 11 static version, cleaner to read sometimes
        Predicate<String> isNotEmpty = Predicate.not(isEmpty);
        System.out.println("\nPredicate.not(isEmpty):");
        System.out.println("  \"\"   → " + isNotEmpty.test(""));    // false
        System.out.println("  \"hi\" → " + isNotEmpty.test("hi"));  // true

        // Passing a Predicate to a helper method
        System.out.println("\ncheckAll:");
        checkAll(10, isEven, isPositive, isEvenAndPositive);
    }

    // Accepts any number of Predicates and tests them all against the value
    @SafeVarargs
    static void checkAll(int value, Predicate<Integer>... predicates) {
        for (Predicate<Integer> predicate : predicates) {
            System.out.println("  " + value + " passes? " + predicate.test(value));
        }
    }
}
