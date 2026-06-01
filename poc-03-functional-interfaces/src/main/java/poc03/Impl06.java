package poc03;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * IMPL 06 — BiFunction, UnaryOperator, BinaryOperator
 *
 * These are variations on Function<T, R> for common patterns:
 *
 * BiFunction<T, U, R>
 *   - Takes TWO inputs (T and U), returns R
 *   - Abstract method: R apply(T t, U u)
 *   - Use when you need two different (or same) input types
 *
 * UnaryOperator<T>  (extends Function<T, T>)
 *   - One input of type T, returns same type T
 *   - Abstract method: T apply(T t)
 *   - Use when input and output are the same type
 *
 * BinaryOperator<T>  (extends BiFunction<T, T, T>)
 *   - Two inputs of type T, returns same type T
 *   - Abstract method: T apply(T t1, T t2)
 *   - Use when two inputs AND output are all the same type
 *
 * Quick mental model:
 *   Function<A, B>         →  one in (A), one out (B)
 *   BiFunction<A, B, C>    →  two in (A, B), one out (C)
 *   UnaryOperator<A>       →  one in (A), same type out (A)
 *   BinaryOperator<A>      →  two in (A, A), same type out (A)
 */
public class Impl06 {

    public static void main(String[] args) {

        // --- BiFunction: two inputs, different types allowed ---

        // Takes a String and an Integer, returns a String
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        System.out.println(repeat.apply("ab", 3));   // ababab
        System.out.println(repeat.apply("ha", 4));   // hahahaha

        // Takes two different types, produces a third
        BiFunction<String, Integer, Boolean> longerThan = (s, n) -> s.length() > n;
        System.out.println(longerThan.apply("hello", 3));  // true  (5 > 3)
        System.out.println(longerThan.apply("hi", 3));     // false (2 > 3)

        // BiFunction also has andThen — apply the BiFunction, then a Function on the result
        BiFunction<Integer, Integer, Integer> multiply = (a, b) -> a * b;
        // multiply then convert to String
        BiFunction<Integer, Integer, String> multiplyToStr = multiply.andThen(n -> "Result: " + n);
        System.out.println(multiplyToStr.apply(6, 7));  // Result: 42

        // --- UnaryOperator: in and out are the same type ---

        // String in, String out
        UnaryOperator<String> trim   = s -> s.trim();
        UnaryOperator<String> upper  = s -> s.toUpperCase();
        UnaryOperator<Integer> doubler = n -> n * 2;

        System.out.println("\nUnaryOperator:");
        System.out.println(trim.apply("  hello  "));  // "hello"
        System.out.println(upper.apply("world"));     // WORLD
        System.out.println(doubler.apply(21));         // 42

        // UnaryOperator inherits andThen from Function
        UnaryOperator<String> trimThenUpper = s -> trim.andThen(upper).apply(s);
        System.out.println(trimThenUpper.apply("  java  ")); // JAVA

        // UnaryOperator.identity() — returns input unchanged
        UnaryOperator<String> identity = UnaryOperator.identity();
        System.out.println(identity.apply("no change")); // no change

        // --- BinaryOperator: two same-type inputs, same-type output ---

        BinaryOperator<Integer> add  = (a, b) -> a + b;
        BinaryOperator<Integer> max  = (a, b) -> a > b ? a : b;
        BinaryOperator<String>  concat = (a, b) -> a + b;

        System.out.println("\nBinaryOperator:");
        System.out.println(add.apply(10, 32));        // 42
        System.out.println(max.apply(7, 3));          // 7
        System.out.println(concat.apply("Hello, ", "World!")); // Hello, World!

        // Built-in helpers on BinaryOperator
        BinaryOperator<Integer> minOp = BinaryOperator.minBy(Integer::compareTo);
        BinaryOperator<Integer> maxOp = BinaryOperator.maxBy(Integer::compareTo);
        System.out.println(minOp.apply(10, 3));  // 3
        System.out.println(maxOp.apply(10, 3));  // 10
    }
}
