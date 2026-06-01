package poc05;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * IMPL 04 — BiConsumer<T, U>
 *
 * BiConsumer<T, U> is Consumer with TWO inputs instead of one.
 *   - Takes TWO inputs: first of type T, second of type U (can be different types)
 *   - Returns NOTHING (void)
 *
 * The single abstract method is: void accept(T t, U u)
 *
 * Java source (simplified):
 *   @FunctionalInterface
 *   public interface BiConsumer<T, U> {
 *       void accept(T t, U u);
 *       default BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) { ... }
 *   }
 *
 * "Bi" is Latin for "two". This naming pattern is consistent across java.util.function:
 *   Consumer    → BiConsumer
 *   Function    → BiFunction
 *   Predicate   → BiPredicate
 *
 * Most common use: Map.forEach(BiConsumer<K, V>)
 * Every time you write map.forEach((key, value) -> ...) you're using a BiConsumer.
 */
public class Impl04 {

    public static void main(String[] args) {

        // --- Basic BiConsumer ---

        // Two parameters, no return value.
        BiConsumer<String, Integer> printScore = (name, score) ->
            System.out.println(name + " scored " + score);

        printScore.accept("Alice", 95); // Alice scored 95
        printScore.accept("Bob", 80);   // Bob scored 80

        // --- BiConsumer with Map.forEach() ---

        // Map.forEach(BiConsumer<K,V>) calls accept(key, value) for each entry.
        // This is the cleanest way to iterate a Map in modern Java.
        Map<String, Integer> inventory = new HashMap<>();
        inventory.put("apples", 10);
        inventory.put("bananas", 5);
        inventory.put("oranges", 8);

        inventory.forEach((item, count) ->
            System.out.println("  " + item + ": " + count + " units")
        );

        // --- BiConsumer.andThen() ---

        // Same contract as Consumer.andThen():
        // both BiConsumers receive the SAME two arguments.
        // First runs, then second. No value flows between them.
        BiConsumer<String, String> greet = (name, lang) ->
            System.out.println("[greet] Hello " + name + " speaking " + lang);
        BiConsumer<String, String> audit = (name, lang) ->
            System.out.println("[audit] " + name + " selected language: " + lang);

        BiConsumer<String, String> greetAndAudit = greet.andThen(audit);
        greetAndAudit.accept("Alice", "Java");
        // [greet] Hello Alice speaking Java
        // [audit] Alice selected language: Java

        // --- BiConsumer populating a result map ---

        // Common pattern: transform key-value pairs from one map into another.
        Map<String, Integer> prices = Map.of("coffee", 3, "tea", 2, "juice", 4);
        Map<String, Integer> discounted = new HashMap<>();

        // BiConsumer<K,V> that writes into 'discounted'
        BiConsumer<String, Integer> applyDiscount = (item, price) ->
            discounted.put(item, price - 1);

        prices.forEach(applyDiscount);
        System.out.println("Discounted: " + discounted); // {coffee=2, tea=1, juice=3}

        // --- BiConsumer with different type combinations ---

        // T = Integer (index), U = String (value) — useful for enumeration
        BiConsumer<Integer, String> indexed = (i, s) ->
            System.out.println("  [" + i + "] " + s);

        java.util.List<String> fruits = java.util.List.of("apple", "banana", "cherry");
        for (int i = 0; i < fruits.size(); i++) {
            indexed.accept(i, fruits.get(i));
        }
        // [0] apple
        // [1] banana
        // [2] cherry
    }
}
