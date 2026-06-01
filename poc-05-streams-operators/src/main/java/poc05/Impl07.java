package poc05;

import java.util.Comparator;
import java.util.function.BinaryOperator;

/**
 * IMPL 07 — BinaryOperator<T>
 *
 * BinaryOperator<T> is a functional interface that:
 *   - Takes TWO inputs of the SAME type T
 *   - Returns a value of the SAME type T
 *
 * "Binary" means "two operands". Like +, *, max — you put two things in, get one back.
 *
 *   (T, T) → T
 *   (String, String) → String
 *   (Integer, Integer) → Integer
 *
 * BinaryOperator<T> EXTENDS BiFunction<T, T, T>.
 * Same "same type" convention as UnaryOperator extending Function<T,T>.
 * The interface exists to make the contract explicit and readable.
 *
 * Java source (simplified):
 *   @FunctionalInterface
 *   public interface BinaryOperator<T> extends BiFunction<T, T, T> {
 *       static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) { ... }
 *       static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) { ... }
 *       // apply(T t1, T t2) is inherited from BiFunction
 *   }
 *
 * Common uses:
 *   1. Stream.reduce(identity, BinaryOperator)   — fold stream into one value (Impl08)
 *   2. Math.operations: Integer::sum, Integer::max, Integer::min
 *   3. BinaryOperator.minBy / maxBy             — pick min or max via Comparator
 */
public class Impl07 {

    public static void main(String[] args) {

        // --- Basic BinaryOperator ---

        BinaryOperator<Integer> add  = (a, b) -> a + b;
        BinaryOperator<Integer> mult = (a, b) -> a * b;
        BinaryOperator<Integer> max  = (a, b) -> a > b ? a : b;
        BinaryOperator<Integer> min  = (a, b) -> a < b ? a : b;

        System.out.println(add.apply(3, 4));  // 7
        System.out.println(mult.apply(3, 4)); // 12
        System.out.println(max.apply(3, 4));  // 4
        System.out.println(min.apply(3, 4));  // 3

        // --- Method references as BinaryOperator ---

        // Integer.sum(a, b), Integer.max(a, b), Integer.min(a, b)
        // all have the signature (int, int) → int, so they work as BinaryOperator<Integer>.
        BinaryOperator<Integer> sum  = Integer::sum;
        BinaryOperator<Integer> maxB = Integer::max;
        BinaryOperator<Integer> minB = Integer::min;

        System.out.println(sum.apply(10, 20));  // 30
        System.out.println(maxB.apply(10, 20)); // 20
        System.out.println(minB.apply(10, 20)); // 10

        // --- BinaryOperator on Strings ---

        BinaryOperator<String> concat  = (a, b) -> a + " " + b;
        BinaryOperator<String> longer  = (a, b) -> a.length() >= b.length() ? a : b;
        BinaryOperator<String> shorter = (a, b) -> a.length() <= b.length() ? a : b;

        System.out.println(concat.apply("Hello", "World"));     // Hello World
        System.out.println(longer.apply("cat", "elephant"));    // elephant
        System.out.println(shorter.apply("cat", "elephant"));   // cat

        // --- BinaryOperator.minBy() and maxBy() —static factory methods ---

        // These build a BinaryOperator that picks the min or max
        // according to a given Comparator. No manual a < b logic needed.
        Comparator<String> byLength = Comparator.comparingInt(String::length);

        BinaryOperator<String> shortestOp = BinaryOperator.minBy(byLength);
        BinaryOperator<String> longestOp  = BinaryOperator.maxBy(byLength);

        System.out.println(shortestOp.apply("java", "programming")); // java
        System.out.println(longestOp.apply("java", "programming"));  // programming

        // minBy/maxBy are especially useful inside Stream.reduce() (next impl).

        // --- BinaryOperator on Doubles ---

        BinaryOperator<Double> average = (a, b) -> (a + b) / 2.0;
        System.out.println(average.apply(10.0, 20.0)); // 15.0

        BinaryOperator<Double> hypotenuse = (a, b) -> Math.sqrt(a * a + b * b);
        System.out.println(hypotenuse.apply(3.0, 4.0)); // 5.0 (3-4-5 right triangle)
    }
}
