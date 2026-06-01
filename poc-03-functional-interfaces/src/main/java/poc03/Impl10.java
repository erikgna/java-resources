package poc03;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * IMPL 10 — Real-World Pipeline (putting it all together)
 *
 * This is the synthesis implementation. It combines:
 *   - Predicate  → filter data
 *   - Function   → transform data
 *   - Consumer   → consume/output data
 *   - Supplier   → produce/source data
 *   - Composition → chain operations into a pipeline
 *   - Method references → concise syntax
 *
 * Scenario: process a list of users.
 *   1. Filter: only active adult users (age >= 18 AND isActive)
 *   2. Transform: User → a formatted report string
 *   3. Consume: print each report line, also collect to a result list
 *
 * This pattern mirrors how Java Streams work internally.
 * Understanding it makes Streams (covered later) much easier to grasp.
 */
public class Impl10 {

    // --- Domain model ---

    static class User {
        private final String name;
        private final int age;
        private final boolean active;

        User(String name, int age, boolean active) {
            this.name = name;
            this.age = age;
            this.active = active;
        }

        String getName()    { return name; }
        int getAge()        { return age; }
        boolean isActive()  { return active; }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + ", active=" + active + "}";
        }
    }

    // --- Pipeline engine ---
    // A generic pipeline: takes a list, applies filter → transform → consume

    static <T, R> List<R> pipeline(
        List<T> input,
        Predicate<T> filter,
        Function<T, R> transform,
        Consumer<R> output
    ) {
        List<R> results = new ArrayList<>();

        for (T item : input) {
            // Step 1: filter — skip items that don't pass the predicate
            if (!filter.test(item)) {
                continue;
            }
            // Step 2: transform — convert the item to a different type
            R transformed = transform.apply(item);
            // Step 3: consume — side effect (print, log, send, etc.)
            output.accept(transformed);
            // Collect for the caller
            results.add(transformed);
        }

        return results;
    }

    public static void main(String[] args) {

        // --- Data source ---
        Supplier<List<User>> dataSource = () -> {
            List<User> users = new ArrayList<>();
            users.add(new User("Alice",   30, true));
            users.add(new User("Bob",     16, true));   // minor — should be filtered
            users.add(new User("Carol",   25, false));  // inactive — should be filtered
            users.add(new User("Dave",    40, true));
            users.add(new User("Eve",     17, false));  // minor AND inactive
            users.add(new User("Frank",   22, true));
            return users;
        };

        // --- Define pipeline stages ---

        // Filter: must be 18+ AND active
        Predicate<User> isAdult  = user -> user.getAge() >= 18;
        Predicate<User> isActive = User::isActive; // method reference
        Predicate<User> eligible = isAdult.and(isActive);

        // Transform: User → formatted report string
        Function<User, String> toReport = user ->
            String.format("[REPORT] %-10s | Age: %2d | Status: ACTIVE", user.getName(), user.getAge());

        // Consume: print each report, and also track count via a wrapper
        int[] count = {0}; // array trick: lambdas can't capture mutable local vars,
                           // but they CAN read/write array elements
        Consumer<String> printer = line -> {
            System.out.println(line);
            count[0]++;
        };

        // --- Run the pipeline ---
        System.out.println("=== User Report ===");
        List<User> allUsers = dataSource.get();
        List<String> reports = pipeline(allUsers, eligible, toReport, printer);

        System.out.println("\nTotal eligible users: " + count[0]);
        System.out.println("Filtered out:         " + (allUsers.size() - count[0]));

        // --- Run different pipelines on the same data ---

        System.out.println("\n=== Names Only (all active, any age) ===");
        pipeline(
            allUsers,
            User::isActive,               // method ref for filter
            User::getName,                // method ref for transform
            System.out::println           // method ref for consume
        );

        System.out.println("\n=== Minors (under 18) ===");
        Predicate<User> isMinor = isAdult.negate();
        pipeline(
            allUsers,
            isMinor,
            u -> u.getName() + " (age " + u.getAge() + ")",
            System.out::println
        );

        // --- Compose a more complex transform ---
        Function<User, String> nameOnly       = User::getName;
        Function<String, String> addPrefix    = name -> ">> " + name;
        Function<User, String> prefixedName   = nameOnly.andThen(addPrefix);

        System.out.println("\n=== Prefixed names (eligible only) ===");
        pipeline(allUsers, eligible, prefixedName, System.out::println);
    }
}
