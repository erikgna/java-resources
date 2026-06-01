package poc03;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * IMPL 04 — Built-in: Consumer<T> and BiConsumer<T, U>
 *
 * Consumer<T>
 *   - T = input type
 *   - Returns: void (nothing)
 *   - Abstract method: void accept(T t)
 *   - "Do something with one thing, produce no result."
 *   - Use case: side effects — printing, saving, sending, logging.
 *
 * BiConsumer<T, U>
 *   - Takes TWO inputs, returns void
 *   - Abstract method: void accept(T t, U u)
 *
 * andThen(Consumer after)
 *   - Run this consumer, then run `after` on the same input.
 *   - Chains multiple side effects together.
 */
public class Impl04 {

    public static void main(String[] args) {

        // Basic Consumer
        Consumer<String> print = s -> System.out.println(s);
        Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());
        Consumer<Integer> printDouble = n -> System.out.println(n * 2);

        print.accept("hello");       // hello
        printUpper.accept("hello");  // HELLO
        printDouble.accept(5);       // 10

        // --- andThen: chain consumers ---
        // First run `print`, then run `printUpper`, on the SAME input
        Consumer<String> printThenPrintUpper = print.andThen(printUpper);
        System.out.println("\nandThen:");
        printThenPrintUpper.accept("java");
        // Output:
        // java
        // JAVA

        // Chain three consumers
        Consumer<String> logPrefix = s -> System.out.println("[LOG] " + s);
        Consumer<String> fullChain = print.andThen(printUpper).andThen(logPrefix);
        System.out.println("\ntriple chain:");
        fullChain.accept("chain");
        // chain
        // CHAIN
        // [LOG] chain

        // --- BiConsumer: two inputs, no output ---
        BiConsumer<String, Integer> printWithRepeat = (s, n) -> {
            for (int i = 0; i < n; i++) {
                System.out.println(s);
            }
        };

        System.out.println("\nBiConsumer (repeat 3 times):");
        printWithRepeat.accept("hello", 3);

        // BiConsumer andThen
        BiConsumer<String, Integer> printAndLog = printWithRepeat.andThen(
            (s, n) -> System.out.println("[done printing '" + s + "' " + n + " times]")
        );

        System.out.println("\nBiConsumer andThen:");
        printAndLog.accept("world", 2);

        // Passing Consumer to a method
        System.out.println("\npassToMethod:");
        processString("functional", print);
        processString("functional", printUpper);
    }

    static void processString(String value, Consumer<String> action) {
        // Consumers are useful here: caller decides what "process" means
        action.accept(value);
    }
}
