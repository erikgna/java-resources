package poc05;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IMPL 06 — UnaryOperator<T> with Stream.iterate()
 *
 * Stream.iterate(seed, UnaryOperator<T>) creates an INFINITE stream
 * where each element is produced by applying the operator to the PREVIOUS element.
 *
 *   seed, op(seed), op(op(seed)), op(op(op(seed))), ...
 *   i.e.: e0=seed, e1=op(e0), e2=op(e1), e3=op(e2), ...
 *
 * This models any SEQUENCE where each term depends on the previous:
 *   - Doubling: 1, 2, 4, 8, 16, ...
 *   - Incrementing: 0, 1, 2, 3, 4, ...
 *   - Fibonacci (with array trick): 0, 1, 1, 2, 3, 5, ...
 *
 * Stream.iterate() vs Stream.generate() — the key difference:
 *   generate(supplier)      — elements are INDEPENDENT (supplier called fresh each time)
 *   iterate(seed, operator) — elements form a SEQUENCE (each depends on previous)
 *
 * Java 9+ added a 3-arg form that takes a Predicate as a stopping condition:
 *   Stream.iterate(seed, predicate, operator) — like a for-loop
 *
 * Always use limit() or the 3-arg form to stop an iterate() stream,
 * or it will run forever.
 */
public class Impl06 {

    public static void main(String[] args) {

        // --- Powers of 2: 1, 2, 4, 8, 16, ... ---

        // Start at 1. Each next element = previous * 2.
        List<Integer> powersOf2 = Stream.iterate(1, n -> n * 2)
            .limit(8)
            .collect(Collectors.toList());
        System.out.println("Powers of 2: " + powersOf2);
        // [1, 2, 4, 8, 16, 32, 64, 128]

        // --- Counting up: 0, 1, 2, 3, 4 ---

        UnaryOperator<Integer> addOne = n -> n + 1;
        List<Integer> counting = Stream.iterate(0, addOne)
            .limit(5)
            .collect(Collectors.toList());
        System.out.println("Counting: " + counting); // [0, 1, 2, 3, 4]

        // --- Fibonacci sequence using an array pair ---

        // Fibonacci: each number = sum of previous two.
        // Problem: iterate only gives us one "previous" value.
        // Solution: pass a 2-element array [a, b] as the state.
        //   Current value = pair[0]
        //   Next state    = [pair[1], pair[0] + pair[1]]
        List<Integer> fibonacci = Stream.iterate(
                new int[]{0, 1},                               // seed: [0, 1]
                pair -> new int[]{pair[1], pair[0] + pair[1]}  // next: [1, 0+1=1], [1, 1+1=2], ...
            )
            .limit(10)
            .map(pair -> pair[0]) // extract the "current" number from each pair
            .collect(Collectors.toList());
        System.out.println("Fibonacci: " + fibonacci);
        // [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]

        // --- Java 9: 3-arg iterate with Predicate (bounded, like a for-loop) ---

        // Stream.iterate(seed, predicate, operator)
        //   seed      = starting value
        //   predicate = keep going as long as this is true (stop when false)
        //   operator  = how to compute next value from current
        //
        // Equivalent for-loop: for (int i = 0; i < 20; i += 3)
        List<Integer> every3 = Stream.iterate(0, n -> n < 20, n -> n + 3)
            .collect(Collectors.toList());
        System.out.println("0 to <20 step 3: " + every3);
        // [0, 3, 6, 9, 12, 15, 18]

        // --- String growing sequence ---

        // Start with "a". Each step appends "a".
        // Shows that iterate works on any type, not just numbers.
        List<String> growing = Stream.iterate("a", s -> s + "a")
            .limit(5)
            .collect(Collectors.toList());
        System.out.println("Growing: " + growing);
        // [a, aa, aaa, aaaa, aaaaa]

        // --- Comparing iterate vs generate side-by-side ---

        System.out.println("\n-- iterate: 1, 3, 5, 7, 9 (each = prev + 2) --");
        Stream.iterate(1, n -> n + 2).limit(5).forEach(System.out::println);

        System.out.println("-- generate: always 1 (independent, no memory) --");
        Stream.generate(() -> 1).limit(5).forEach(System.out::println);
    }
}
