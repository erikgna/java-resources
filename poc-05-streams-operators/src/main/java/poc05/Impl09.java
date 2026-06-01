package poc05;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * IMPL 09 — Composing operators
 *
 * Every functional interface in java.util.function is designed to COMPOSE.
 * Composition = combining small, single-purpose functions into larger pipelines.
 *
 * Composition methods:
 *
 *   Function.andThen(Function after)
 *     → run THIS, feed output to AFTER
 *     → f.andThen(g) means: x → g(f(x))   (f runs first)
 *
 *   Function.compose(Function before)
 *     → run BEFORE, feed output to THIS
 *     → f.compose(g) means: x → f(g(x))   (g runs first)
 *     → f.andThen(g) == g.compose(f)
 *
 *   Consumer.andThen(Consumer after)
 *     → run THIS consumer, THEN AFTER consumer, with the SAME input
 *     → no output flows between them (Consumer returns void)
 *
 * UnaryOperator inherits andThen/compose from Function.
 * BinaryOperator inherits andThen from BiFunction.
 *
 * IMPORTANT: andThen/compose on UnaryOperator<T> returns Function<T,T>, not UnaryOperator<T>.
 * Java generics lose the "UnaryOperator" type during composition — it becomes Function<T,T>.
 * Functionally identical; the difference is only the declared return type.
 */
public class Impl09 {

    public static void main(String[] args) {

        // ─── Function.andThen vs compose ──────────────────────────────────────────

        // f: x → x + 3
        // g: x → x * 2
        Function<Integer, Integer> f = x -> x + 3;
        Function<Integer, Integer> g = x -> x * 2;

        // f.andThen(g): first add 3, then multiply by 2
        //   input=5 → f(5)=8 → g(8)=16
        System.out.println(f.andThen(g).apply(5)); // 16

        // f.compose(g): first multiply by 2, then add 3
        //   input=5 → g(5)=10 → f(10)=13
        System.out.println(f.compose(g).apply(5)); // 13

        // ─── UnaryOperator composition ─────────────────────────────────────────────

        UnaryOperator<String> trim   = String::trim;
        UnaryOperator<String> upper  = String::toUpperCase;
        UnaryOperator<String> exclaim = s -> s + "!";

        // Returns Function<String,String> (not UnaryOperator<String>) — see note above.
        Function<String, String> normalize = trim.andThen(upper).andThen(exclaim);

        System.out.println(normalize.apply("  hello world  ")); // HELLO WORLD!
        System.out.println(normalize.apply("  java          ")); // JAVA!

        // Using the composed function with replaceAll:
        List<String> words = new ArrayList<>(List.of("  foo  ", "  bar  ", "  baz  "));
        words.replaceAll(s -> normalize.apply(s));
        System.out.println(words); // [FOO!, BAR!, BAZ!]

        // ─── Consumer.andThen — multi-step side effects ───────────────────────────

        // Both consumers receive the SAME value. First runs, then second.
        // No data flows between them — they both consume the original input independently.
        Consumer<String> log    = s -> System.out.println("[LOG]   " + s);
        Consumer<String> save   = s -> System.out.println("[SAVED] " + s);
        Consumer<String> notify = s -> System.out.println("[NOTIF] Sent alert for: " + s);

        Consumer<String> pipeline = log.andThen(save).andThen(notify);
        pipeline.accept("Order #42 created");
        // [LOG]   Order #42 created
        // [SAVED] Order #42 created
        // [NOTIF] Sent alert for: Order #42 created

        // ─── Supplier + Function + Consumer: a mini pipeline ──────────────────────

        // Supplier produces the data.
        Supplier<List<Integer>> dataSource = () -> List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // UnaryOperator (stored as Function) transforms each element.
        Function<Integer, Integer> squareThenNegate = ((UnaryOperator<Integer>) n -> n * n)
            .andThen(n -> -n);

        // Consumer logs each result.
        Consumer<Integer> logResult = n -> System.out.println("  result: " + n);

        // Wire them together:
        System.out.println("\nSquare then negate, for evens only:");
        dataSource.get().stream()
            .filter(n -> n % 2 == 0)
            .map(squareThenNegate::apply)
            .forEach(logResult);
        // result: -4, -16, -36, -64, -100

        // ─── BinaryOperator + Function.andThen ────────────────────────────────────

        // BinaryOperator reduces two values; we can use .andThen (via BiFunction cast) to transform the result.
        java.util.function.BiFunction<Integer, Integer, String> sumThenFormat =
            ((java.util.function.BiFunction<Integer, Integer, Integer>) (BinaryOperator<Integer>) Integer::sum)
                .andThen(n -> "Total = " + n);

        System.out.println(sumThenFormat.apply(7, 8)); // Total = 15
    }
}
