package poc03;

import java.util.function.Function;

/**
 * IMPL 02 — Built-in: Function<T, R>
 *
 * Java ships with many functional interfaces in java.util.function.
 * You don't need to define your own for common cases.
 *
 * Function<T, R>
 *   - T = input type
 *   - R = return (result) type
 *   - Abstract method: R apply(T t)
 *   - "Take one thing, produce another thing."
 *
 * Useful built-in methods on Function:
 *   - andThen(Function after)  → apply this, then apply after
 *   - compose(Function before) → apply before first, then apply this
 *   - Function.identity()      → returns its input unchanged
 */
public class Impl02 {

    public static void main(String[] args) {

        // Function<String, Integer>: takes a String, returns an Integer
        // Here: the length of the string
        Function<String, Integer> strLength = s -> s.length();

        System.out.println(strLength.apply("hello"));       // 5
        System.out.println(strLength.apply("functional"));  // 10

        // Function<Integer, String>: takes an Integer, returns a String
        Function<Integer, String> intToStr = n -> "Number: " + n;

        System.out.println(intToStr.apply(42));   // Number: 42
        System.out.println(intToStr.apply(100));  // Number: 100

        // Function<String, String>: same type in and out
        Function<String, String> toUpper = s -> s.toUpperCase();

        System.out.println(toUpper.apply("java")); // JAVA

        // andThen: apply strLength first, then apply intToStr
        // "hello" → 5 → "Number: 5"
        Function<String, String> lengthAsString = strLength.andThen(intToStr);
        System.out.println(lengthAsString.apply("hello")); // Number: 5

        // compose: apply intToStr AFTER something else
        // compose(before) means: run `before` first, then run `this`
        // intToStr.compose(strLength) means: strLength first, then intToStr
        // same result as andThen above but written from the other direction
        Function<String, String> composed = intToStr.compose(strLength);
        System.out.println(composed.apply("world")); // Number: 5

        // Function.identity(): returns whatever you give it, unchanged
        // Useful as a no-op placeholder when a Function is required
        Function<String, String> identity = Function.identity();
        System.out.println(identity.apply("unchanged")); // unchanged

        // Passing a Function to a helper method
        System.out.println(transform("  hello world  ", s -> s.trim()));       // "hello world"
        System.out.println(transform("  hello world  ", s -> s.trim().toUpperCase())); // "HELLO WORLD"
    }

    // Generic helper — accepts any Function<String, String>
    static String transform(String input, Function<String, String> fn) {
        return fn.apply(input);
    }
}
