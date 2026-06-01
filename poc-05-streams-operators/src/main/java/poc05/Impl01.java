package poc05;

import java.util.function.Supplier;

/**
 * IMPL 01 — Supplier<T>
 *
 * Supplier<T> is a functional interface that:
 *   - Takes NO input
 *   - Returns a value of type T
 *
 * Think of it like a vending machine:
 *   - You don't give it anything.
 *   - It gives you something back each time you press the button.
 *
 * The single abstract method is: T get()
 *
 * Java source (simplified):
 *   @FunctionalInterface
 *   public interface Supplier<T> {
 *       T get();
 *   }
 *
 * Why use Supplier instead of just storing a value in a variable?
 *   - LAZINESS: The Supplier doesn't execute until you call get().
 *     The lambda is just a "recipe" — it hasn't run yet.
 *   - INJECTION: You can pass a Supplier as a method parameter and
 *     let the receiver decide WHEN to call it (or if at all).
 *   - FACTORY: Each call to get() can produce a fresh, brand-new object.
 */
public class Impl01 {

    public static void main(String[] args) {

        // --- Basic Supplier ---

        // The lambda () -> "Hello, Java!" is stored but NOT yet executed.
        // Nothing happens here. It's a plan, not an action.
        Supplier<String> greeting = () -> "Hello, Java!";

        // get() triggers the lambda and returns the value.
        String value = greeting.get();
        System.out.println(value); // Hello, Java!

        // You can call get() multiple times — each call re-executes the lambda.
        System.out.println(greeting.get()); // Hello, Java! (again)

        // --- Supplier as a factory (creates a NEW object each time) ---

        // Each call to get() builds a NEW StringBuilder from scratch.
        // This is the Factory pattern expressed as a Supplier.
        Supplier<StringBuilder> sbFactory = () -> new StringBuilder();

        StringBuilder sb1 = sbFactory.get();
        StringBuilder sb2 = sbFactory.get();
        sb1.append("First");
        sb2.append("Second");

        // sb1 and sb2 are DIFFERENT objects — get() built each one fresh.
        System.out.println(sb1.toString()); // First
        System.out.println(sb2.toString()); // Second
        System.out.println(sb1 == sb2);     // false — different instances

        // --- Different return types ---

        Supplier<Integer> randomInt = () -> (int) (Math.random() * 100);
        System.out.println("Random: " + randomInt.get()); // e.g. 42
        System.out.println("Random: " + randomInt.get()); // e.g. 71 (probably different)

        Supplier<Double> pi = () -> Math.PI;
        System.out.println("Pi: " + pi.get()); // 3.141592653589793

        Supplier<Boolean> coinFlip = () -> Math.random() > 0.5;
        System.out.println("Heads: " + coinFlip.get()); // true or false

        // --- Lazy initialization: the classic use case ---

        // This lambda prints a message WHEN it runs.
        // Notice we created the Supplier but haven't called get() yet.
        Supplier<String> expensive = () -> {
            System.out.println("  [computing now — this is expensive!]");
            return "computed-result";
        };

        System.out.println("Supplier created — nothing ran yet.");
        System.out.println("About to call get()...");
        String result = expensive.get(); // <-- the lambda runs HERE
        System.out.println("Got: " + result);

        // Real-world example: only open a DB connection if you actually need it.
        // Supplier<Connection> db = () -> DriverManager.getConnection(url);
        // The connection isn't opened until something calls db.get().
    }
}
