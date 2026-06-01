package poc03;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * IMPL 05 — Built-in: Supplier<T>
 *
 * Supplier<T>
 *   - Takes NO input
 *   - Returns: T
 *   - Abstract method: T get()
 *   - "Produce a value on demand."
 *
 * Key concept: LAZY evaluation.
 *   A Supplier does NOT compute its value until you call get().
 *   This is different from assigning a value directly:
 *
 *     String value = expensiveComputation();  // runs immediately
 *     Supplier<String> lazy = () -> expensiveComputation(); // runs only when you call lazy.get()
 *
 * Use cases:
 *   - Defer expensive work until it's actually needed
 *   - Factory / object creation patterns
 *   - Default values (only compute if needed)
 *   - Dependency injection
 */
public class Impl05 {

    public static void main(String[] args) {

        // Basic Supplier — produces a constant value
        Supplier<String> greeting = () -> "Hello, World!";
        System.out.println(greeting.get()); // Hello, World!
        System.out.println(greeting.get()); // each call to get() re-executes the lambda

        // Supplier producing different values each time (random)
        Supplier<Double> random = () -> Math.random();
        System.out.println(random.get()); // some random number
        System.out.println(random.get()); // different random number each time

        // --- Lazy evaluation ---
        // The lambda body only runs when get() is called
        Supplier<List<String>> lazyList = () -> {
            System.out.println("[building expensive list...]");
            List<String> list = new ArrayList<>();
            list.add("one");
            list.add("two");
            list.add("three");
            return list;
        };

        System.out.println("\nBefore calling get() — nothing printed yet");
        List<String> result = lazyList.get(); // NOW it runs
        System.out.println("Got list: " + result);

        // --- Factory pattern ---
        // Supplier<Person> produces new Person objects on demand
        Supplier<Person> personFactory = () -> new Person("Default", 0);
        Person p1 = personFactory.get();
        Person p2 = personFactory.get();
        System.out.println("\nFactory:");
        System.out.println("p1: " + p1);
        System.out.println("p2: " + p2);
        System.out.println("same object? " + (p1 == p2)); // false — each get() makes a new one

        // --- Default value pattern ---
        // If you have a nullable value, use a Supplier for the fallback
        String maybeNull = null;
        System.out.println("\nDefault value:");
        System.out.println(getOrDefault(maybeNull, () -> "fallback value")); // fallback value
        System.out.println(getOrDefault("actual", () -> "fallback value"));  // actual
    }

    // Returns value if not null, otherwise calls the Supplier to get a default
    static String getOrDefault(String value, Supplier<String> defaultSupplier) {
        if (value != null) {
            return value;
        }
        return defaultSupplier.get(); // only runs the lambda when value is null
    }

    // Simple Person for factory demo
    static class Person {
        String name;
        int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }
}
