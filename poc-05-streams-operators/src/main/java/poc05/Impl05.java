package poc05;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * IMPL 05 — UnaryOperator<T>
 *
 * UnaryOperator<T> is a functional interface that:
 *   - Takes ONE input of type T
 *   - Returns a value of the SAME type T
 *
 * "Unary" means "one operand". "Operator" means it transforms.
 * Together: transforms ONE thing into another thing OF THE SAME TYPE.
 *
 * Examples:
 *   String  → String  (trim, uppercase, add prefix)
 *   Integer → Integer (double it, square it, negate it)
 *   Double  → Double  (round, multiply by tax rate)
 *
 * UnaryOperator<T> EXTENDS Function<T, T>.
 * So UnaryOperator IS a Function where input and output types are the same.
 * The difference is convention: UnaryOperator signals "same type in, same type out."
 *
 * Java source (simplified):
 *   @FunctionalInterface
 *   public interface UnaryOperator<T> extends Function<T, T> {
 *       static <T> UnaryOperator<T> identity() { return t -> t; }
 *       // apply(T t) is inherited from Function<T, T>
 *   }
 *
 * Key Java APIs that take UnaryOperator:
 *   - List.replaceAll(UnaryOperator<E>)   — transform elements in-place
 *   - Stream.iterate(seed, UnaryOperator) — generate sequences (Impl06)
 */
public class Impl05 {

    public static void main(String[] args) {

        // --- Basic UnaryOperator ---

        // Input: String → Output: String (same type, different value)
        UnaryOperator<String> toUpper = s -> s.toUpperCase();
        System.out.println(toUpper.apply("hello")); // HELLO
        System.out.println(toUpper.apply("world")); // WORLD

        UnaryOperator<Integer> doubler = n -> n * 2;
        System.out.println(doubler.apply(5));  // 10
        System.out.println(doubler.apply(21)); // 42

        UnaryOperator<Double> roundToTwo = d -> Math.round(d * 100.0) / 100.0;
        System.out.println(roundToTwo.apply(3.14159)); // 3.14
        System.out.println(roundToTwo.apply(2.71828)); // 2.72

        // --- UnaryOperator.identity() — the "do nothing" operator ---

        // identity() returns the input unchanged.
        // Useful as a default/no-op when a transform is optional.
        // Example: "apply this transform if configured, otherwise identity"
        UnaryOperator<String> noOp = UnaryOperator.identity();
        System.out.println(noOp.apply("unchanged")); // unchanged
        System.out.println(noOp.apply("still here")); // still here

        // --- List.replaceAll(UnaryOperator<E>) — transform IN-PLACE ---

        // replaceAll() applies the operator to each element and replaces it.
        // The list IS modified — this is one of the rare Java stream-adjacent methods
        // that mutates the list instead of producing a new one.
        List<String> words = new ArrayList<>(List.of("hello", "world", "java"));
        words.replaceAll(s -> s.toUpperCase()); // mutates words
        System.out.println(words); // [HELLO, WORLD, JAVA]

        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        nums.replaceAll(n -> n * n); // square each element in place
        System.out.println(nums); // [1, 4, 9, 16, 25]

        // --- UnaryOperator.andThen() — composing two operators ---

        // andThen() chains operators: run THIS, then run AFTER with its output.
        // IMPORTANT: andThen/compose return Function<T,T>, not UnaryOperator<T>.
        // Java's generics cannot preserve UnaryOperator through composition.
        // Functionally equivalent — just a different declared type.
        UnaryOperator<String> trim   = s -> s.trim();
        UnaryOperator<String> upper  = s -> s.toUpperCase();
        UnaryOperator<String> exclaim = s -> s + "!";

        // trim → upper → exclaim
        Function<String, String> pipeline = trim.andThen(upper).andThen(exclaim);
        System.out.println(pipeline.apply("  hello  ")); // HELLO!
        System.out.println(pipeline.apply("  java   ")); // JAVA!

        // --- Using replaceAll with a composed operator ---

        List<String> messy = new ArrayList<>(List.of("  foo  ", "  bar  ", "  baz  "));
        messy.replaceAll(s -> s.trim().toUpperCase()); // inline: trim then upper
        System.out.println(messy); // [FOO, BAR, BAZ]
    }
}
