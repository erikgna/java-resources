package poc05;

import java.util.List;
import java.util.function.Consumer;

/**
 * IMPL 03 — Consumer<T>
 *
 * Consumer<T> is a functional interface that:
 *   - Takes ONE input of type T
 *   - Returns NOTHING (void)
 *
 * Think of it like a shredder:
 *   - You feed it something.
 *   - It processes the thing and produces no output.
 *
 * The single abstract method is: void accept(T t)
 *
 * Java source (simplified):
 *   @FunctionalInterface
 *   public interface Consumer<T> {
 *       void accept(T t);
 *       default Consumer<T> andThen(Consumer<? super T> after) { ... }
 *   }
 *
 * Consumer is the "side effect" interface.
 * Its entire purpose IS the side effect: print, log, save, send, accumulate.
 * It is NOT meant to transform data — that's Function/UnaryOperator's job.
 *
 * Most common use: Stream.forEach(Consumer<T>)
 * Every time you write .forEach(item -> doSomething(item)), the lambda IS a Consumer.
 */
public class Impl03 {

    public static void main(String[] args) {

        // --- Basic Consumer ---

        // The lambda takes one String and returns nothing (void).
        Consumer<String> printer = s -> System.out.println("[LOG] " + s);

        // accept() executes the lambda with the given argument.
        printer.accept("Hello"); // [LOG] Hello
        printer.accept("World"); // [LOG] World

        // --- Consumer in forEach ---

        // This is the most common Consumer use in Java.
        // forEach(Consumer<T>) calls consumer.accept(element) for each element.
        List<String> names = List.of("Alice", "Bob", "Charlie");
        names.forEach(name -> System.out.println("Name: " + name));

        // Method reference as Consumer.
        // System.out::println is a reference to the println method.
        // It matches Consumer<String> because println(String) takes one arg and returns void.
        names.forEach(System.out::println);

        // --- Consumer.andThen() — chain two consumers ---

        // andThen() returns a NEW Consumer that:
        //   1. Runs THIS consumer with the input.
        //   2. Then runs the AFTER consumer with the SAME input.
        //
        // Key point: both consumers receive the ORIGINAL input.
        // The "output" of the first consumer does NOT flow to the second
        // (there is no output — Consumer returns void).
        Consumer<String> log   = s -> System.out.println("  [log]   " + s);
        Consumer<String> upper = s -> System.out.println("  [UPPER] " + s.toUpperCase());

        Consumer<String> logThenUpper = log.andThen(upper);
        logThenUpper.accept("hello");
        // [log]   hello
        // [UPPER] HELLO

        // --- Consumer accumulating state ---

        // Consumer's side effect can be modifying external state.
        // Here we use the int[] array trick (same as Impl02) to allow mutation.
        int[] sum = {0};
        Consumer<Integer> addToSum = n -> sum[0] += n;
        List.of(1, 2, 3, 4, 5).forEach(addToSum);
        System.out.println("Sum: " + sum[0]); // 15

        // --- Consumer with different types ---

        Consumer<Integer> printSquare = n -> System.out.println(n + "^2 = " + (n * n));
        printSquare.accept(4); // 4^2 = 16
        printSquare.accept(7); // 7^2 = 49

        Consumer<Double> printRounded = d -> System.out.printf("%.2f%n", d);
        printRounded.accept(3.14159); // 3.14

        // --- Chaining three consumers ---

        Consumer<String> step1 = s -> System.out.println("  Step 1 received: " + s);
        Consumer<String> step2 = s -> System.out.println("  Step 2 uppercased: " + s.toUpperCase());
        Consumer<String> step3 = s -> System.out.println("  Step 3 length: " + s.length());

        Consumer<String> pipeline = step1.andThen(step2).andThen(step3);
        pipeline.accept("java"); // all three fire with "java"
    }
}
