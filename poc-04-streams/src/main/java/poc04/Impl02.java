package poc04;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * IMPL 02 — Streams from Arrays
 *
 * You can create streams from arrays, not just lists.
 * Java has two paths depending on the array type:
 *
 *   - Object arrays (String[], Integer[]) → Arrays.stream(array) → Stream<T>
 *   - Primitive arrays (int[], double[]) → Arrays.stream(array) → IntStream / DoubleStream
 *
 * Why does the primitive distinction matter?
 *   Stream<Integer> boxes each int into an Integer object (heap allocation per element).
 *   IntStream holds actual int primitives — no boxing, faster and less memory.
 *   For large numeric workloads, always prefer IntStream over Stream<Integer>.
 *
 * IntStream also provides specialized operations that Stream<Integer> does not:
 *   sum(), average(), min(), max() — these only make sense for numbers.
 */
public class Impl02 {

    public static void main(String[] args) {

        // --- Stream from a String array ---

        String[] colors = {"red", "green", "blue", "yellow", "purple"};

        // Arrays.stream(T[]) returns Stream<T>
        Stream<String> colorStream = Arrays.stream(colors);

        System.out.println("--- Colors from array ---");
        colorStream.forEach(c -> System.out.println(c));

        // --- Stream from an int[] (primitive array) ---

        int[] scores = {85, 92, 78, 95, 60, 88};

        // Arrays.stream(int[]) returns IntStream — NOT Stream<Integer>
        // IntStream works with raw int values (no boxing overhead)
        IntStream scoreStream = Arrays.stream(scores);

        System.out.println("\n--- Scores from int[] ---");
        scoreStream.forEach(s -> System.out.println(s));

        // --- IntStream special numeric operations ---

        int sum = Arrays.stream(scores).sum();
        double average = Arrays.stream(scores).average().getAsDouble(); // returns OptionalDouble
        int max = Arrays.stream(scores).max().getAsInt();               // returns OptionalInt
        int min = Arrays.stream(scores).min().getAsInt();

        System.out.println("\n--- Numeric operations on IntStream ---");
        System.out.println("Sum:     " + sum);
        System.out.println("Average: " + average);
        System.out.println("Max:     " + max);
        System.out.println("Min:     " + min);

        // --- IntStream.range() and IntStream.rangeClosed() ---
        // These generate a sequence of integers without needing an array.
        // range(start, end)       → start up to but NOT including end
        // rangeClosed(start, end) → start up to AND including end

        System.out.println("\n--- IntStream.range(1, 6) ---");
        IntStream.range(1, 6).forEach(i -> System.out.print(i + " ")); // 1 2 3 4 5
        System.out.println();

        System.out.println("\n--- IntStream.rangeClosed(1, 6) ---");
        IntStream.rangeClosed(1, 6).forEach(i -> System.out.print(i + " ")); // 1 2 3 4 5 6
        System.out.println();

        // --- Converting IntStream back to Stream<Integer> ---
        // Use .boxed() to convert primitive IntStream to Stream<Integer>
        // This is needed when you want to collect into a List<Integer> or use Stream<T> methods.
        System.out.println("\n--- boxed() converts IntStream to Stream<Integer> ---");
        IntStream.rangeClosed(1, 5)
            .boxed() // IntStream → Stream<Integer>
            .forEach(i -> System.out.println("Boxed: " + i));

        // --- Stream.of() with explicit values ---
        // Not an array, not a list — just inline values
        System.out.println("\n--- Stream.of() direct values ---");
        Stream.of("one", "two", "three").forEach(System.out::println);
    }
}
