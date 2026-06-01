package poc03;

import java.util.function.Function;

/**
 * IMPL 09 — Custom Generic Functional Interface + Checked Exceptions
 *
 * Problem:
 *   Java's built-in Function<T, R> cannot throw checked exceptions.
 *   Checked exceptions are the ones you MUST handle (like IOException,
 *   ParseException). The compiler forces you to catch or declare them.
 *
 *   This means you cannot write:
 *     Function<String, Integer> parse = s -> Integer.parseInt(s); // fine, unchecked
 *
 *   But you CAN'T write a Function that calls something like:
 *     new FileInputStream(path)   ← throws IOException (checked)
 *
 *   The lambda would be rejected by the compiler because Function's
 *   apply() method declares no checked exceptions.
 *
 * Solution:
 *   Define a custom functional interface whose method declares
 *   `throws Exception`. Then wrap it into a normal Function using
 *   a static helper that catches the checked exception and rethrows
 *   it as an unchecked RuntimeException.
 *
 * This pattern is widely used in real Java code.
 */
public class Impl09 {

    // --- Custom functional interfaces (must be at class level, not inside methods) ---

    @FunctionalInterface
    interface ThrowingBiFunction<T, U, R> {
        R apply(T t, U u) throws Exception;
    }

    @FunctionalInterface
    interface ThrowingFunction<T, R> {
        // Unlike Function.apply(), this declares throws Exception
        R apply(T t) throws Exception;
    }

    // --- Wrapper: convert ThrowingFunction → normal Function ---
    // Catches any checked exception and rethrows as RuntimeException
    static <T, R> Function<T, R> wrap(ThrowingFunction<T, R> fn) {
        return t -> {
            try {
                return fn.apply(t);
            } catch (Exception e) {
                // RuntimeException is unchecked — no need to declare it
                throw new RuntimeException("Wrapped checked exception", e);
            }
        };
    }

    // --- Simulated "risky" operations that throw checked exceptions ---

    // Parses integer; throws checked exception on bad input (simulated)
    static Integer riskyParse(String s) throws Exception {
        if (s == null || s.isBlank()) {
            throw new Exception("Cannot parse blank/null string");
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new Exception("Not a valid integer: '" + s + "'", e);
        }
    }

    public static void main(String[] args) {

        // --- Using ThrowingFunction directly ---
        ThrowingFunction<String, Integer> throwingParse = s -> riskyParse(s);

        System.out.println("ThrowingFunction:");
        try {
            System.out.println(throwingParse.apply("42"));     // 42
            System.out.println(throwingParse.apply("  7  "));  // 7 (trims whitespace)
            System.out.println(throwingParse.apply("bad"));    // throws
        } catch (Exception e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // --- Wrapping ThrowingFunction into a normal Function ---
        // Now you can pass it anywhere that expects a Function<String, Integer>
        Function<String, Integer> safeParse = wrap(s -> riskyParse(s));

        System.out.println("\nWrapped as Function (happy path):");
        System.out.println(safeParse.apply("100")); // 100

        System.out.println("\nWrapped as Function (bad input — throws RuntimeException):");
        try {
            System.out.println(safeParse.apply("oops")); // throws RuntimeException
        } catch (RuntimeException e) {
            System.out.println("RuntimeException: " + e.getMessage());
            System.out.println("Cause: " + e.getCause().getMessage());
        }

        // --- The root problem: why we need this ---
        // This would NOT compile (uncomment to see the compiler error):
        //
        // Function<String, Integer> broken = s -> riskyParse(s);
        // Error: Unhandled exception: java.lang.Exception
        //
        // Function.apply() doesn't declare throws Exception, so the compiler
        // refuses any lambda body that can throw a checked exception.

        // --- Custom generic functional interface with two type params ---

        ThrowingBiFunction<String, String, String> risky = (a, b) -> {
            if (a.isEmpty() || b.isEmpty()) throw new Exception("Empty input");
            return a + b;
        };

        System.out.println("\nThrowingBiFunction:");
        try {
            System.out.println(risky.apply("Hello, ", "World!"));  // Hello, World!
            System.out.println(risky.apply("", "World!"));          // throws
        } catch (Exception e) {
            System.out.println("Caught: " + e.getMessage());
        }
    }
}
