package poc05;

import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IMPL 10 — Integrated pipeline: Supplier + Consumer + UnaryOperator + BinaryOperator
 *
 * All four functional interfaces working together in one cohesive scenario.
 * This implementation shows how they naturally divide responsibilities:
 *
 *   Supplier       — WHERE does the data come from?   (the SOURCE)
 *   UnaryOperator  — HOW is each item transformed?    (the TRANSFORM)
 *   Consumer       — WHAT side effect happens?        (the OBSERVE/AUDIT)
 *   BinaryOperator — HOW do we combine all results?   (the AGGREGATE)
 *
 * Scenario: An order processing pipeline.
 *   1. Supplier produces raw orders from a data source (DB, API, queue in real life).
 *   2. UnaryOperator normalizes each order (clean names, apply discount).
 *   3. Consumer logs each processed order and alerts on high-value ones.
 *   4. BinaryOperator sums all order totals into a grand total.
 *
 * This pattern is called ETL (Extract → Transform → Load):
 *   Extract   = Supplier
 *   Transform = UnaryOperator
 *   Audit/Log = Consumer
 *   Aggregate = BinaryOperator + reduce()
 */
public class Impl10 {

    // A simple data record to make the scenario concrete.
    // In real code this would be a proper class with validation, etc.
    static class Order {
        final String item;
        final double price;
        final int qty;

        Order(String item, double price, int qty) {
            this.item  = item;
            this.price = price;
            this.qty   = qty;
        }

        double total() {
            return price * qty;
        }

        @Override
        public String toString() {
            return String.format("Order{%-12s x%d @ %.2f = %.2f}", item, qty, price, total());
        }
    }

    public static void main(String[] args) {

        // ─── 1. SUPPLIER — produce the raw data ───────────────────────────────────
        //
        // In production this would call a repository, REST client, or message queue.
        // The Supplier is lazy: no orders are created until get() is called.
        Supplier<List<Order>> orderSource = () -> List.of(
            new Order("  laptop  ", 999.99, 1), // dirty item names — spaces around them
            new Order("  mouse   ",  29.99, 3),
            new Order("  keyboard",  79.99, 2)
        );

        System.out.println("=== RAW ORDERS (from Supplier) ===");
        List<Order> rawOrders = orderSource.get();
        rawOrders.forEach(System.out::println);

        // ─── 2. UNARY OPERATOR — normalize each order ─────────────────────────────
        //
        // Takes an Order, returns a cleaned Order (same type in, same type out).
        // Trims whitespace from the item name and converts to uppercase.
        UnaryOperator<Order> normalize = o ->
            new Order(o.item.trim().toUpperCase(), o.price, o.qty);

        // Stream.map() accepts Function<T,R>. UnaryOperator<T> extends Function<T,T>,
        // so it works seamlessly here.
        List<Order> cleanOrders = rawOrders.stream()
            .map(normalize)
            .collect(Collectors.toList());

        System.out.println("\n=== NORMALIZED ORDERS (via UnaryOperator) ===");
        cleanOrders.forEach(System.out::println);

        // ─── 3. CONSUMER — audit side effects ─────────────────────────────────────
        //
        // Consumer takes an Order, returns nothing, produces side effects.
        // We chain two consumers with andThen():
        //   auditLog     — always prints a log line
        //   highValueAlert — conditionally prints an alert for large orders

        Consumer<Order> auditLog = o ->
            System.out.printf("  [AUDIT] %-10s total=%.2f%n", o.item, o.total());

        Consumer<Order> highValueAlert = o -> {
            if (o.total() > 500.0) {
                System.out.println("  [ALERT] High-value order detected: " + o.item);
            }
        };

        Consumer<Order> fullAudit = auditLog.andThen(highValueAlert);

        System.out.println("\n=== AUDIT LOG (via Consumer) ===");
        cleanOrders.forEach(fullAudit);

        // ─── 4. BINARY OPERATOR — aggregate totals ────────────────────────────────
        //
        // BinaryOperator<Double> takes two Doubles, returns one Double.
        // reduce() uses it to fold all order totals into a single grand total.
        BinaryOperator<Double> sumTotals = Double::sum;

        Optional<Double> grandTotal = cleanOrders.stream()
            .map(Order::total)     // extract each order's total: Double stream
            .reduce(sumTotals);    // fold: total1 + total2 + total3 → grand total

        System.out.println("\n=== RESULTS ===");
        grandTotal.ifPresent(total -> System.out.printf("Grand Total: %.2f%n", total));

        // ─── BONUS: iterate + generate used together ──────────────────────────────
        //
        // Loyalty tier system:
        //   Supplier       → produces tier names
        //   Stream.iterate → generates discount percentages (5, 10, 15, 20, 25)
        //   BinaryOperator → combines tier name + discount into a label
        System.out.println("\n=== LOYALTY TIERS ===");

        Supplier<Stream<String>> tierNames =
            () -> Stream.of("Bronze", "Silver", "Gold", "Platinum", "Diamond");

        List<Integer> discountPcts = Stream.iterate(5, n -> n + 5)
            .limit(5)
            .collect(Collectors.toList());

        BinaryOperator<String> buildLabel = (tier, pct) -> tier + " → " + pct + "% off";

        List<String> tiers = tierNames.get().collect(Collectors.toList());
        for (int i = 0; i < tiers.size(); i++) {
            System.out.println("  " + buildLabel.apply(tiers.get(i), discountPcts.get(i) + ""));
        }
    }
}
